package dev.krocon.shop.ep019;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Folge 019: Die EINE Stelle, an der der Code die Hibernate-Session braucht.
 * Services sprechen weiter nur mit dem EntityManager (JPA-API).
 */
@Component("ep019UnwrapSession")
public class UnwrapSession {

  private final EntityManager em;

  public UnwrapSession(EntityManager em) {
    this.em = em;
  }

  /** Hibernate-only: mehrere IDs in einem Rutsch laden (Session#findMultiple). */
  public List<Ep019Order> loadOrders(List<Long> ids) {
    Session session = em.unwrap(Session.class);
    return session.findMultiple(Ep019Order.class, ids);
  }
}
