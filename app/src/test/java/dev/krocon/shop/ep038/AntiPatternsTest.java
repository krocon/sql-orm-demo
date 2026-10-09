package dev.krocon.shop.ep038;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ShopJpaTest
@Import(ReportRepository.class)
class AntiPatternsTest {
  @Autowired EntityManager em;
  @Autowired SessionFactory sessionFactory;
  @Autowired ReportRepository reports;
  Statistics stats;

  @BeforeEach
  void clear() {
    stats = sessionFactory.getStatistics();
    stats.clear();
  }

  // 1. Alles EAGER: jede Order zieht ihren Kunden mit – bei JPQL als Extra-Query pro Kunde
  @Test
  void eager_nPlusOne() {
    List<dev.krocon.shop.ep038.bad.Order> bad = em.createQuery(
        "select o from Ep038BadOrder o where o.id <= 10", dev.krocon.shop.ep038.bad.Order.class)
        .getResultList();
    long eagerStatements = stats.getPrepareStatementCount();
    System.out.println("EAGER: " + bad.size() + " Orders -> " + eagerStatements + " Statements");
    assertThat(eagerStatements).isEqualTo(1 + 10);   // 10 verschiedene Kunden

    em.clear();
    stats.clear();
    List<Order> good = em.createQuery("select o from Order o where o.id <= 10", Order.class)
        .getResultList();
    System.out.println("LAZY: " + good.size() + " Orders -> " + stats.getPrepareStatementCount() + " Statement");
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }

  // 2. Entity als API: Jackson folgt jeder Beziehung (Lazy Loading + Endlos-Rekursion)
  @Test
  void entityAsApi_vsDto() {
    JsonMapper json = JsonMapper.builder().build();
    Order order = em.find(Order.class, 1L);

    assertThatThrownBy(() -> json.writeValueAsString(order))
        .isInstanceOf(JacksonException.class);
    System.out.println("Statements beim Serialisieren der Entity: " + stats.getPrepareStatementCount());

    String dto = json.writeValueAsString(OrderDto.from(order));
    System.out.println("DTO: " + dto);
    assertThat(dto).startsWith("{\"id\":1,");
  }

  // 3. CascadeType.ALL auf @ManyToOne: remove(order) will auch den Kunden löschen
  @Test
  void cascadeAllOnManyToOne_deletesCustomer() {
    dev.krocon.shop.ep038.bad.Order bad = em.find(dev.krocon.shop.ep038.bad.Order.class, 1L);
    em.remove(bad);
    // Hibernate löscht Order UND Customer -> der Kunde hat weitere Bestellungen -> FK-Verletzung
    assertThatThrownBy(em::flush)
        .isInstanceOf(PersistenceException.class)
        .hasStackTraceContaining("customers");
  }

  @Test
  void cascadeOnlyParentToChild_customerStays() {
    Order good = em.find(Order.class, 1L);
    Long customerId = good.getCustomer().getId();
    em.remove(good);            // Cascade nur auf items (Aggregat)
    em.flush();
    em.clear();
    assertThat(em.find(Order.class, 1L)).isNull();
    assertThat(em.find(Customer.class, customerId)).isNotNull();
  }

  // 4. ORM für Reports: findAll lädt 5000 Entities, die Projektion liefert 4 Zeilen
  @Test
  void report_findAllVsProjection() {
    Map<OrderStatus, BigDecimal> inJava = reports.revenueByStatusFindAll();
    long loadedEntities = stats.getEntityLoadCount();
    System.out.println("findAll: " + loadedEntities + " Entities geladen");
    assertThat(loadedEntities).isGreaterThanOrEqualTo(5000);

    em.clear();
    stats.clear();
    List<StatusRevenue> projection = reports.revenueByStatus();
    System.out.println("Projektion: " + projection + ", Entities: " + stats.getEntityLoadCount());
    assertThat(stats.getEntityLoadCount()).isZero();
    assertThat(projection).hasSize(4);
    Map<OrderStatus, BigDecimal> inDb = projection.stream()
        .collect(Collectors.toMap(StatusRevenue::status, StatusRevenue::revenue));
    assertThat(inDb).isEqualTo(inJava);
  }
}
