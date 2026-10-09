package dev.krocon.shop.ep019;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.OrderItem;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Version;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@ShopJpaTest
@Import(UnwrapSession.class)
class JpaVsHibernateTest {

  @Autowired EntityManager em;
  @Autowired UnwrapSession unwrapSession;
  Statistics stats;

  @BeforeEach
  void setUp() {
    stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
  }

  @Test
  void jpaIsTheApiHibernateTheImplementation() {
    // Spezifikation: jakarta.persistence-api (Jar-Name zeigt die Version)
    String apiJar = EntityManager.class.getProtectionDomain().getCodeSource().getLocation().toString();
    // Implementierung: Hibernate ORM
    System.out.println("Jakarta Persistence API: " + apiJar.substring(apiJar.lastIndexOf('/') + 1));
    System.out.println("Hibernate ORM:           " + Version.getVersionString());
    System.out.println("EntityManager dahinter:  " + em.unwrap(SessionImplementor.class).getClass().getName());

    assertThat(apiJar).contains("jakarta.persistence-api-3.2");
    assertThat(Version.getVersionString()).startsWith("7.4.");
    assertThat(em.unwrap(Session.class)).isInstanceOf(Session.class);
  }

  @Test
  void javaxIsGoneSinceJakartaEe9() {
    // alter Import aus javax.* existiert im Klassenpfad nicht mehr → Kompilierfehler im Code
    assertThatThrownBy(() -> Class.forName("javax.persistence.Entity"))
        .isInstanceOf(ClassNotFoundException.class);
  }

  @Test
  void domainUsesOnlyJakartaPersistence() throws IOException {
    // Arbeitsverzeichnis beim Maven-Lauf ist app/
    Path domain = Path.of("src/main/java/dev/krocon/shop/domain");
    try (Stream<Path> files = Files.list(domain)) {
      for (Path f : files.toList()) {
        String src = Files.readString(f);
        assertThat(src).as(f.toString()).doesNotContain("javax.persistence").doesNotContain("org.hibernate");
      }
    }
    // Wo kommen Hibernate-Extras dazu? Ausgabe aller Dateien mit org.hibernate-Import
    try (Stream<Path> all = Files.walk(Path.of("src/main/java"))) {
      List<Path> withHibernate = all.filter(p -> p.toString().endsWith(".java"))
          .filter(p -> read(p).contains("import org.hibernate")).toList();
      withHibernate.forEach(p -> System.out.println("Hibernate-Extra in: " + p));
      assertThat(withHibernate).contains(Path.of("src/main/java/dev/krocon/shop/ep019/Ep019Order.java"),
          Path.of("src/main/java/dev/krocon/shop/ep019/UnwrapSession.java"));
    }
  }

  @Test
  void batchSizeLoadsLazyProxiesInGroups() {
    // ohne Extra: domain.Order hat kein @BatchSize → jede Lazy-Order einzeln
    List<OrderItem> plain = em.createQuery("select i from OrderItem i where i.id <= 40 order by i.id", OrderItem.class)
        .getResultList();
    stats.clear();
    plain.forEach(i -> i.getOrder().getTotal());
    long withoutBatchSize = stats.getPrepareStatementCount();
    em.clear();

    // mit @BatchSize(size = 20) auf Ep019Order → bis zu 20 Orders pro SELECT
    List<Ep019OrderItem> items = em.createQuery("select i from Ep019OrderItem i where i.id <= 40 order by i.id",
        Ep019OrderItem.class).getResultList();
    stats.clear();
    items.forEach(i -> i.getOrder().getTotal());
    long withBatchSize = stats.getPrepareStatementCount();

    System.out.println("Lazy-Orders laden: ohne @BatchSize " + withoutBatchSize + " Queries, mit @BatchSize " + withBatchSize);
    assertThat(withoutBatchSize).isEqualTo(40);   // 40 Positionen aus 40 verschiedenen Orders
    assertThat(withBatchSize).isEqualTo(2);      // 2 × "where id = any (?)" mit je 20 IDs
  }

  @Test
  void sessionOnlyInOnePlace() {
    stats.clear();
    List<Ep019Order> orders = unwrapSession.loadOrders(List.of(1L, 2L, 3L));
    assertThat(orders).extracting(Ep019Order::getId).containsExactly(1L, 2L, 3L);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }

  private static String read(Path p) {
    try {
      return Files.readString(p);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
