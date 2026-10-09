package dev.krocon.shop.ep024;

import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 024: Massenimport – mit und ohne flush()/clear(). */
@Service("ep024ProductImport")
public class ProductImport {

  static final int BATCH = 50;

  private final EntityManager em;

  public ProductImport(EntityManager em) {
    this.em = em;
  }

  /** Problem: alles bleibt managed → Persistence Context wächst mit jeder Zeile. */
  @Transactional
  public void importAll(List<ProductRow> rows) {
    for (ProductRow r : rows) {
      em.persist(new Product(r.sku(), r.name(), r.price(), 0));
    }
  }

  /** Lösung: alle 50 Zeilen flush() (SQL raus) und clear() (Objekte loslassen). */
  @Transactional
  public void importInBatches(List<ProductRow> rows) {
    for (int i = 0; i < rows.size(); i++) {
      ProductRow r = rows.get(i);
      em.persist(new Product(r.sku(), r.name(), r.price(), 0));
      if ((i + 1) % BATCH == 0) {
        em.flush();
        em.clear();
      }
    }
  }
}
