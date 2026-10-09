package dev.krocon.shop.ep035;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.krocon.shop.TestcontainersConfiguration;
import dev.krocon.shop.domain.Order;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** OSIV aus (application.yaml: open-in-view: false): Fehlendes fällt sofort auf. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OsivOffTest {
  @Autowired MockMvc mvc;
  @Autowired OrderService service;
  @Autowired SessionFactory sessionFactory;
  Statistics stats;

  @BeforeEach
  void clear() {
    stats = sessionFactory.getStatistics();
    stats.clear();
  }

  @Test
  void controllerPart_lazyInitializationException() {
    Order order = service.findById(1L);           // Transaktion ist hier schon zu Ende
    // „Controller-Teil“: Zugriff auf eine Lazy-Beziehung außerhalb der Service-Transaktion
    assertThatThrownBy(() -> order.getItems().size())
        .isInstanceOf(LazyInitializationException.class);
    assertThatThrownBy(() -> order.getCustomer().getName())
        .isInstanceOf(LazyInitializationException.class);
  }

  @Test
  void entityAsJson_failsFast() throws Exception {
    MvcResult result = mvc.perform(get("/ep035/orders/1")).andReturn();
    // Der Serializer greift auf customer zu → keine Session mehr → Fehler statt versteckter Query
    assertThat(result.getResolvedException()).hasRootCauseInstanceOf(LazyInitializationException.class);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }

  @Test
  void dto_oneQuery() throws Exception {
    mvc.perform(get("/ep035/orders/1/view"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].sku").exists());
    // JOIN FETCH: Order + Customer + Items + Products in einem Statement
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
  }
}
