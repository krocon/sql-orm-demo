package dev.krocon.shop;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Echte PostgreSQL für alle Tests (Folge 039): Testcontainers startet postgres:18.
 * Ist die Umgebungsvariable DB_URL gesetzt (z. B. die Compose-Datenbank), wird diese genutzt
 * und kein Container gestartet.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  @Conditional(NoExternalDb.class)
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer("postgres:18");
  }

  static class NoExternalDb implements Condition {
    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
      return System.getenv("DB_URL") == null;
    }
  }
}
