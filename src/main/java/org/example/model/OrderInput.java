package org.example.model;

import java.util.List;

public record OrderInput(
        long customerId,
        String status,
        List<OrderItemInput> items
) {

    public OrderInput {
        items = List.copyOf(items);
    }
}