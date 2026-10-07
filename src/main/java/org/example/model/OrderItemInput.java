package org.example.model;

import java.math.BigDecimal;

public record OrderItemInput(
        long bookId,
        int quantity,
        BigDecimal price
) {
}