package dev.krocon.shop.ep038;

import dev.krocon.shop.domain.OrderStatus;
import java.math.BigDecimal;

public record StatusRevenue(OrderStatus status, BigDecimal revenue) { }
