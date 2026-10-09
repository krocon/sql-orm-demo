package dev.krocon.shop.ep039;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Läuft gegen echtes PostgreSQL (Testcontainers oder DB_URL). Alles hier ist PostgreSQL-spezifisch:
 * Auf H2 scheitert schon die Migration V380 (jsonb, Partial Index, GIN), ebenso die Abfragen.
 */
@ShopJpaTest
class H2VsPostgresTest {
  @Autowired JdbcClient jdbc;

  @Test
  void jsonbMigrationAndOperators() {
    jdbc.sql("INSERT INTO ep039_order_events (order_id, payload) VALUES"
        + " (1, '{\"type\": \"PAID\", \"amount\": 19.90}'),"
        + " (2, '{\"type\": \"SHIPPED\", \"carrier\": \"DHL\"}')").update();

    // ->> liest ein Feld als Text, @> prüft „enthält“ (nutzt den GIN-Index)
    List<String> carriers = jdbc.sql(
        "SELECT payload ->> 'carrier' FROM ep039_order_events WHERE payload @> '{\"type\": \"SHIPPED\"}'")
        .query(String.class).list();
    assertThat(carriers).containsExactly("DHL");
  }

  @Test
  void partialIndexExists() {
    String def = jdbc.sql("SELECT indexdef FROM pg_indexes WHERE indexname = 'ep039_order_events_open_idx'")
        .query(String.class).single();
    assertThat(def).contains("WHERE (NOT processed)");
  }

  @Test
  void upsertWithOnConflictDoUpdate() {
    String upsert = "INSERT INTO ep039_daily_revenue (day, revenue) VALUES (DATE '2026-01-01', ?)"
        + " ON CONFLICT (day) DO UPDATE SET revenue = ep039_daily_revenue.revenue + EXCLUDED.revenue";
    jdbc.sql(upsert).param(new BigDecimal("10.00")).update();
    jdbc.sql(upsert).param(new BigDecimal("5.50")).update();

    BigDecimal revenue = jdbc.sql("SELECT revenue FROM ep039_daily_revenue WHERE day = DATE '2026-01-01'")
        .query(BigDecimal.class).single();
    assertThat(revenue).isEqualByComparingTo("15.50");
  }

  @Test
  void transactionalDdl() {
    // PostgreSQL: DDL ist transaktional – der Test-Rollback nimmt auch CREATE TABLE zurück
    jdbc.sql("CREATE TABLE ep039_tmp (id int)").update();
    assertThat(jdbc.sql("SELECT count(*) FROM pg_tables WHERE tablename = 'ep039_tmp'")
        .query(Long.class).single()).isEqualTo(1);
  }
}
