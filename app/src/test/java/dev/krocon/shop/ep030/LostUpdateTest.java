package dev.krocon.shop.ep030;

import static dev.krocon.shop.ep030.StockFixture.PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.ep034.SqlRecorder;
import jakarta.persistence.EntityManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Folge 030 – Problem: Admin A und B laden Produkt 7 gleichzeitig (stock = 10).
 * A zieht 3 ab und speichert 7, B hat noch 10 gesehen, bucht 5 dazu und speichert 15.
 * Ohne @Version gewinnt der Letzte: Lost Update.
 */
@ShopJpaTest
@TestPropertySource(properties = SqlRecorder.PROPERTY)
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // echte Commits
class LostUpdateTest {

  @Autowired EntityManager em;
  @Autowired PlatformTransactionManager txManager;
  @Autowired DataSource dataSource;
  StockFixture fixture;

  @BeforeEach
  void setUp() {
    fixture = new StockFixture(dataSource);
    fixture.setStock(10);
  }

  @AfterEach
  void tearDown() {
    fixture.restore();
  }

  @Test
  void withoutVersion_lastWriterWins() throws Exception {
    TransactionTemplate tx = new TransactionTemplate(txManager);
    CountDownLatch bothLoaded = new CountDownLatch(2);
    CountDownLatch aSaved = new CountDownLatch(1);
    SqlRecorder.clear();

    CompletableFuture<Void> adminA = CompletableFuture.runAsync(() -> {
      tx.executeWithoutResult(s -> {
        Ep030UnversionedProduct p = em.find(Ep030UnversionedProduct.class, PRODUCT_ID);   // sieht 10
        arriveAndAwait(bothLoaded);
        p.setStock(p.getStock() - 3);   // A speichert 7
      });
      aSaved.countDown();
    });
    CompletableFuture<Void> adminB = CompletableFuture.runAsync(() ->
      tx.executeWithoutResult(s -> {
        Ep030UnversionedProduct p = em.find(Ep030UnversionedProduct.class, PRODUCT_ID);   // sieht auch 10
        arriveAndAwait(bothLoaded);
        await(aSaved);
        p.setStock(p.getStock() + 5);   // B rechnet mit 10 und speichert 15
      }));
    CompletableFuture.allOf(adminA, adminB).get();

    // UPDATE products SET stock = 15 WHERE id = 7 – die Änderung von A ist weg
    assertThat(fixture.stock()).isEqualTo(15);   // richtig wäre 12
    assertThat(SqlRecorder.startingWith("update"))
      .containsExactly("update products set stock=? where id=?", "update products set stock=? where id=?");
  }

  /** Beide Admins haben geladen → weiter. */
  static void arriveAndAwait(CountDownLatch latch) {
    latch.countDown();
    await(latch);
  }

  static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
