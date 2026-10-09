package dev.krocon.shop.ep035;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.krocon.shop.TestcontainersConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** OSIV an (Spring-Boot-Standard): der Persistence Context lebt bis zum Ende des Requests. */
@SpringBootTest(properties = "spring.jpa.open-in-view=true")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OsivTest {
  @Autowired MockMvc mvc;
  @Autowired SessionFactory sessionFactory;

  @Test
  void entityAsJson_lazyLoadingWhileRendering() throws Exception {
    Statistics stats = sessionFactory.getStatistics();
    stats.clear();

    MvcResult result = mvc.perform(get("/ep035/orders/1")).andReturn();

    // 1 Query für findById + versteckte Queries beim JSON-Rendern (customer, customer.orders …)
    System.out.println("Statements mit OSIV: " + stats.getPrepareStatementCount());
    assertThat(stats.getPrepareStatementCount()).isGreaterThan(1);
    // Bonus: bidirektionale Beziehungen (Order → Customer → orders → Order …) = Endlos-Rekursion,
    // Jackson bricht bei Tiefe 500 ab – die Antwort ist kaputtes JSON.
    assertThat(result.getResolvedException())
        .isInstanceOf(HttpMessageNotWritableException.class)
        .hasMessageContaining("nesting depth");
  }
}
