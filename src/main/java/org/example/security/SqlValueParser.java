package org.example.security;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SqlValueParser {

    private SqlValueParser() {
    }

    public static Object parse(
            String tableName,
            String columnName,
            String value
    ) {
        String key = tableName + "." + columnName;

        return switch (key) {
            case "books.author_id",
                 "orders.customer_id",
                 "order_items.order_id",
                 "order_items.book_id" ->
                    Long.parseLong(value);

            case "books.stock",
                 "order_items.quantity" ->
                    Integer.parseInt(value);

            case "books.price",
                 "order_items.price" ->
                    new BigDecimal(value);

            case "orders.created_at" ->
                    Timestamp.valueOf(value);

            default -> value;
        };
    }
}