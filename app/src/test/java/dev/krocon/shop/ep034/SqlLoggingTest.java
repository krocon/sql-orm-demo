package dev.krocon.shop.ep034;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Folge 034: SQL sichtbar machen. Profil "ep034" = src/test/resources/application-ep034.yaml
 * (SQL debug, Bind-Parameter trace, Session Metrics).
 */
@ShopJpaTest
@ActiveProfiles("ep034")
@ExtendWith(OutputCaptureExtension.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)   // Session soll im Test enden → Metrics im Log
class SqlLoggingTest {

  @Autowired EntityManager em;
  @Autowired PlatformTransactionManager txManager;

  @Test
  void sqlBindParametersAndSessionMetrics(CapturedOutput output) {
    new TransactionTemplate(txManager).executeWithoutResult(s ->
      em.createQuery("select o from Order o where o.id = :id", Order.class)
        .setParameter("id", 42L)
        .getSingleResult());

    String log = output.getOut();
    // 1) SQL
    assertThat(log).contains("org.hibernate.SQL", "from orders o1_0 where o1_0.id=?");
    // 2) Bind-Parameter – Logger org.hibernate.orm.jdbc.bind
    assertThat(log).contains("org.hibernate.orm.jdbc.bind", "binding parameter (1:BIGINT) <- [42]");
    // 3) Session Metrics – Logger org.hibernate.session.metrics (DEBUG)
    assertThat(log).contains("HHH000401: Logging session metrics", "executing 1 JDBC statements");
  }
}
