package dev.krocon.shop.ep030;

import static dev.krocon.shop.ep030.LostUpdateTest.await;
import static dev.krocon.shop.ep030.StockFixture.PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Product;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Folge 030 – Lösung 3: PESSIMISTIC_WRITE sperrt die Zeile (FOR NO KEY UPDATE).
 * B wartet, bis A committet, und liest dann den neuen Stand: 10 - 3 + 5 = 12.
 */
@ShopJpaTest
@Import(StockService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PessimisticLockTest {

  @Autowired Ep030ProductRepository productRepository;
  @Autowired StockService stockService;
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
  void lockedRow_secondTransactionWaits() throws Exception {
    TransactionTemplate tx = new TransactionTemplate(txManager);
    CountDownLatch aLocked = new CountDownLatch(1);

    CompletableFuture<Void> adminA = CompletableFuture.runAsync(() ->
      tx.executeWithoutResult(s -> {
        Product p = productRepository.findForUpdate(PRODUCT_ID).orElseThrow();   // Sperre
        aLocked.countDown();
        sleep(500);                       // A hält die Sperre eine Weile
        p.setStock(p.getStock() - 3);
      }));
    CompletableFuture<Long> adminB = CompletableFuture.supplyAsync(() -> {
      await(aLocked);
      long start = System.nanoTime();
      stockService.changeStockLocked(PRODUCT_ID, +5);   // blockiert bis A committet
      return (System.nanoTime() - start) / 1_000_000;
    });

    adminA.get();
    long waitedMs = adminB.get();
    System.out.println("ep030 pessimistic: B wartete " + waitedMs + " ms");

    assertThat(waitedMs).isGreaterThan(300);
    assertThat(fixture.stock()).isEqualTo(12);   // kein Lost Update
  }

  static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
