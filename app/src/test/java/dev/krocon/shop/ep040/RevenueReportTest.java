package dev.krocon.shop.ep040;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.TestcontainersConfiguration;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.repo.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/** Derselbe Umsatzreport viermal: JPA findAll, JdbcClient, MyBatis, jOOQ. Rollback je Test. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class RevenueReportTest {
  @Autowired OrderRepository orders;
  @Autowired RevenueJdbc jdbc;
  @Autowired RevenueMapper mybatis;
  @Autowired RevenueJooq jooq;
  @Autowired OrderService service;
  @Autowired SessionFactory sessionFactory;
  Statistics stats;

  @BeforeEach
  void clear() {
    stats = sessionFactory.getStatistics();
    stats.clear();
  }

  static Map<OrderStatus, BigDecimal> asMap(List<StatusRevenue> rows) {
    return rows.stream().collect(Collectors.toMap(StatusRevenue::status, StatusRevenue::revenue));
  }

  @Test
  void problem_jpaFindAllSumsInJava() {
    // Report mit JPA: alle Bestellungen als Entities laden …
    Map<OrderStatus, BigDecimal> revenue = orders.findAll().stream()
        .collect(groupingBy(Order::getStatus,
            reducing(BigDecimal.ZERO, Order::getTotal, BigDecimal::add)));
    System.out.println("JPA findAll: " + stats.getEntityLoadCount() + " Entities -> " + revenue);
    assertThat(stats.getEntityLoadCount()).isEqualTo(5000);
    assertThat(revenue).isEqualTo(asMap(jdbc.revenueByStatus()));
  }

  @Test
  void jdbcClient_myBatis_jooq_sameResult() {
    List<StatusRevenue> viaJdbc = jdbc.revenueByStatus();
    System.out.println("JdbcClient: " + viaJdbc);
    assertThat(viaJdbc).hasSize(4);
    assertThat(asMap(mybatis.revenueByStatus())).isEqualTo(asMap(viaJdbc));
    assertThat(asMap(jooq.revenueByStatus())).isEqualTo(asMap(viaJdbc));
    assertThat(asMap(jooq.revenueByStatusWithoutCodegen())).isEqualTo(asMap(viaJdbc));
    assertThat(stats.getEntityLoadCount()).isZero();   // keine einzige Entity
  }

  @Test
  void jooq_renderedSql() {
    String sql = jooq.revenueShareSql();
    System.out.println("jOOQ-SQL: " + sql);
    assertThat(sql).contains("over ()").contains("group by");
  }

  @Test
  void mixJpaAndJdbc_needsFlush() {
    BigDecimal total = orders.findById(2L).orElseThrow().getTotal();
    BigDecimal cancelledBefore = asMap(jdbc.revenueByStatus()).get(OrderStatus.CANCELLED);

    // ohne flush: JDBC-Report sieht die Änderung noch nicht
    BigDecimal withoutFlush = asMap(service.cancelAndReportWithoutFlush(2L)).get(OrderStatus.CANCELLED);
    assertThat(withoutFlush).isEqualByComparingTo(cancelledBefore);

    // mit flush (in derselben Transaktion): Änderung ist im Report
    BigDecimal withFlush = asMap(service.cancelAndReport(2L)).get(OrderStatus.CANCELLED);
    assertThat(withFlush).isEqualByComparingTo(cancelledBefore.add(total));
  }
}
