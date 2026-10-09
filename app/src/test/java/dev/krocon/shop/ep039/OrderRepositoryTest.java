package dev.krocon.shop.ep039;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.repo.CustomerRepository;
import dev.krocon.shop.repo.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Genau wie im Video: eigener Container pro Testklasse. Ohne Docker wird der Test übersprungen. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OrderRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18");  // Flyway läuft mit

  @Autowired OrderRepository orders;
  @Autowired CustomerRepository customers;

  @Test
  void flywayMigratedRealPostgres() {
    assertThat(orders.count()).isEqualTo(5000);       // V2__seed.sql ist gelaufen
    assertThat(customers.count()).isEqualTo(1000);
  }

  @Test
  void runsOnPostgres18() {
    assertThat(postgres.getDockerImageName()).isEqualTo("postgres:18");
  }
}
