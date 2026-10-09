package dev.krocon.shop.ep034;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Folge 034: Batch Size als Notbremse. Derselbe naive Code, aber Hibernate lädt
 * die Kunden in Blöcken à 100 (… where id = any(?)) statt einzeln.
 */
@ShopJpaTest
@Import(OrderService.class)
@TestPropertySource(properties = {
  "spring.jpa.properties.hibernate.default_batch_fetch_size=100",
  "logging.level.org.hibernate.SQL=info"
})
class BatchFetchTest {

  @Autowired OrderService orderService;
  @Autowired EntityManager em;

  @Test
  void batchFetch_turnsNPlusOneIntoFewQueries() {
    long statements = QueryCount.of(em).count(() -> orderService.pageNaive());
    System.out.println("ep034 batch fetch: " + statements + " Statements");
    assertThat(statements).isLessThanOrEqualTo(1 + 10);   // 999 Kunden / 100 pro Batch
  }
}
