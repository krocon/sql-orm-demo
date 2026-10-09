package dev.krocon.shop.ep036;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("ep036OrderRepository")
public interface OrderRepository extends JpaRepository<Order, Long> {

  /** Mit @SoftDelete braucht keine Query ein "where deleted = false" – Hibernate ergänzt es selbst. */
  @Query("select o from Ep036Order o")
  List<Order> findAllJpql();
}
