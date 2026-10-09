package dev.krocon.shop.ep036;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Envers schreibt beim Commit → echte Transaktionen statt Test-Rollback. */
@ShopJpaTest
@Import(AuditConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OrderHistoryTest {
  @Autowired OrderRepository orders;
  @Autowired EntityManager em;
  @Autowired TransactionTemplate tx;
  @Autowired JdbcClient jdbc;
  Long id;

  @AfterEach
  void cleanup() {
    CurrentUser.clear();
    if (id != null) {
      List<Integer> revs = jdbc.sql("select rev from ep036_orders_aud where id = ?").param(id)
          .query(Integer.class).list();
      jdbc.sql("delete from ep036_orders_aud where id = ?").param(id).update();
      revs.forEach(rev -> jdbc.sql("delete from revinfo where rev = ?").param(rev).update());
      jdbc.sql("delete from ep036_orders where id = ?").param(id).update();
    }
  }

  @Test
  void historyOfAnOrder() {
    CurrentUser.set("anna");
    id = tx.execute(s -> orders.save(new Order(new BigDecimal("19.90"))).getId());   // Revision 1: ADD

    CurrentUser.set("ben");
    tx.executeWithoutResult(s -> orders.findById(id).orElseThrow().setStatus(OrderStatus.PAID));  // MOD

    CurrentUser.set("carla");
    tx.executeWithoutResult(s -> orders.deleteById(id));                                    // DEL (soft)

    tx.executeWithoutResult(s -> {
      AuditReader reader = AuditReaderFactory.get(em);
      List<Number> revisions = reader.getRevisions(Order.class, id);
      assertThat(revisions).hasSize(3);

      // Stand von Revision 1: noch NEW, angelegt von anna
      Order first = reader.find(Order.class, id, revisions.get(0));
      assertThat(first.getStatus()).isEqualTo(OrderStatus.NEW);
      assertThat(first.getCreatedBy()).isEqualTo("anna");

      // Revision 2: ben hat auf PAID gesetzt
      Order second = reader.find(Order.class, id, revisions.get(1));
      assertThat(second.getStatus()).isEqualTo(OrderStatus.PAID);
      assertThat(second.getUpdatedBy()).isEqualTo("ben");

      @SuppressWarnings("unchecked")
      List<Object[]> rows = reader.createQuery()
          .forRevisionsOfEntity(Order.class, false, true)
          .add(AuditEntity.id().eq(id))
          .getResultList();
      List<RevisionType> types = rows.stream().map(r -> (RevisionType) r[2]).toList();
      System.out.println("Envers-Revisionen: " + types);
      assertThat(types).containsExactly(RevisionType.ADD, RevisionType.MOD, RevisionType.DEL);
    });

    // Die Zeile selbst ist nur weich gelöscht
    assertThat(jdbc.sql("select deleted from ep036_orders where id = ?").param(id)
        .query(Boolean.class).single()).isTrue();
  }
}
