package dev.krocon.shop.ep029;

import dev.krocon.shop.domain.Order;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * SO NICHT (Folge 029): Entity als JSON raus und als Request Body rein.
 * GET: customer → orders → customer → … (Endlosschleife) oder Lazy-Fehler.
 * PUT: Der Client setzt status und total – Mass Assignment.
 */
@RestController
@RequestMapping("/ep029/bad/orders")
public class BadOrderController {

  private final Ep029OrderRepository orderRepository;

  public BadOrderController(Ep029OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @GetMapping("/{id}")
  public Order get(@PathVariable Long id) {      // Entity als JSON
    return orderRepository.findById(id).orElseThrow();
  }

  @PutMapping("/{id}")
  @Transactional
  public Order update(@PathVariable Long id, @RequestBody Order order) {   // Client setzt status, total …
    Order existing = orderRepository.findById(id).orElseThrow();
    existing.setStatus(order.getStatus());
    existing.setTotal(order.getTotal());   // Summe kommt vom Client!
    return existing;
  }
}
