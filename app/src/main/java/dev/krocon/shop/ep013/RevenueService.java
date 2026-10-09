package dev.krocon.shop.ep013;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.repo.OrderRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 013: Umsatz pro Land – einmal über Entities, einmal als natives SQL. */
@Service("ep013RevenueService")
public class RevenueService {

  private final OrderRepository orders;
  private final JdbcClient jdbc;

  public RevenueService(OrderRepository orders, DataSource dataSource) {
    this.orders = orders;
    this.jdbc = JdbcClient.create(dataSource);
  }

  /** Problem: lädt ALLE Bestellungen als Entities und summiert in Java. */
  @Transactional(readOnly = true)
  public Map<String, BigDecimal> revenueWithEntities() {
    Map<String, BigDecimal> revenue = new HashMap<>();
    for (Order o : orders.findAll()) {             // lädt ALLE Bestellungen
      revenue.merge(o.getCustomer().getCountry(),  // + Query pro Kunde
          o.getTotal(), BigDecimal::add);
    }
    return revenue;
  }

  /** Lösung: Report als natives SQL, die Datenbank rechnet. */
  @Transactional(readOnly = true)
  public List<Revenue> revenueWithSql() {
    return jdbc.sql("""
        SELECT c.country, sum(o.total) AS total
        FROM orders o JOIN customers c ON c.id = o.customer_id
        GROUP BY ROLLUP (c.country)
        ORDER BY c.country NULLS LAST""")
      .query(Revenue.class).list();
  }
}
