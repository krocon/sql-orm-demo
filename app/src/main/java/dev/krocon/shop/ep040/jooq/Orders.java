package dev.krocon.shop.ep040.jooq;

import java.math.BigDecimal;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

/**
 * Handgeschriebener Ausschnitt dessen, was der jOOQ-Codegenerator aus der Tabelle orders erzeugt
 * (dort: Tables.ORDERS, OrdersRecord, alle Spalten). Siehe episodes/040/README.md → Codegen.
 */
public class Orders extends TableImpl<Record> {

  public static final Orders ORDERS = new Orders();

  public final TableField<Record, Long> ID =
      createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "");
  public final TableField<Record, String> STATUS =
      createField(DSL.name("status"), SQLDataType.CLOB.nullable(false), this, "");
  public final TableField<Record, BigDecimal> TOTAL =
      createField(DSL.name("total"), SQLDataType.NUMERIC(10, 2).nullable(false), this, "");

  private Orders() {
    super(DSL.name("orders"));
  }
}
