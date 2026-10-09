package dev.krocon.shop.ep029;

import dev.krocon.shop.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Folge 029: Lesezugriffe direkt als DTO-Projektion (select new). */
public interface Ep029OrderRepository extends JpaRepository<Order, Long> {

  @Query("""
    select new dev.krocon.shop.ep029.OrderDto(o.id, o.status, o.total)
    from Order o where o.customer.id = :customerId""")
  List<OrderDto> findDtosByCustomer(Long customerId);   // nur 3 Spalten

  @Query("""
    select new dev.krocon.shop.ep029.OrderDto(o.id, o.status, o.total)
    from Order o where o.id = :id""")
  Optional<OrderDto> findDtoById(Long id);
}
