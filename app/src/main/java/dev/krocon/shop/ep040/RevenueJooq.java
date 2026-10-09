package dev.krocon.shop.ep040;

import static dev.krocon.shop.ep040.jooq.Orders.ORDERS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.table;

import java.math.BigDecimal;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/** jOOQ: SQL als typsicheres DSL. DSLContext kommt aus spring-boot-starter-jooq. */
@Component("ep040RevenueJooq")
public class RevenueJooq {

  private final DSLContext dsl;

  public RevenueJooq(DSLContext dsl) {
    this.dsl = dsl;
  }

  /** Wie im Video – mit (hier nachgebauten) generierten Klassen. */
  public List<StatusRevenue> revenueByStatus() {
    return dsl.select(ORDERS.STATUS, sum(ORDERS.TOTAL).as("revenue"))
        .from(ORDERS).groupBy(ORDERS.STATUS)
        .fetchInto(StatusRevenue.class);
  }

  /** Ohne Codegen: Tabellen und Spalten per Name – geht, ist aber nicht mehr typsicher. */
  public List<StatusRevenue> revenueByStatusWithoutCodegen() {
    var status = field(name("status"), String.class);
    var total = field(name("total"), BigDecimal.class);
    return dsl.select(status, sum(total).as("revenue"))
        .from(table(name("orders"))).groupBy(status)
        .fetchInto(StatusRevenue.class);
  }

  /** Window Function als Bonus: Anteil je Status am Gesamtumsatz (SQL: sum(...) over ()). */
  public String revenueShareSql() {
    var revenue = sum(ORDERS.TOTAL);
    return dsl.select(ORDERS.STATUS, revenue.as("revenue"),
            revenue.div(sum(revenue).over()).as("share"))
        .from(ORDERS).groupBy(ORDERS.STATUS).getSQL();
  }
}
