package dev.krocon.shop.ep033;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.ep034.QueryCount;
import dev.krocon.shop.ep034.SqlRecorder;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

/** Folge 033: findAll lädt alles, Pageable nur eine Seite. */
@ShopJpaTest
@TestPropertySource(properties = SqlRecorder.PROPERTY)
class FindAllTest {

  @Autowired Ep033OrderRepository orderRepository;
  @Autowired EntityManager em;

  @Test
  void findAll_loadsEveryRow() {
    QueryCount queries = QueryCount.of(em);
    queries.count(() -> {
      List<Order> all = orderRepository.findAll();   // alle Zeilen
      assertThat(all).hasSize(5000);
    });
    assertThat(queries.statistics().getEntityLoadCount()).isEqualTo(5000);   // der Admin sieht 20
  }

  @Test
  void pageable_loadsOnePage_withIdAsTiebreaker() {
    QueryCount queries = QueryCount.of(em);
    SqlRecorder.clear();

    Page<Order> page = queries.measure(() -> orderRepository.findByStatus(OrderStatus.NEW,
        PageRequest.of(0, 20, Sort.by("createdAt", "id")))).result();

    assertThat(page.getContent()).hasSize(20);
    assertThat(page.getTotalElements()).isEqualTo(1000);
    assertThat(queries.statistics().getEntityLoadCount()).isEqualTo(20);
    String sql = SqlRecorder.statements().get(0);
    System.out.println("ep033 page sql: " + sql);
    assertThat(sql).endsWith("order by o1_0.created_at,o1_0.id fetch first ? rows only");   // Seite 0: kein OFFSET
  }
}
