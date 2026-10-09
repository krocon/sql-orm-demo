package dev.krocon.shop.ep034;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.function.Supplier;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Wiederverwendbarer Test-Helfer (Folge 034): zählt die JDBC-Statements,
 * die ein Codeblock auslöst – über Hibernate Statistics
 * (hibernate.generate_statistics: true in application.yaml).
 *
 * <pre>
 * QueryCount.of(em).assertStatements(1, () -> orderService.loadPage());
 * </pre>
 */
public final class QueryCount {

  private final Statistics stats;

  private QueryCount(Statistics stats) {
    this.stats = stats;
  }

  public static QueryCount of(EntityManager em) {
    Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    if (!stats.isStatisticsEnabled()) {
      throw new IllegalStateException("hibernate.generate_statistics ist aus");
    }
    return new QueryCount(stats);
  }

  /** Führt den Block aus und liefert die Anzahl vorbereiteter JDBC-Statements. */
  public long count(Runnable action) {
    stats.clear();
    action.run();
    return stats.getPrepareStatementCount();
  }

  /** Wie {@link #count(Runnable)}, liefert zusätzlich das Ergebnis des Blocks. */
  public <T> Counted<T> measure(Supplier<T> action) {
    stats.clear();
    T result = action.get();
    return new Counted<>(result, stats.getPrepareStatementCount());
  }

  /** Assert auf die Statement-Anzahl: fängt N+1 im Test, bevor es live geht. */
  public void assertStatements(long expected, Runnable action) {
    long actual = count(action);
    assertThat(actual)
      .as("Anzahl JDBC-Statements (N+1?)")
      .isEqualTo(expected);
  }

  /** Höchstens n Statements – robuster, wenn sich Details ändern. */
  public void assertAtMost(long max, Runnable action) {
    long actual = count(action);
    assertThat(actual).as("Anzahl JDBC-Statements (N+1?)").isLessThanOrEqualTo(max);
  }

  /** Direkter Zugriff, z. B. für Entity-Load- oder Update-Zähler. */
  public Statistics statistics() {
    return stats;
  }

  public record Counted<T>(T result, long statements) { }
}
