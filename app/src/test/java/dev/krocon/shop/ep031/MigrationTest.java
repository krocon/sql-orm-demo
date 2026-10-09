package dev.krocon.shop.ep031;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Folge 031: Flyway-Migrationen gegen echtes PostgreSQL.
 * 1) Die App ist per Flyway migriert (inkl. V300/V301) und ddl-auto: validate ist zufrieden.
 * 2) Beispiel aus dem Video in eigenem Schema: V1, V2 → Checksumme → vorwärts korrigieren → Expand/Contract.
 */
@ShopJpaTest
class MigrationTest {

  static final String DEMO_SCHEMA = "ep031_demo";

  @Autowired DataSource dataSource;
  @Autowired EntityManager em;

  @Test
  void appSchema_comesFromFlyway_andValidates() {
    JdbcClient jdbc = JdbcClient.create(dataSource);
    List<String> versions = jdbc.sql("""
        select version from flyway_schema_history
        where success and version in ('300', '301') order by installed_rank""")
      .query(String.class).list();
    assertThat(versions).containsExactly("300", "301");

    // Kontext ist mit ddl-auto: validate gestartet → Mapping passt zur Migration
    List<Ep031CustomerContact> contacts = em
      .createQuery("select c from Ep031CustomerContact c order by c.customerId", Ep031CustomerContact.class)
      .getResultList();
    assertThat(contacts).extracting(Ep031CustomerContact::getPhoneE164)
      .containsExactly("+49401234567", "+4930987654", null);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void videoExample_checksum_forwardFix_expandContract(@TempDir Path dir) throws IOException {
    copyExampleMigrations(dir);
    // Erst nur V1 und V2 vorhanden (V2_1 kommt später als Korrektur dazu)
    Files.move(dir.resolve("V2_1__fix_status_default.sql"), dir.resolve("V2_1.later"));

    Flyway flyway = flyway(dir, "2");
    assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
    assertThat(columns()).containsExactly("id", "customer_id", "total", "created_at", "status");

    // Nie umschreiben: Wer V2 nachträglich ändert, bekommt beim nächsten Start einen Fehler
    Path v2 = dir.resolve("V2__add_order_status.sql");
    String original = Files.readString(v2);
    Files.writeString(v2, original.replace("varchar(20)", "varchar(30)"));
    assertThatThrownBy(() -> flyway(dir, "2").validate())
      .isInstanceOf(FlywayValidateException.class)
      .hasMessageContaining("checksum mismatch for migration version 2")
      .satisfies(e -> System.out.println("ep031 validate: " + e.getMessage().lines().limit(4).toList()));

    // Vorwärts korrigieren: V2 zurück im Originalzustand, Korrektur als neue Migration V2_1
    Files.writeString(v2, original);
    Files.move(dir.resolve("V2_1.later"), dir.resolve("V2_1__fix_status_default.sql"));
    assertThat(flyway(dir, "2.1").migrate().migrationsExecuted).isEqualTo(1);

    // Expand: V3 neue Spalte, V4 Daten kopieren → alte und neue Spalte existieren parallel
    assertThat(flyway(dir, "4").migrate().migrationsExecuted).isEqualTo(2);
    assertThat(columns()).contains("status", "state");
    assertThat(jdbc().sql("select count(*) from " + DEMO_SCHEMA + ".orders where state = status")
      .query(Integer.class).single()).isEqualTo(3);

    // Contract (nächstes Release): alte Spalte weg
    assertThat(flyway(dir, "5").migrate().migrationsExecuted).isEqualTo(1);
    assertThat(columns()).doesNotContain("status").contains("state");

    for (MigrationInfo info : flyway(dir, "5").info().applied()) {
      System.out.println("ep031 history: V" + info.getVersion() + " " + info.getDescription()
        + " checksum=" + info.getChecksum() + " " + info.getState());
    }
  }

  @AfterEach
  void dropDemoSchema() {
    jdbc().sql("DROP SCHEMA IF EXISTS " + DEMO_SCHEMA + " CASCADE").update();
  }

  Flyway flyway(Path dir, String target) {
    return Flyway.configure()
      .dataSource(dataSource)
      .schemas(DEMO_SCHEMA)                    // eigenes Schema inkl. eigener flyway_schema_history
      .locations("filesystem:" + dir)
      .target(target)
      .load();
  }

  List<String> columns() {
    return jdbc().sql("""
        select column_name from information_schema.columns
        where table_schema = ? and table_name = 'orders' order by ordinal_position""")
      .param(DEMO_SCHEMA).query(String.class).list();
  }

  JdbcClient jdbc() {
    return JdbcClient.create(dataSource);
  }

  static void copyExampleMigrations(Path dir) throws IOException {
    Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath:ep031/flyway/*.sql");
    for (Resource r : files) {
      try (var in = r.getInputStream()) {
        Files.copy(in, dir.resolve(r.getFilename()));
      }
    }
  }
}
