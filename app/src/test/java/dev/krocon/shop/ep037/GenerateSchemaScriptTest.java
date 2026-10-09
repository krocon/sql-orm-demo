package dev.krocon.shop.ep037;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * „Generieren höchstens als Startpunkt“: Hibernate schreibt das DDL aller Entities in eine Datei.
 * Die Datenbank selbst bleibt unberührt (ddl-auto bleibt validate). Ergebnis: prüfen, anpassen,
 * als Migration einchecken.
 */
@ShopJpaTest
@TestPropertySource(properties = {
    "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create",
    "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=target/ep037/create.sql",
    "spring.jpa.properties.hibernate.hbm2ddl.delimiter=;",
    "spring.jpa.properties.hibernate.format_sql=true"
})
class GenerateSchemaScriptTest {
  static final Path SCRIPT = Path.of("target/ep037/create.sql");

  @BeforeAll
  static void prepare() throws Exception {
    Files.createDirectories(SCRIPT.getParent());
    Files.deleteIfExists(SCRIPT);   // Hibernate hängt sonst an eine vorhandene Datei an
  }

  @Test
  void writesCreateScript() throws Exception {
    String sql = Files.readString(SCRIPT);
    System.out.println(sql);
    assertThat(sql).contains("create table ep037_customers", "full_name varchar(255)");
    // Typen weichen von der Migration ab (varchar(255) statt text) → deshalb prüfen statt blind übernehmen
    assertThat(sql).contains("create table orders");
  }
}
