package dev.krocon.shop.ep033;

import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Folge 033: Pagination & Sorting. */
public interface Ep033OrderRepository extends JpaRepository<Order, Long> {

  /** Page: Daten + zusätzliche COUNT-Query für totalElements/totalPages. */
  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  /** Slice: nur Daten, lädt size + 1 Zeilen, um hasNext zu kennen. Kein COUNT. */
  Slice<Order> findSliceByStatus(OrderStatus status, Pageable pageable);

  /** Keyset: Window + ScrollPosition → WHERE id > :lastId ORDER BY id. */
  Window<Order> findFirst20ByOrderByIdAsc(ScrollPosition position);

  /**
   * Video-Beispiel: JOIN FETCH auf Collection + Pageable.
   * Hibernate 7.4 + PostgreSQL schiebt LIMIT/OFFSET in eine Unterabfrage (derived table) –
   * KEINE HHH90003004-Warnung mehr. Frühere Versionen (z. B. Hibernate 6) paginieren im Speicher.
   */
  @Query("select o from Order o join fetch o.items")
  List<Order> findWithItems(Pageable pageable);

  /**
   * SO NICHT: Sortierung nach einem Feld der gefetchten Collection.
   * Das kann Hibernate 7.4 nicht in eine Unterabfrage schieben → HHH90003004,
   * alle Zeilen werden geladen und im Speicher paginiert.
   */
  @Query("select o from Order o join fetch o.items i order by i.quantity desc, o.id")
  List<Order> findWithItemsByQuantity(Pageable pageable);

  /** SO: erst IDs paginieren (in der DB) … */
  @Query("select o.id from Order o")
  List<Long> findIds(Pageable pageable);

  /** … dann genau diese Orders mit Items laden. */
  @Query("select distinct o from Order o join fetch o.items where o.id in :ids order by o.id")
  List<Order> findWithItemsByIdIn(Collection<Long> ids);
}
