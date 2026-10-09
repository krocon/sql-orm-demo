package dev.krocon.shop.ep017;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Folge 017: Listen-Ansicht mit Kundennamen. */
@Service("ep017OrderListService")
public class OrderListService {

  private static final Logger log = LoggerFactory.getLogger(OrderListService.class);
  private final Ep017OrderRepository orderRepository;

  public OrderListService(Ep017OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /** Problem: 1 Query für die Bestellungen + 1 Query pro Kunde. */
  @Transactional(readOnly = true)
  public List<String> customerNames(OrderStatus status, int max) {
    List<Order> orders = orderRepository.findByStatus(status, Limit.of(max));   // 1 Query
    List<String> names = new ArrayList<>();
    for (Order o : orders) {
      log.info(o.getCustomer().getName());   // + 1 Query pro Kunde
      names.add(o.getCustomer().getName());
    }
    return names;
  }

  /** Lösung: dieselbe Schleife, Kunden per Entity Graph schon geladen. */
  @Transactional(readOnly = true)
  public List<String> customerNamesWithGraph(OrderStatus status, int max) {
    List<Order> orders = orderRepository.findWithCustomerByStatus(status, Limit.of(max));   // 1 Query
    List<String> names = new ArrayList<>();
    for (Order o : orders) {
      log.info(o.getCustomer().getName());   // keine weitere Query
      names.add(o.getCustomer().getName());
    }
    return names;
  }
}
