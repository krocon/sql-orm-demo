package dev.krocon.shop.ep025;

import static java.math.RoundingMode.HALF_UP;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 025: Dirty Checking – gewollt, versehentlich und abgeschaltet. */
@Service("ep025OrderService")
public class OrderService {

  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /** Hook: managed Entity ändern genügt. */
  @Transactional
  public void ship(Long id) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.setStatus(OrderStatus.SHIPPED);
    // kein save(): UPDATE kommt beim Flush
  }

  /** Problem: Anzeigelogik verändert die Entity → UPDATE beim Commit. */
  @Transactional
  public OrderView show(Long id) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.setTotal(order.getTotal().setScale(0, HALF_UP)); // nur Anzeige
    return OrderView.of(order);
  }  // Commit: gerundeter Betrag per UPDATE in der DB!

  /** Gleicher Fehler, aber readOnly: kein Snapshot, kein Dirty Checking, kein UPDATE. */
  @Transactional(readOnly = true)
  public OrderView showReadOnly(Long id) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.setTotal(order.getTotal().setScale(0, HALF_UP));
    return OrderView.of(order);
  }

  /** Richtig: Entity bleibt unberührt, gerundet wird im DTO. */
  @Transactional(readOnly = true)
  public OrderView showDto(Long id) {
    return OrderView.of(orderRepository.findById(id).orElseThrow());
  }
}
