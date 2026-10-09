package dev.krocon.shop.ep029;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Request-DTO: Der Client schickt nur Kunde und Positionen.
 * Kein id, kein status, kein total → kein Mass Assignment.
 */
public record CreateOrderRequest(
  @NotNull Long customerId,
  @NotEmpty List<@Valid Line> items) {

  public record Line(@NotNull Long productId, @Positive int quantity) { }
}
