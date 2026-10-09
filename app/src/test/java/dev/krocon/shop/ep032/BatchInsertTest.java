package dev.krocon.shop.ep032;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Folge 032: 10 000 Inserts mit hibernate.jdbc.batch_size = 50, in Chunks mit flush/clear.
 * Beweis über die "Session Metrics" (generate_statistics: true), die Hibernate beim Schließen loggt.
 */
@ShopJpaTest
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
  "spring.jpa.properties.hibernate.jdbc.batch_size=50",
  "spring.jpa.properties.hibernate.order_inserts=true",
  "logging.level.org.hibernate.SQL=info",            // 10 000 SQL-Zeilen im Log wären zu viel
  "logging.level.org.hibernate.session.metrics=debug" // Hibernate 7.4: Session Metrics auf DEBUG
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BatchInsertTest {

  static final int ROWS = 10_000;
  static final int CHUNK = 500;

  @Autowired EntityManager em;
  @Autowired PlatformTransactionManager txManager;
  @Autowired DataSource dataSource;

  @AfterEach
  void cleanUp() {
    JdbcClient.create(dataSource).sql("TRUNCATE ep032_batch_items, ep032_identity_items").update();
  }

  @Test
  void sequence_isBatched(CapturedOutput output) {
    Metrics m = insert(output, i -> new Ep032BatchItem("item " + i));
    System.out.println("ep032 SEQUENCE + batch_size 50: " + m);
    assertThat(m.batches()).isEqualTo(ROWS / 50);    // 200 JDBC-Batches à 50 INSERTs
    // einzeln nur noch ~200 × nextval (allocationSize 50); bei frischer Sequenz holt der
    // pooled-Optimizer einmal zusätzlich – daher 200 oder 201
    assertThat(m.executed()).isBetween(ROWS / 50L, ROWS / 50L + 1);
    assertThat(count("ep032_batch_items")).isEqualTo(ROWS);
  }

  @Test
  void identity_isNotBatched(CapturedOutput output) {
    Metrics m = insert(output, i -> new Ep032IdentityItem("item " + i));
    System.out.println("ep032 IDENTITY + batch_size 50: " + m);
    assertThat(m.batches()).isZero();                // trotz batch_size kein Insert-Batching
    assertThat(m.executed()).isEqualTo(ROWS);        // jede Zeile ein eigenes INSERT … RETURNING
    assertThat(count("ep032_identity_items")).isEqualTo(ROWS);
  }

  /** Schreibt ROWS Entities in einer Transaktion, alle CHUNK Zeilen flush + clear. */
  Metrics insert(CapturedOutput output, IntFunction<Object> factory) {
    int before = output.getOut().length();
    long start = System.nanoTime();
    new TransactionTemplate(txManager).executeWithoutResult(s -> {
      for (int i = 1; i <= ROWS; i++) {
        em.persist(factory.apply(i));
        if (i % CHUNK == 0) {
          em.flush();
          em.clear();   // Persistence Context bleibt klein
        }
      }
      assertThat(em.unwrap(Session.class).getStatistics().getEntityCount()).isZero();
    });
    long ms = (System.nanoTime() - start) / 1_000_000;
    String log = output.getOut().substring(before);              // Session Metrics der Transaktion
    return new Metrics(
      find(log, "preparing (\\d+) JDBC statements"),
      find(log, "executing (\\d+) JDBC statements"),
      find(log, "executing (\\d+) JDBC batches"), ms);
  }

  static long find(String log, String regex) {
    Matcher m = Pattern.compile(regex).matcher(log);
    long max = -1;   // die Insert-Session hat die größten Werte
    while (m.find()) {
      max = Math.max(max, Long.parseLong(m.group(1)));
    }
    return max;
  }

  long count(String table) {
    return JdbcClient.create(dataSource).sql("select count(*) from " + table).query(Long.class).single();
  }

  record Metrics(long prepared, long executed, long batches, long millis) { }
}
