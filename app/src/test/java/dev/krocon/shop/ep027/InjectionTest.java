package dev.krocon.shop.ep027;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** Folge 027 (Problem): String-Verkettung vs. gebundener Parameter. */
@ShopJpaTest
@Import(OrderQueries.class)
class InjectionTest {

  static final String ATTACK = "PAID' OR '1'='1";

  @Autowired OrderQueries queries;

  @Test
  void concatenation_returnsAllOrders() {
    List<Order> leaked = queries.byStatusUnsafe(ATTACK);
    System.out.println("Verkettet mit \"" + ATTACK + "\": " + leaked.size() + " Bestellungen");
    assertThat(leaked).hasSize(5000);          // WHERE status = 'PAID' OR '1'='1' → alles
  }

  @Test
  void boundParameter_isJustAValue() {
    assertThat(queries.byStatusNative(ATTACK)).isEmpty();       // Status heißt wirklich so → keiner
    assertThat(queries.byStatusNative("PAID")).hasSize(2000);
  }
}
