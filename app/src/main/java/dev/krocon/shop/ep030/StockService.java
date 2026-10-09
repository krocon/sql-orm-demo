package dev.krocon.shop.ep030;

import dev.krocon.shop.domain.Product;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Folge 030: drei Wege, Lagerbestand sicher zu ändern. */
@Service
public class StockService {

  private final Ep030ProductRepository productRepository;
  private final TransactionTemplate tx;

  public StockService(Ep030ProductRepository productRepository, PlatformTransactionManager txManager) {
    this.productRepository = productRepository;
    this.tx = new TransactionTemplate(txManager);
  }

  /** Optimistisch: @Version prüft beim UPDATE; bei Konflikt komplett neu versuchen. */
  public int changeStockWithRetry(Long productId, int delta, int maxAttempts) {
    for (int attempt = 1; ; attempt++) {
      try {
        return tx.execute(status -> {
          Product p = productRepository.findById(productId).orElseThrow();
          p.setStock(p.getStock() + delta);
          return p.getStock();
        });
      } catch (ObjectOptimisticLockingFailureException e) {
        if (attempt >= maxAttempts) {
          throw e;   // im Controller → 409 Conflict
        }
      }
    }
  }

  /** Pessimistisch: Zeile sperren, andere Transaktionen warten bis zum Commit. */
  @Transactional
  public int changeStockLocked(Long productId, int delta) {
    Product p = productRepository.findForUpdate(productId).orElseThrow();
    p.setStock(p.getStock() + delta);
    return p.getStock();
  }

  /** Atomar: ein UPDATE mit Bedingung. false = ausverkauft. */
  @Transactional
  public boolean reserveOne(Long productId) {
    return productRepository.decrementStock(productId) == 1;
  }
}
