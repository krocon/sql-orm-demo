package dev.krocon.shop.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String country;

  private String city;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "customer")
  private List<Order> orders = new ArrayList<>();

  protected Customer() { }   // für JPA

  public Customer(String email, String name, String country) {
    this.email = email;
    this.name = name;
    this.country = country;
  }

  public Long getId() { return id; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCountry() { return country; }
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
  public Instant getCreatedAt() { return createdAt; }
  public List<Order> getOrders() { return orders; }
}
