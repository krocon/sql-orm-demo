package dev.krocon.shop.ep034;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/** Folge 034: Queries im Test zählen – ein Assert fängt N+1, bevor es live geht. */
@ShopJpaTest
@Import(OrderService.class)
@TestPropertySource(properties = "logging.level.org.hibernate.SQL=info")   // 1000 SQL-Zeilen sparen
class QueryCountTest {

  @Autowired OrderService orderService;
  @Autowired EntityManager em;

  @Test
  void naive_isNPlusOne_andTheAssertCatchesIt() {
    QueryCount queries = QueryCount.of(em);

    long statements = queries.count(() -> orderService.pageNaive());
    System.out.println("ep034 naive: " + statements + " Statements für 5000 Orders");
    assertThat(statements).isEqualTo(1 + 999);   // 1 × orders + 999 × customers

    // So sieht der Test aus, der im CI rot wird (frischer Persistence Context):
    em.clear();
    assertThatThrownBy(() -> queries.assertStatements(1, () -> orderService.pageNaive()))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("N+1");
  }

  @Test
  void joinFetch_oneStatement() {
    QueryCount.of(em).assertStatements(1, () -> assertThat(orderService.pageJoinFetch()).hasSize(5000));
  }

  @Test
  void entityGraph_oneStatement() {
    QueryCount.of(em).assertStatements(1, () -> assertThat(orderService.pageEntityGraph()).hasSize(5000));
  }

  @Test
  void projection_oneStatement_noEntities() {
    QueryCount queries = QueryCount.of(em);
    queries.assertStatements(1, () -> assertThat(orderService.pageProjection()).hasSize(5000));
    assertThat(queries.statistics().getEntityLoadCount()).isZero();
  }
}
