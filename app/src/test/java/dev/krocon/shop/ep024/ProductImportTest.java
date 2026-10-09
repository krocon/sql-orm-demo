package dev.krocon.shop.ep024;

import static org.assertj.core.api.Assertions.assertThat;

import dev.krocon.shop.ShopJpaTest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** Folge 024 (Problem): Import ohne clear() hält jedes Objekt im Persistence Context. */
@ShopJpaTest
@Import(ProductImport.class)
class ProductImportTest {

  static final int ROWS = 2_000;

  @Autowired EntityManager em;
  @Autowired ProductImport productImport;

  @Test
  void withoutClear_everyEntityStaysManaged() {
    productImport.importAll(rows("A"));
    int managed = managedEntities();
    System.out.println("importAll: " + managed + " Entities im Persistence Context");
    assertThat(managed).isEqualTo(ROWS);         // wächst linear → bei 1 Mio. Zeilen: Speicher voll
  }

  @Test
  void flushAndClear_contextStaysSmall() {
    productImport.importInBatches(rows("B"));
    int managed = managedEntities();
    System.out.println("importInBatches: " + managed + " Entities im Persistence Context");
    assertThat(managed).isLessThan(ProductImport.BATCH);
    Long saved = em.createQuery("select count(p) from Product p where p.sku like 'B-%'", Long.class).getSingleResult();
    assertThat(saved).isEqualTo(ROWS);           // trotzdem alles gespeichert
  }

  private int managedEntities() {
    return em.unwrap(Session.class).getStatistics().getEntityCount();   // Hibernate: SessionStatistics
  }

  private static List<ProductRow> rows(String prefix) {
    return IntStream.range(0, ROWS)
        .mapToObj(i -> new ProductRow(prefix + "-" + i, "Import " + i, BigDecimal.TEN))
        .toList();
  }
}
