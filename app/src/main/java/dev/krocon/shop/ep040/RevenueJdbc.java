package dev.krocon.shop.ep040;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Spring JdbcClient: SQL pur, Ergebnis direkt in ein Record. */
@Component("ep040RevenueJdbc")
public class RevenueJdbc {

  private final JdbcClient jdbc;

  public RevenueJdbc(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<StatusRevenue> revenueByStatus() {
    return jdbc.sql("SELECT status, sum(total) AS revenue FROM orders"
        + " GROUP BY status").query(StatusRevenue.class).list();
  }
}
