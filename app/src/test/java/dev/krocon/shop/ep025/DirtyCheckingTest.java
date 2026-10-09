package dev.krocon.shop.ep025;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Folge 025: echte Commits (NOT_SUPPORTED), damit readOnly wirkt.
 * Bestellung 7 wird danach wieder zurückgesetzt.
 */
@ShopJpaTest
@Import(OrderService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DirtyCheckingTest {

  static final long ID = 7L;

  @Autowired OrderService orderService;
  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;
  Statistics stats;
  String status;
  BigDecimal total;

  @BeforeEach
  void remember() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    stats.clear();
    status = jdbc.queryForObject("select status from orders where id = ?", String.class, ID);
    total = dbTotal();
  }

  @AfterEach
  void restore() {
    jdbc.update("update orders set status = ?, total = ? where id = ?", status, total, ID);
  }

  @Test
  void ship_updatesWithoutSave() {
    orderService.ship(ID);
    assertThat(stats.getEntityUpdateCount()).isEqualTo(1);
    assertThat(jdbc.queryForObject("select status from orders where id = ?", String.class, ID)).isEqualTo("SHIPPED");
  }

  @Test
  void accidentalUpdate_roundedTotalLandsInDb() {
    OrderView view = orderService.show(ID);
    System.out.println("vorher " + total + " → angezeigt " + view.total() + " → in der DB jetzt " + dbTotal());
    assertThat(stats.getEntityUpdateCount()).isEqualTo(1);              // niemand hat save() gerufen
    assertThat(dbTotal()).isEqualByComparingTo(total.setScale(0, java.math.RoundingMode.HALF_UP));
    assertThat(dbTotal()).isNotEqualByComparingTo(total);               // Daten verfälscht
  }

  @Test
  void readOnly_noUpdate() {
    orderService.showReadOnly(ID);
    assertThat(stats.getEntityUpdateCount()).isZero();
    assertThat(dbTotal()).isEqualByComparingTo(total);
  }

  @Test
  void dto_noUpdate() {
    OrderView view = orderService.showDto(ID);
    assertThat(view.roundedTotal().scale()).isZero();
    assertThat(stats.getEntityUpdateCount()).isZero();
  }

  private BigDecimal dbTotal() {
    return jdbc.queryForObject("select total from orders where id = ?", BigDecimal.class, ID);
  }
}
