package dev.krocon.shop.ep034;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Zeichnet jedes SQL auf, das Hibernate abschickt (Folge 034).
 * Einbinden per Testklasse:
 * <pre>
 * &#64;TestPropertySource(properties = SqlRecorder.PROPERTY)
 * </pre>
 */
public class SqlRecorder implements StatementInspector {

  public static final String PROPERTY =
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=dev.krocon.shop.ep034.SqlRecorder";

  private static final List<String> SQL = new CopyOnWriteArrayList<>();

  @Override
  public String inspect(String sql) {
    SQL.add(sql);
    return sql;   // SQL unverändert weitergeben
  }

  public static void clear() {
    SQL.clear();
  }

  public static List<String> statements() {
    return new ArrayList<>(SQL);
  }

  /** Alle aufgezeichneten Statements, die mit dem Präfix beginnen (z. B. "select"). */
  public static List<String> startingWith(String prefix) {
    return SQL.stream().filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase())).toList();
  }
}
