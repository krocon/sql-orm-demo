package dev.krocon.shop.ep038;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import dev.krocon.shop.repo.OrderRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository("ep038ReportRepository")
public class ReportRepository {

  private final OrderRepository orders;
  private final EntityManager em;

  public ReportRepository(OrderRepository orders, EntityManager em) {
    this.orders = orders;
    this.em = em;
  }

  /** Anti-Pattern: alle Bestellungen als Entities laden und in Java summieren. */
  public Map<OrderStatus, BigDecimal> revenueByStatusFindAll() {
    List<Order> all = orders.findAll();
    return all.stream().collect(groupingBy(Order::getStatus,
        reducing(BigDecimal.ZERO, Order::getTotal, BigDecimal::add)));
  }

  /** Besser: SQL-Projektion – die Datenbank gruppiert, es kommen 4 Zeilen zurück. */
  public List<StatusRevenue> revenueByStatus() {
    return em.createQuery("""
        select new dev.krocon.shop.ep038.StatusRevenue(o.status, sum(o.total))
        from Order o group by o.status order by o.status""", StatusRevenue.class)
        .getResultList();
  }
}
