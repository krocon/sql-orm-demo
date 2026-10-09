package dev.krocon.shop.ep030;

import static dev.krocon.shop.ep030.LostUpdateTest.arriveAndAwait;
import static dev.krocon.shop.ep030.LostUpdateTest.await;
import static dev.krocon.shop.ep030.StockFixture.PRODUCT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Product;
import dev.krocon.shop.ep034.SqlRecorder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Folge 030 – Lösung 1+2: Product hat @Version. Dasselbe Szenario wie im LostUpdateTest,
 * aber B bekommt beim Speichern eine ObjectOptimisticLockingFailureException → 409 oder Retry.
 */
@ShopJpaTest
@Import(StockService.class)
@TestPropertySource(properties = SqlRecorder.PROPERTY)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OptimisticLockTest {

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
  void withVersion_secondWriterGetsConflict() throws Exception {
    TransactionTemplate tx = new TransactionTemplate(txManager);
    CountDownLatch bothLoaded = new CountDownLatch(2);
    CountDownLatch aSaved = new CountDownLatch(1);
    long versionBefore = fixture.version();
    SqlRecorder.clear();

    CompletableFuture<Void> adminA = CompletableFuture.runAsync(() -> {
      tx.executeWithoutResult(s -> {
        Product p = productRepository.findById(PRODUCT_ID).orElseThrow();
        arriveAndAwait(bothLoaded);
        p.setStock(p.getStock() - 3);
      });
      aSaved.countDown();
    });
    CompletableFuture<Void> adminB = CompletableFuture.runAsync(() ->
      tx.executeWithoutResult(s -> {
        Product p = productRepository.findById(PRODUCT_ID).orElseThrow();
        arriveAndAwait(bothLoaded);
        await(aSaved);
        p.setStock(p.getStock() + 5);   // UPDATE … WHERE id=? AND version=? → 0 Zeilen
      }));

    adminA.get();
    assertThatThrownBy(adminB::get)
      .isInstanceOf(ExecutionException.class)
      .hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);

    assertThat(fixture.stock()).isEqualTo(7);                     // A bleibt erhalten
    assertThat(fixture.version()).isEqualTo(versionBefore + 1);
    String update = SqlRecorder.startingWith("update").get(0);
    System.out.println("ep030 optimistic sql: " + update);
    assertThat(update).isEqualTo(
      "update products set name=?,price=?,sku=?,stock=?,version=? where id=? and version=?");

    // Konflikt behandeln: B versucht es mit frischen Daten noch einmal → 7 + 5
    assertThat(stockService.changeStockWithRetry(PRODUCT_ID, +5, 3)).isEqualTo(12);
    assertThat(fixture.stock()).isEqualTo(12);
  }
}
