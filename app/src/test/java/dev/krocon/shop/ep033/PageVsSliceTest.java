package dev.krocon.shop.ep033;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.ep034.SqlRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

/** Folge 033: Page feuert zusätzlich COUNT, Slice nicht. */
@ShopJpaTest
@TestPropertySource(properties = SqlRecorder.PROPERTY)
class PageVsSliceTest {

  static final Pageable SECOND_PAGE = PageRequest.of(1, 20, Sort.by("createdAt", "id"));

  @Autowired Ep033OrderRepository orderRepository;

  @BeforeEach
  void clear() {
    SqlRecorder.clear();
  }

  @Test
  void page_runsCountQuery() {
    Page<Order> page = orderRepository.findByStatus(OrderStatus.NEW, SECOND_PAGE);

    System.out.println("ep033 page: " + SqlRecorder.statements());
    assertThat(SqlRecorder.statements()).hasSize(2);
    assertThat(SqlRecorder.statements().get(1)).isEqualTo("select count(*) from orders o1_0 where o1_0.status=?");
    assertThat(page.getTotalPages()).isEqualTo(50);
  }

  @Test
  void slice_noCount_fetchesOneMore() {
    Slice<Order> slice = orderRepository.findSliceByStatus(OrderStatus.NEW, SECOND_PAGE);

    System.out.println("ep033 slice: " + SqlRecorder.statements());
    assertThat(SqlRecorder.statements()).hasSize(1).noneMatch(s -> s.contains("count("));
    assertThat(SqlRecorder.statements().get(0)).endsWith("offset ? rows fetch first ? rows only");
    assertThat(slice.getContent()).hasSize(20);
    assertThat(slice.hasNext()).isTrue();   // weiß es durch die 21. Zeile
  }
}
