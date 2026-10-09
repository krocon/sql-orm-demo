package dev.krocon.shop.ep022;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.hibernate.SpringImplicitNamingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;

/** Folge 022 (Problem): @OneToMany ohne mappedBy → Hibernate erwartet eine Join-Tabelle. */
@ShopJpaTest
class UnidirectionalJoinTableTest {

  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;

  @Test
  void schemaLogShowsExtraJoinTable() throws Exception {
    // Schema so generieren, wie Hibernate es für diese Mappings erwartet (ohne DB-Zugriff)
    Path out = Files.createTempFile("ep022-schema", ".sql");
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
        .applySetting("jakarta.persistence.schema-generation.scripts.action", "create")
        .applySetting("jakarta.persistence.schema-generation.scripts.create-target", out.toString())
        .applySetting("hibernate.format_sql", "false")
        .build();
    Metadata metadata = new MetadataSources(registry)
        .addAnnotatedClass(UnidirectionalOrder.class)
        .addAnnotatedClass(UnidirectionalItem.class)
        .getMetadataBuilder()
        .applyImplicitNamingStrategy(new SpringImplicitNamingStrategy())        // wie Spring Boot
        .applyPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy())
        .build();
    metadata.buildSessionFactory().close();   // schreibt das DDL-Skript (JPA schema-generation)
    StandardServiceRegistryBuilder.destroy(registry);

    String ddl = Files.readString(out);
    System.out.println("Erwartetes Schema:\n" + ddl);
    assertThat(ddl).contains("create table ep022_uni_orders_items");
  }

  @Test
  void insertsGoIntoJoinTable() {
    UnidirectionalOrder order = new UnidirectionalOrder("unidirektional");
    order.getItems().add(new UnidirectionalItem("MUG-1"));
    order.getItems().add(new UnidirectionalItem("SKU-002"));
    em.persist(order);
    em.flush();                          // SQL-Log: 1 + 2 INSERTs + 2 INSERTs in die Join-Tabelle

    Integer links = jdbc.queryForObject(
        "select count(*) from ep022_uni_orders_items where ep022_uni_order_id = ?", Integer.class, order.getId());
    assertThat(links).isEqualTo(2);
  }
}
