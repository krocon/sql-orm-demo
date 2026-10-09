package dev.krocon.shop.ep030;

import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Setzt Produkt 7 auf Lagerbestand 10 und stellt danach den Originalzustand wieder her. */
class StockFixture {

  static final long PRODUCT_ID = 7L;

  private final JdbcClient jdbc;
  private int originalStock;
  private long originalVersion;

  StockFixture(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  void setStock(int stock) {
    originalStock = jdbc.sql("select stock from products where id = ?").param(PRODUCT_ID).query(Integer.class).single();
    originalVersion = jdbc.sql("select version from products where id = ?").param(PRODUCT_ID).query(Long.class).single();
    jdbc.sql("update products set stock = ? where id = ?").params(stock, PRODUCT_ID).update();
  }

  int stock() {
    return jdbc.sql("select stock from products where id = ?").param(PRODUCT_ID).query(Integer.class).single();
  }

  long version() {
    return jdbc.sql("select version from products where id = ?").param(PRODUCT_ID).query(Long.class).single();
  }

  void restore() {
    jdbc.sql("update products set stock = ?, version = ? where id = ?")
      .params(originalStock, originalVersion, PRODUCT_ID).update();
  }
}
