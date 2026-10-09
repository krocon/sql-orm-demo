package dev.krocon.shop.ep033;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.ep034.SqlRecorder;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Folge 033: Keyset statt OFFSET – Window/ScrollPosition und EXPLAIN im Vergleich. */
@ShopJpaTest
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {SqlRecorder.PROPERTY, "logging.level.org.hibernate.orm.jdbc.bind=trace"})
class KeysetScrollTest {

  @Autowired Ep033OrderRepository orderRepository;
  @Autowired DataSource dataSource;

  @Test
  void window_scrollsByKeyset(CapturedOutput output) {
    SqlRecorder.clear();
    Window<Order> w = orderRepository.findFirst20ByOrderByIdAsc(ScrollPosition.keyset());
    assertThat(w.getContent()).extracting(Order::getId).startsWith(1L, 2L).endsWith(20L);
    assertThat(w.hasNext()).isTrue();

    // nächste Seite: Position = letzte Zeile des Fensters (id 20)
    Window<Order> next = orderRepository.findFirst20ByOrderByIdAsc(w.positionAt(w.size() - 1));
    assertThat(next.getContent()).extracting(Order::getId).startsWith(21L).endsWith(40L);

    List<String> sql = SqlRecorder.statements();
    sql.forEach(s -> System.out.println("ep033 keyset sql: " + s));
    assertThat(sql.get(0)).endsWith("from orders o1_0 order by o1_0.id fetch first ? rows only");
    assertThat(sql.get(1)).endsWith("from orders o1_0 where o1_0.id>? order by o1_0.id fetch first ? rows only");
    // Bind-Parameter: id > 20, LIMIT 21 (eine Zeile mehr, damit Spring Data hasNext kennt)
    assertThat(output.getOut()).contains("binding parameter (1:BIGINT) <- [20]", "binding parameter (2:INTEGER) <- [21]");
  }

  @Test
  void explain_offsetReadsAndDiscards_keysetDoesNot() {
    long offsetRows = rowsScanned("select * from orders order by id offset 4000 limit 20");
    long keysetRows = rowsScanned("select * from orders where id > 4000 order by id limit 20");

    System.out.printf("ep033 explain: OFFSET 4000 liest %d Zeilen, Keyset liest %d Zeilen%n", offsetRows, keysetRows);
    assertThat(offsetRows).isEqualTo(4020);   // 4000 gelesen und weggeworfen
    assertThat(keysetRows).isEqualTo(20);
  }

  /** Zeilen, die der Index Scan unter dem LIMIT tatsächlich geliefert hat (EXPLAIN ANALYZE). */
  long rowsScanned(String sql) {
    String json = JdbcClient.create(dataSource).sql("EXPLAIN (ANALYZE, FORMAT JSON) " + sql)
      .query((rs, n) -> rs.getString(1)).single();
    JsonNode limit = JsonMapper.builder().build().readTree(json).get(0).get("Plan");
    JsonNode scan = limit.get("Plans").get(0);
    System.out.println("ep033 plan: " + Map.of("node", scan.get("Node Type").asString(), "sql", sql));
    return Math.round(scan.get("Actual Rows").asDouble());
  }
}
