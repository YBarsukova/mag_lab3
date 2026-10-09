package org.example.service;

import org.example.model.OrderInput;
import org.example.model.OrderItemInput;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("SqlResolve")
public class OrderService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "NEW",
            "PAID",
            "SHIPPED",
            "COMPLETED",
            "CANCELLED"
    );

    private final Connection connection;

    public OrderService(Connection connection) {
        this.connection = connection;
    }

    public long createOrder(
            OrderInput order
    ) throws SQLException {

        validateOrder(order);

        boolean previousAutoCommit =
                connection.getAutoCommit();

        connection.setAutoCommit(false);

        try {
            long orderId =
                    insertOrder(order);

            insertItems(
                    orderId,
                    order.items()
            );

            connection.commit();

            return orderId;

        } catch (SQLException | RuntimeException e) {
            rollback(e);
            throw e;

        } finally {
            connection.setAutoCommit(
                    previousAutoCommit
            );
        }
    }

    public int createOrders(
            List<OrderInput> orders
    ) throws SQLException {

        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Orders are required."
            );
        }

        for (OrderInput order : orders) {
            validateOrder(order);
        }

        boolean previousAutoCommit =
                connection.getAutoCommit();

        connection.setAutoCommit(false);

        try {
            for (OrderInput order : orders) {
                long orderId =
                        insertOrder(order);

                insertItems(
                        orderId,
                        order.items()
                );
            }

            connection.commit();

            return orders.size();

        } catch (SQLException | RuntimeException e) {
            rollback(e);
            throw e;

        } finally {
            connection.setAutoCommit(
                    previousAutoCommit
            );
        }
    }

    private long insertOrder(
            OrderInput order
    ) throws SQLException {

        String sql = """
                INSERT INTO orders (
                    customer_id,
                    status
                )
                VALUES (?, ?)
                RETURNING id
                """;

        String status =
                normalizeStatus(order.status());

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    order.customerId()
            );

            statement.setString(
                    2,
                    status
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new SQLException(
                            "Order ID was not returned."
                    );
                }

                return resultSet.getLong(1);
            }
        }
    }

    private void insertItems(
            long orderId,
            List<OrderItemInput> items
    ) throws SQLException {

        String sql = """
                INSERT INTO order_items (
                    order_id,
                    book_id,
                    quantity,
                    price
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (OrderItemInput item : items) {

                statement.setLong(
                        1,
                        orderId
                );

                statement.setLong(
                        2,
                        item.bookId()
                );

                statement.setInt(
                        3,
                        item.quantity()
                );

                statement.setBigDecimal(
                        4,
                        item.price()
                );

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void validateOrder(
            OrderInput order
    ) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order is required."
            );
        }

        if (order.customerId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid customer ID."
            );
        }

        String status =
                normalizeStatus(order.status());

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid order status."
            );
        }

        if (order.items() == null
                || order.items().isEmpty()) {

            throw new IllegalArgumentException(
                    "Order must contain items."
            );
        }

        for (OrderItemInput item : order.items()) {

            if (item.bookId() <= 0
                    || item.quantity() <= 0
                    || item.price() == null
                    || item.price().signum() < 0) {

                throw new IllegalArgumentException(
                        "Invalid order item."
                );
            }
        }
    }

    private String normalizeStatus(
            String status
    ) {
        if (status == null) {
            return "";
        }

        return status
                .replaceAll("[^A-Za-z]", "")
                .toUpperCase(Locale.ROOT);
    }

    private void rollback(
            Throwable originalError
    ) {
        try {
            connection.rollback();

        } catch (SQLException rollbackError) {
            originalError.addSuppressed(
                    rollbackError
            );
        }
    }
}