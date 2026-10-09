package dev.krocon.shop.ep040;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.repo.OrderRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mischen ist legitim: JPA zum Schreiben, JdbcClient für den Report – in derselben Transaktion. */
@Service("ep040OrderService")
public class OrderService {

  private final OrderRepository orders;
  private final RevenueJdbc revenue;
  private final EntityManager em;

  public OrderService(OrderRepository orders, RevenueJdbc revenue, EntityManager em) {
    this.orders = orders;
    this.revenue = revenue;
    this.em = em;
  }

  @Transactional
  public List<StatusRevenue> cancelAndReport(Long orderId) {
    Order order = orders.findById(orderId).orElseThrow();
    order.setStatus(OrderStatus.CANCELLED);   // nur im Persistence Context
    em.flush();   // wichtig: JdbcClient geht an Hibernate vorbei – ohne flush fehlt die Änderung im Report
    return revenue.revenueByStatus();
  }

  /** Zum Vergleich: ohne flush sieht der JDBC-Report noch den alten Stand. */
  @Transactional
  public List<StatusRevenue> cancelAndReportWithoutFlush(Long orderId) {
    orders.findById(orderId).orElseThrow().setStatus(OrderStatus.CANCELLED);
    return revenue.revenueByStatus();
  }
}
