package dev.krocon.shop.ep038;

import dev.krocon.shop.repo.OrderRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API gibt OrderDto-Records zurück, paginiert – nie Entities, nie findAll(). */
@RestController("ep038OrderController")
@RequestMapping("/ep038")
public class OrderController {

  private final OrderRepository orders;
  private final ReportRepository reports;

  public OrderController(OrderRepository orders, ReportRepository reports) {
    this.orders = orders;
    this.reports = reports;
  }

  @GetMapping("/orders")
  public List<OrderDto> list(@RequestParam(defaultValue = "0") int page) {
    return orders.findAll(PageRequest.of(page, 20, Sort.by("id")))
        .map(OrderDto::from).getContent();
  }

  @GetMapping("/reports/revenue")
  public List<StatusRevenue> revenue() {
    return reports.revenueByStatus();
  }
}
