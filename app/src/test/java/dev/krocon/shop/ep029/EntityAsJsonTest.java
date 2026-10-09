package dev.krocon.shop.ep029;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.ep034.QueryCount;
import dev.krocon.shop.ep034.SqlRecorder;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Folge 029: Entity als JSON → Endlosschleife / Lazy-Fehler / Mass Assignment.
 * Lösung: Record-DTO + select-new-Projektion.
 * Statt MockMvc serialisieren wir direkt mit Jacksons JsonMapper (macht Spring MVC genauso).
 */
@ShopJpaTest
@TestPropertySource(properties = SqlRecorder.PROPERTY)
class EntityAsJsonTest {

  static final long ORDER_ID = 1L;      // Bestellung 1 gehört Kunde 1 + 7919 % 999 = 927
  static final long CUSTOMER_ID = 927L;

  @Autowired Ep029OrderRepository orderRepository;
  @Autowired EntityManager em;

  final JsonMapper jsonMapper = JsonMapper.builder().build();
  BadOrderController badController;
  OrderController goodController;

  @BeforeEach
  void setUp() {
    badController = new BadOrderController(orderRepository);
    goodController = new OrderController(orderRepository, em);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)   // wie mit open-in-view: false
  void entityAsJson_outsideTransaction_lazyError() {
    Order order = badController.get(ORDER_ID);   // Transaktion ist schon zu

    assertThatThrownBy(() -> jsonMapper.writeValueAsString(order))
      .isInstanceOf(JacksonException.class)
      .hasMessageContaining("Could not initialize proxy")
      .hasRootCauseInstanceOf(LazyInitializationException.class)
      .satisfies(e -> System.out.println("ep029 lazy: " + e.getMessage().lines().findFirst().orElse("")));
  }

  @Test
  void entityAsJson_insideTransaction_endlessLoop() {
    Order order = badController.get(ORDER_ID);   // Session offen → Lazy-Laden klappt …

    // … und Jackson läuft customer → orders → customer → orders → …
    assertThatThrownBy(() -> jsonMapper.writeValueAsString(order))
      .isInstanceOf(JacksonException.class)
      .hasMessageContaining("nesting depth")
      .hasMessageContaining("Order[\"customer\"]->dev.krocon.shop.domain.Customer$HibernateProxy[\"orders\"]")
      .satisfies(e -> System.out.println("ep029 loop: " + e.getMessage().substring(0, 260)));
  }

  @Test
  void entityAsRequestBody_massAssignment() {
    // Der Client schickt einfach eine eigene Summe mit
    String json = """
      {"status": "PAID", "total": 0.01}""";
    Order fromClient = jsonMapper.readValue(json, Order.class);

    Order saved = badController.update(ORDER_ID, fromClient);

    assertThat(saved.getStatus()).isEqualTo(OrderStatus.PAID);
    assertThat(saved.getTotal()).isEqualByComparingTo("0.01");   // Summe vom Client übernommen!
  }

  @Test
  void dtoAsJson_onlyThreeFields() {
    OrderDto dto = goodController.get(ORDER_ID);

    String json = jsonMapper.writeValueAsString(dto);
    System.out.println("ep029 dto json: " + json);
    assertThat(json).startsWith("{\"id\":1,\"status\":").contains("\"total\":");
    assertThat(jsonMapper.readTree(json).propertyNames()).containsExactly("id", "status", "total");
  }

  @Test
  void createRequest_serverComputesTotal() {
    // "total" im JSON gibt es im Request-Record gar nicht → wird ignoriert
    String json = """
      {"customerId": 927, "items": [{"productId": 1, "quantity": 2}], "total": 0.01}""";
    CreateOrderRequest request = jsonMapper.readValue(json, CreateOrderRequest.class);

    OrderDto created = goodController.create(request);

    BigDecimal mugPrice = em.find(dev.krocon.shop.domain.Product.class, 1L).getPrice();
    assertThat(created.status()).isEqualTo(OrderStatus.NEW);
    assertThat(created.total()).isEqualByComparingTo(mugPrice.multiply(BigDecimal.valueOf(2)));
  }

  @Test
  void selectNew_loadsOnlyThreeColumns_noEntities() {
    QueryCount queries = QueryCount.of(em);
    SqlRecorder.clear();

    QueryCount.Counted<List<OrderDto>> counted =
      queries.measure(() -> orderRepository.findDtosByCustomer(CUSTOMER_ID));

    String sql = SqlRecorder.statements().get(0);
    System.out.println("ep029 projection sql: " + sql);
    assertThat(sql).startsWith("select o1_0.id,o1_0.status,o1_0.total from orders o1_0");
    assertThat(counted.statements()).isEqualTo(1);
    assertThat(counted.result()).isNotEmpty().allSatisfy(d -> assertThat(d).isInstanceOf(OrderDto.class));
    assertThat(queries.statistics().getEntityLoadCount()).isZero();   // nichts im Persistence Context
  }
}
