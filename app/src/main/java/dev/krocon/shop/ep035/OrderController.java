package dev.krocon.shop.ep035;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.repo.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("ep035OrderController")
@RequestMapping("/ep035")
public class OrderController {

  private final OrderRepository orders;
  private final OrderService service;

  public OrderController(OrderRepository orders, OrderService service) {
    this.orders = orders;
    this.service = service;
  }

  /** Problem (wie im Video): Entity als JSON – der Serializer löst Lazy Loading aus. */
  @GetMapping("/orders/{id}")
  public Order get(@PathVariable Long id) {
    return orders.findById(id).orElseThrow();
  }

  /** Lösung: der Service liefert ein fertig geladenes DTO. */
  @GetMapping("/orders/{id}/view")
  public OrderView view(@PathVariable Long id) {
    return service.findWithItemsById(id);
  }
}
