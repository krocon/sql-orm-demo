package dev.krocon.shop.ep028;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Folge 028: Repository aus dem Video. Heißt hier Ep028OrderRepository, weil
 * repo.OrderRepository schon den Bean-Namen „orderRepository“ belegt.
 */
public interface Ep028OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByCustomerEmailAndStatus(String email, OrderStatus s);

  @Query("select o from Order o where o.total > :min")
  Page<Order> findLarge(@Param("min") BigDecimal min, Pageable page);

  List<OrderSummary> findByStatus(OrderStatus status);  // Record

  List<OrderTotalView> findTop3ByStatusOrderByTotalDesc(OrderStatus status);   // Interface-Projection

  /** Problem: funktioniert, ist aber unlesbar → lieber @Query oder Specification. */
  List<Order> findByCustomerEmailAndStatusInAndCreatedAtAfterAndTotalGreaterThan(
      String email, Collection<OrderStatus> statuses, Instant after, BigDecimal min);
}
