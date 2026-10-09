package dev.krocon.shop.ep029;

import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * SO (Folge 029): Pro Endpunkt Request- und Response-DTO, Lesezugriffe als Projektion.
 * (Transaktionen hier der Kürze halber am Controller – in echt im Service.)
 */
@RestController
@RequestMapping("/ep029/orders")
public class OrderController {

  private final Ep029OrderRepository orderRepository;
  private final EntityManager em;

  public OrderController(Ep029OrderRepository orderRepository, EntityManager em) {
    this.orderRepository = orderRepository;
    this.em = em;
  }

  @GetMapping("/{id}")
  public OrderDto get(@PathVariable Long id) {
    return orderRepository.findDtoById(id).orElseThrow();
  }

  @GetMapping(params = "customerId")
  public List<OrderDto> byCustomer(@RequestParam Long customerId) {
    return orderRepository.findDtosByCustomer(customerId);
  }

  @PostMapping
  @Transactional
  public OrderDto create(@Valid @RequestBody CreateOrderRequest request) {
    Order order = new Order(em.getReference(Customer.class, request.customerId()));
    for (CreateOrderRequest.Line line : request.items()) {
      order.addItem(em.find(Product.class, line.productId()), line.quantity());   // total rechnet der Server
    }
    return OrderMapper.toDto(orderRepository.save(order));
  }
}
