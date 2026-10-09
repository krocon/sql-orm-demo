package dev.krocon.shop.ep027;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 027: JPQL, Native SQL, HQL-Window-Function/CTE, Criteria API, DTO-Projektion. */
@ShopJpaTest
@Import(OrderQueries.class)
class OrderQueriesTest {

  @Autowired OrderQueries queries;
  @Autowired JdbcTemplate jdbc;

  @Test
  void jpqlDtoProjection() {
    List<OrderSummary> list = queries.summaries(OrderStatus.PAID);
    long paid = jdbc.queryForObject("select count(*) from orders where status = 'PAID'", Long.class);
    System.out.println("PAID-Bestellungen als OrderSummary: " + list.size() + ", z. B. " + list.get(0));
    assertThat(list).hasSize((int) paid);
    assertThat(list.get(0).email()).isNotBlank();
  }

  @Test
  void nativeJsonb() {
    List<Long> ids = queries.orderIdsWithAttrs("{\"channel\": \"app\", \"gift\": true}");
    System.out.println("JSONB @> {channel: app, gift: true}: " + ids.size() + " Bestellungen, z. B. " + ids.subList(0, 3));
    assertThat(ids).isNotEmpty().allMatch(id -> id % 30 == 0);   // app: id % 3 = 0, gift: id % 10 = 0
  }

  @Test
  void windowFunction_nativeAndHqlGiveSameResult() {
    List<OrderRank> nativeTop = queries.topOrderPerCustomerNative(5);
    List<OrderRank> hqlTop = queries.topOrderPerCustomerHql(5);   // HQL: with … rank() over (…)
    nativeTop.forEach(r -> System.out.println("Top je Kunde: " + r));
    assertThat(hqlTop).containsExactlyElementsOf(nativeTop);
  }

  @Test
  void criteriaWithOptionalFilters() {
    List<Order> all = queries.filter(new OrderFilter(null, null, null));
    List<Order> paidBig = queries.filter(new OrderFilter(OrderStatus.PAID, new BigDecimal("400"), null));
    List<Order> forCustomer = queries.filter(new OrderFilter(null, null, "kunde1@shop.example"));
    System.out.println("Criteria: alle " + all.size() + ", PAID ab 400 € " + paidBig.size()
        + ", kunde1 " + forCustomer.size());
    assertThat(all).hasSize(5000);
    assertThat(paidBig).isNotEmpty()
        .allMatch(o -> o.getStatus() == OrderStatus.PAID && o.getTotal().compareTo(new BigDecimal("400")) >= 0);
    assertThat(forCustomer).allMatch(o -> o.getCustomer().getEmail().equals("kunde1@shop.example"));
  }
}
