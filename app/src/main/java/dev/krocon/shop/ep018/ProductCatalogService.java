package dev.krocon.shop.ep018;

import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 018: Produktseite – First-Level-Cache gilt nur innerhalb einer Transaktion. */
@Service("ep018ProductCatalogService")
public class ProductCatalogService {

  private final EntityManager em;   // transaktionsgebundener Proxy

  public ProductCatalogService(EntityManager em) {
    this.em = em;
  }

  @Transactional(readOnly = true)
  public Product show(Long id) {
    em.find(Product.class, id);          // SELECT
    return em.find(Product.class, id);   // kein SELECT: First-Level
  }

  /** Dasselbe mit der @Cacheable-Entity: ab dem zweiten Request aus dem Second-Level-Cache. */
  @Transactional(readOnly = true)
  public CachedProduct showCached(Long id) {
    return em.find(CachedProduct.class, id);
  }
}
