package dev.krocon.shop.ep030;

import dev.krocon.shop.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Folge 030: pessimistisch sperren und atomar updaten. */
public interface Ep030ProductRepository extends JpaRepository<Product, Long> {

  // PostgreSQL + Hibernate 7.4: select … for no key update of p1_0
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findForUpdate(Long id);

  // Atomar, ohne vorheriges Lesen. Rückgabe 0 = ausverkauft.
  // Achtung: JPQL-Bulk-Updates erhöhen @Version NICHT (dafür: "update versioned Product …").
  @Modifying
  @Query("update Product p set p.stock = p.stock - 1 where p.id = :id and p.stock > 0")
  int decrementStock(Long id);
}
