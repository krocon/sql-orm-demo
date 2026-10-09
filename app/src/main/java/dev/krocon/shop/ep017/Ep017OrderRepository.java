package dev.krocon.shop.ep017;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Folge 017: dieselbe Liste – mit N+1 und dreimal ohne. */
public interface Ep017OrderRepository extends JpaRepository<Order, Long> {

  /** Problem: customer ist LAZY → später eine Query pro Kunde. */
  List<Order> findByStatus(OrderStatus status);

  /** Wie oben, aber nur die ersten n (im Video: 100 Bestellungen). */
  List<Order> findByStatus(OrderStatus status, Limit limit);

  /** Lösung 1: Entity Graph → 1 Query mit JOIN. */
  @EntityGraph(attributePaths = "customer")
  List<Order> findWithCustomerByStatus(OrderStatus status, Limit limit);

  /** Lösung 2: JOIN FETCH in JPQL. */
  @Query("select o from Order o join fetch o.customer where o.status = :status")
  List<Order> findJoinFetchByStatus(OrderStatus status);

  /** Lösung 4: DTO-Projektion statt ganzer Entities. */
  @Query("select new dev.krocon.shop.ep017.OrderRow(o.id, c.name) "
      + "from Order o join o.customer c where o.status = :status")
  List<OrderRow> findRowsByStatus(OrderStatus status);
}
