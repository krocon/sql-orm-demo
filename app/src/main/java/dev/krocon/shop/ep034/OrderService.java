package dev.krocon.shop.ep034;

import dev.krocon.shop.domain.Order;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Folge 034: Bestellseite vorher (N+1) und nachher (JOIN FETCH, Entity Graph, Projektion).
 * readOnly = true: Hibernate spart sich beim Lesen Snapshots und Dirty Checking.
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

  private final Ep034OrderRepository orderRepository;

  public OrderService(Ep034OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /** Vorher: 1 Query für Orders + 1 Query pro Kunde. */
  public List<OrderSummary> pageNaive() {
    List<Order> orders = orderRepository.findAll();
    return orders.stream()
      .map(o -> new OrderSummary(o.getId(), o.getTotal(), o.getCustomer().getName()))   // N+1
      .toList();
  }

  public List<OrderSummary> pageJoinFetch() {
    return toSummaries(orderRepository.findAllWithCustomer());
  }

  public List<OrderSummary> pageEntityGraph() {
    return toSummaries(orderRepository.findAllWithCustomerGraph());
  }

  /** Nachher: eine Query, nur drei Spalten, keine Entities. */
  public List<OrderSummary> pageProjection() {
    return orderRepository.findSummaries();
  }

  private static List<OrderSummary> toSummaries(List<Order> orders) {
    return orders.stream()
      .map(o -> new OrderSummary(o.getId(), o.getTotal(), o.getCustomer().getName()))
      .toList();
  }
}
