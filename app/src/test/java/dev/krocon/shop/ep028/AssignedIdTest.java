package dev.krocon.shop.ep028;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.repo.OrderRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Folge 028: save() = isNew ? persist : merge. */
@ShopJpaTest
class AssignedIdTest {

  @Autowired Ep028ProductRepository productRepository;
  @Autowired Ep028PersistableProductRepository persistableRepository;
  @Autowired OrderRepository orderRepository;
  @Autowired EntityManager em;
  Statistics stats;

  @BeforeEach
  void setUp() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
  }

  @Test
  void generatedId_isNew_persist() {
    Order order = new Order(em.find(Customer.class, 1L));
    stats.clear();
    Order saved = orderRepository.save(order);   // id == null → isNew → persist
    assertThat(saved).isSameAs(order);           // persist: dasselbe Objekt, jetzt managed
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);   // nur INSERT (IDENTITY)
  }

  @Test
  void assignedId_merge_selectThenInsert() {
    var p = new Ep028Product(42L, "Mug");        // ID selbst vergeben
    Ep028Product saved = productRepository.save(p);   // isNew false → merge: erst SELECT
    em.flush();
    System.out.println("save() mit eigener ID: " + stats.getPrepareStatementCount() + " Statements (SELECT + INSERT)");
    assertThat(saved).isNotSameAs(p);            // merge liefert eine Kopie, p bleibt detached
    assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
  }

  @Test
  void persistable_onlyInsert() {
    var p = new Ep028PersistableProduct(43L, "Mug");
    Ep028PersistableProduct saved = persistableRepository.save(p);   // isNew() true → persist
    em.flush();
    assertThat(saved).isSameAs(p);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);       // nur INSERT
  }
}
