package dev.krocon.shop.ep033;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.ep034.QueryCount;
import dev.krocon.shop.ep034.SqlRecorder;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

/**
 * Folge 033: JOIN FETCH auf Collections + Pagination.
 * Gemessen mit Hibernate 7.4.1 + PostgreSQL 18:
 *  - Die Video-Query wird per Unterabfrage korrekt in der DB paginiert (keine Warnung).
 *  - Sobald das nicht geht (z. B. ORDER BY auf die Collection), kommt HHH90003004
 *    und Hibernate lädt ALLES und paginiert im Speicher.
 *  - Portabel und immer sicher: erst IDs paginieren, dann Items nachladen.
 */
@ShopJpaTest
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = SqlRecorder.PROPERTY)
class FetchPaginationTest {

  static final Pageable FIRST_20 = PageRequest.of(0, 20, Sort.by("id"));

  @Autowired Ep033OrderRepository orderRepository;
  @Autowired EntityManager em;

  @Test
  void videoQuery_hibernate74PaginatesInSubquery(CapturedOutput output) {
    QueryCount queries = QueryCount.of(em);
    SqlRecorder.clear();

    List<Order> page = queries.measure(() -> orderRepository.findWithItems(FIRST_20)).result();

    String sql = SqlRecorder.statements().get(0);
    System.out.println("ep033 join fetch + page (video query): " + sql);
    assertThat(page).hasSize(20);
    assertThat(output.getOut()).doesNotContain("HHH90003004");
    assertThat(sql).contains("from (select ").contains("fetch first ? rows only) o1_0");
    assertThat(queries.statistics().getEntityLoadCount()).isLessThan(100);   // 20 Orders + ihre Items
  }

  @Test
  void orderByCollection_paginatesInMemory(CapturedOutput output) {
    QueryCount queries = QueryCount.of(em);
    SqlRecorder.clear();

    List<Order> page = queries.measure(() -> orderRepository.findWithItemsByQuantity(PageRequest.of(0, 20))).result();

    String sql = SqlRecorder.statements().get(0);
    long loaded = queries.statistics().getEntityLoadCount();
    System.out.println("ep033 in memory: " + loaded + " Entities für 20 Orders, SQL: " + sql);
    assertThat(page).hasSize(20);
    assertThat(output.getOut()).contains("HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory");
    assertThat(sql).doesNotContain("fetch first").doesNotContain("offset");   // kein LIMIT in der DB
    assertThat(loaded).isEqualTo(5000 + 10000);                               // alle Orders + alle Items
  }

  @Test
  void idsFirst_thenFetchItems(CapturedOutput output) {
    QueryCount queries = QueryCount.of(em);
    SqlRecorder.clear();

    List<Order> page = queries.measure(() -> {
      List<Long> ids = orderRepository.findIds(FIRST_20);   // LIMIT in der DB
      return orderRepository.findWithItemsByIdIn(ids);       // dann JOIN FETCH … WHERE id IN
    }).result();

    assertThat(page).hasSize(20).allSatisfy(o -> assertThat(o.getItems()).isNotEmpty());
    assertThat(output.getOut()).doesNotContain("HHH90003004");
    assertThat(SqlRecorder.statements()).hasSize(2);
    assertThat(SqlRecorder.statements().get(0)).isEqualTo("select o1_0.id from orders o1_0 order by o1_0.id fetch first ? rows only");
    System.out.println("ep033 ids-first: " + queries.statistics().getEntityLoadCount() + " Entities, " + SqlRecorder.statements());
  }
}
