package dev.krocon.shop.ep030;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Product;
import dev.krocon.shop.ep034.SqlRecorder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/** Folge 030: Welches SQL erzeugt Hibernate 7.4 für die Lock-Modi auf PostgreSQL? */
@ShopJpaTest
@TestPropertySource(properties = SqlRecorder.PROPERTY)
class PessimisticLockSqlTest {

  @Autowired EntityManager em;
  @Autowired Ep030ProductRepository productRepository;

  @BeforeEach
  void clear() {
    SqlRecorder.clear();
  }

  @Test
  void pessimisticWrite_isForNoKeyUpdate() {
    productRepository.findForUpdate(1L);
    String sql = SqlRecorder.statements().get(0);
    System.out.println("ep030 lock sql: " + sql);
    assertThat(sql).endsWith("where p1_0.id=? for no key update of p1_0");   // nicht FOR UPDATE
  }

  @Test
  void entityManagerFind_withLockModes() {
    em.find(Product.class, 1L, LockModeType.PESSIMISTIC_WRITE);
    em.find(Product.class, 2L, LockModeType.PESSIMISTIC_READ);
    assertThat(SqlRecorder.statements().get(0)).endsWith("for no key update of p1_0");
    assertThat(SqlRecorder.statements().get(1)).endsWith("for share of p1_0");
  }
}
