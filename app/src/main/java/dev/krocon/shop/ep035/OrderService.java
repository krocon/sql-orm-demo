package dev.krocon.shop.ep035;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.repo.OrderRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Der Service lädt gezielt (JOIN FETCH) und gibt ein DTO zurück. */
@Service("ep035OrderService")
public class OrderService {

  private final OrderRepository orders;
  private final EntityManager em;

  public OrderService(OrderRepository orders, EntityManager em) {
    this.orders = orders;
    this.em = em;
  }

  /** Problem: gibt die Entity heraus – Beziehungen sind nach der Transaktion noch Lazy-Proxies. */
  @Transactional(readOnly = true)
  public Order findById(Long id) {
    return orders.findById(id).orElseThrow();
  }

  /** Lösung: eine Query mit JOIN FETCH, Mapping auf das DTO noch in der Transaktion. */
  @Transactional(readOnly = true)
  public OrderView findWithItemsById(Long id) {
    Order order = em.createQuery("""
        select o from Order o
          join fetch o.customer
          join fetch o.items i
          join fetch i.product
        where o.id = :id""", Order.class)
        .setParameter("id", id)
        .getSingleResult();
    return OrderView.from(order);
  }
}
