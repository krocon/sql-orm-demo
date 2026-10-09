package dev.krocon.shop.ep034;

import dev.krocon.shop.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Folge 034: verschiedene Fetch-Pläne für dieselbe Bestellseite. */
public interface Ep034OrderRepository extends JpaRepository<Order, Long> {

  @Query("select o from Order o join fetch o.customer")
  List<Order> findAllWithCustomer();

  @EntityGraph(attributePaths = "customer")
  @Query("select o from Order o")
  List<Order> findAllWithCustomerGraph();

  @Query("""
    select new dev.krocon.shop.ep034.OrderSummary(o.id, o.total, c.name)
    from Order o join o.customer c""")
  List<OrderSummary> findSummaries();
}
