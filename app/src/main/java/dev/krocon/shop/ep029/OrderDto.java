package dev.krocon.shop.ep029;

import dev.krocon.shop.domain.OrderStatus;
import java.math.BigDecimal;

/** Response-DTO: unveränderlich, nur die Felder, die der Client braucht (Folge 029). */
public record OrderDto(Long id, OrderStatus status, BigDecimal total) { }
