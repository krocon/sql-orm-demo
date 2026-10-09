package dev.krocon.shop.ep016;

import dev.krocon.shop.domain.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 016: Service-Methode, nach deren Ende die Session zu ist. */
@Service("ep016OrderService")
public class OrderService {

  private final Ep016OrderRepository orders;

  public OrderService(Ep016OrderRepository orders) {
    this.orders = orders;
  }

  /** Problem: customer bleibt ein uninitialisierter Proxy. */
  @Transactional(readOnly = true)
  public Order find(Long id) {
    return orders.findById(id).orElseThrow();
  }

  /** Lösung: customer per JOIN FETCH in derselben Query. */
  @Transactional(readOnly = true)
  public Order findWithCustomer(Long id) {
    return orders.findWithCustomer(id).orElseThrow();
  }
}
