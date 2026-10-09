package dev.krocon.shop.ep030;

import static dev.krocon.shop.ep030.StockFixture.PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.ep034.SqlRecorder;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Folge 030 – Lösung 4: atomar updaten, ohne vorheriges Lesen. */
@ShopJpaTest
@Import(StockService.class)
@TestPropertySource(properties = SqlRecorder.PROPERTY)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AtomicUpdateTest {

  @Autowired StockService stockService;
  @Autowired DataSource dataSource;
  StockFixture fixture;

  @BeforeEach
  void setUp() {
    fixture = new StockFixture(dataSource);
    fixture.setStock(1);   // nur noch eine Tasse da
  }

  @AfterEach
  void tearDown() {
    fixture.restore();
  }

  @Test
  void decrement_onlyWhileStockAboveZero() {
    SqlRecorder.clear();

    assertThat(stockService.reserveOne(PRODUCT_ID)).isTrue();    // 1 Zeile geändert
    assertThat(stockService.reserveOne(PRODUCT_ID)).isFalse();   // 0 Zeilen = ausverkauft

    assertThat(fixture.stock()).isZero();                         // nie negativ
    assertThat(SqlRecorder.statements()).allMatch(s -> s.startsWith("update"));   // kein SELECT davor
    System.out.println("ep030 atomic sql: " + SqlRecorder.statements().get(0));
  }
}
