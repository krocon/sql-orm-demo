package dev.krocon.shop.ep027;

import dev.krocon.shop.domain.Customer;
import dev.krocon.shop.domain.Order;
import dev.krocon.shop.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/** Folge 027: JPQL, Native SQL, HQL-Extras und Criteria API nebeneinander. */
@Repository("ep027OrderQueries")
public class OrderQueries {

  private final EntityManager em;

  public OrderQueries(EntityManager em) {
    this.em = em;
  }

  /** Hook: JPQL auf Entities, DTO per select new, Parameter gebunden. */
  public List<OrderSummary> summaries(OrderStatus status) {
    return em.createQuery("""
            select new dev.krocon.shop.ep027.OrderSummary(o.id, c.email, o.total)
            from Order o join o.customer c
            where o.status = :status""", OrderSummary.class)
        .setParameter("status", status)
        .getResultList();
  }

  /** Problem: String-Verkettung → SQL-Injection. NICHT nachmachen. */
  @SuppressWarnings("unchecked")
  public List<Order> byStatusUnsafe(String status) {
    String sql = "SELECT * FROM orders WHERE status = '" + status + "'";
    return em.createNativeQuery(sql, Order.class).getResultList();
  }

  /** Native SQL, aber sicher: Parameter gebunden. */
  @SuppressWarnings("unchecked")
  public List<Order> byStatusNative(String status) {
    return em.createNativeQuery("SELECT * FROM orders WHERE status = :status", Order.class)
        .setParameter("status", status)
        .getResultList();
  }

  /** Native SQL für PostgreSQL-Features: JSONB-Containment-Operator @>. */
  @SuppressWarnings("unchecked")
  public List<Long> orderIdsWithAttrs(String jsonFilter) {
    return em.createNativeQuery("""
            SELECT o.id FROM orders o
            JOIN ep027_order_attrs a ON a.order_id = o.id
            WHERE a.attrs @> CAST(:filter AS jsonb)
            ORDER BY o.id""", Long.class)
        .setParameter("filter", jsonFilter)
        .getResultList();
  }

  /** Native SQL mit Window Function: Top-Bestellung je Kunde. */
  public List<OrderRank> topOrderPerCustomerNative(int limit) {
    List<Object[]> rows = em.createNativeQuery("""
            SELECT id, customer_id, total, rnk FROM (
              SELECT o.id, o.customer_id, o.total,
                     rank() OVER (PARTITION BY o.customer_id ORDER BY o.total DESC) AS rnk
              FROM orders o
            ) t
            WHERE rnk = 1
            ORDER BY total DESC, id
            LIMIT :limit""", Object[].class)
        .setParameter("limit", limit)
        .getResultList();
    return rows.stream()
        .map(r -> new OrderRank(((Number) r[0]).longValue(), ((Number) r[1]).longValue(),
            (BigDecimal) r[2], ((Number) r[3]).longValue()))
        .toList();
  }

  /** Dasselbe in HQL (Hibernate 6+): Window Function + CTE – kein Standard-JPQL. */
  public List<OrderRank> topOrderPerCustomerHql(int limit) {
    return em.createQuery("""
            with ranked as (
              select o.id as id, o.customer.id as customerId, o.total as total,
                     rank() over (partition by o.customer.id order by o.total desc) as rnk
              from Order o
            )
            select new dev.krocon.shop.ep027.OrderRank(r.id, r.customerId, r.total, r.rnk)
            from ranked r
            where r.rnk = 1
            order by r.total desc, r.id""", OrderRank.class)
        .setMaxResults(limit)
        .getResultList();
  }

  /** Criteria API: dynamische Filter mit optionalen Bedingungen. */
  public List<Order> filter(OrderFilter f) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> q = cb.createQuery(Order.class);
    Root<Order> o = q.from(Order.class);
    List<Predicate> where = new ArrayList<>();
    if (f.status() != null) {
      where.add(cb.equal(o.get("status"), f.status()));
    }
    if (f.minTotal() != null) {
      where.add(cb.greaterThanOrEqualTo(o.get("total"), f.minTotal()));
    }
    if (f.email() != null) {
      Join<Order, Customer> c = o.join("customer");
      where.add(cb.equal(c.get("email"), f.email()));
    }
    q.select(o).where(where.toArray(Predicate[]::new)).orderBy(cb.asc(o.get("id")));
    return em.createQuery(q).getResultList();
  }
}
