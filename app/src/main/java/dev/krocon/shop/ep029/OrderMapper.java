package dev.krocon.shop.ep029;

import dev.krocon.shop.domain.Order;
import java.util.List;

/**
 * Manuelles Mapping Entity → DTO (Folge 029).
 * Mit MapStruct wäre das nur ein Interface:
 * <pre>
 * &#64;Mapper(componentModel = "spring")
 * public interface OrderMapper { OrderDto toDto(Order order); }
 * </pre>
 * MapStruct erzeugt die Implementierung zur Compile-Zeit (hier nicht eingebunden,
 * um keine zusätzliche Dependency zu brauchen).
 */
public final class OrderMapper {

  private OrderMapper() { }

  public static OrderDto toDto(Order order) {
    return new OrderDto(order.getId(), order.getStatus(), order.getTotal());
  }

  public static List<OrderDto> toDtos(List<Order> orders) {
    return orders.stream().map(OrderMapper::toDto).toList();
  }
}
