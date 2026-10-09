package dev.krocon.shop.ep016;

import dev.krocon.shop.domain.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Folge 016: Customer gezielt für diesen Use Case mitladen. */
public interface Ep016OrderRepository extends JpaRepository<Order, Long> {

  @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.id = :id")
  Optional<Order> findWithCustomer(Long id);   // gezielt für diesen Fall

  @EntityGraph(attributePaths = "customer")    // dasselbe per Entity Graph
  Optional<Order> findWithCustomerGraphById(Long id);
}
