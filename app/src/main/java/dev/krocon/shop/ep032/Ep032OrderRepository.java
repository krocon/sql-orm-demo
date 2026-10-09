package dev.krocon.shop.ep032;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Folge 032: Bulk-UPDATE statt Schleife. */
public interface Ep032OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByCreatedAtBefore(Instant cutoff);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
    update Order o set o.status = :status
    where o.createdAt < :cutoff""")
  int archiveOlderThan(OrderStatus status, Instant cutoff);   // ein UPDATE statt N

  /** Zum Vergleich: ohne clear bleiben geladene Entities veraltet. */
  @Modifying
  @Query("""
    update Order o set o.status = :status
    where o.createdAt < :cutoff""")
  int archiveOlderThanWithoutClear(OrderStatus status, Instant cutoff);

  /** Zum Vergleich: clear ohne flush – was passiert mit offenen Änderungen? */
  @Modifying(clearAutomatically = true)
  @Query("""
    update Order o set o.status = :status
    where o.createdAt < :cutoff""")
  int archiveOlderThanWithoutFlush(OrderStatus status, Instant cutoff);
}
