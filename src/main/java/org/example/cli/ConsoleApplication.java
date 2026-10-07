package org.example.cli;

import org.example.logging.AppLogger;
import org.example.model.ColumnUpdate;
import org.example.model.OrderInput;
import org.example.model.OrderItemInput;
import org.example.model.QueryFilter;
import org.example.repository.InsertRepository;
import org.example.repository.SelectRepository;
import org.example.repository.UpdateRepository;
import org.example.security.SqlIdentifierValidator;
import org.example.service.OrderService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ConsoleApplication {

    private final Scanner scanner;
    private final SelectRepository selectRepository;
    private final UpdateRepository updateRepository;
    private final InsertRepository insertRepository;
    private final OrderService orderService;

    public ConsoleApplication(
            Connection connection,
            Scanner scanner
    ) {
        this.scanner = scanner;

        this.selectRepository =
                new SelectRepository(connection);

        this.updateRepository =
                new UpdateRepository(connection);

        this.insertRepository =
                new InsertRepository(connection);

        this.orderService =
                new OrderService(connection);
    }

    public void run() {
        boolean running = true;

        while (running) {
            printMenu();

            System.out.print("Choose an option: ");
            String choice =
                    scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewTable();
                case "2" -> viewTableWithFilter();
                case "3" -> viewTableWithMultipleFilters();
                case "4" -> updateOneRow();
                case "5" -> updateMultipleRows();
                case "6" -> insertOneRow();
                case "7" -> createOrder();
                case "8" -> insertMultipleRows();
                case "9" -> createMultipleOrders();

                case "0" -> {
                    AppLogger.info("Goodbye.");
                    running = false;
                }

                default ->
                        AppLogger.error(
                                "Unknown option. Please try again."
                        );
            }

            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("""
                
                === BOOKSTORE DATABASE ===
                
                1. View table
                2. View table with filter
                3. View table with multiple filters
                4. Update one row
                5. Update multiple rows
                6. Insert one row
                7. Create order with items
                8. Insert multiple rows
                9. Create multiple orders
                0. Exit
                
                """);
    }

    private void viewTable() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        try {
            selectRepository.printAll(tableName);

            AppLogger.info(
                    "Query completed successfully."
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to read data from the database.",
                    e
            );
        }
    }

    private void viewTableWithFilter() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        String columnName =
                readAllowedColumn(tableName);

        if (columnName == null) {
            return;
        }

        System.out.print("Value: ");
        String value =
                scanner.nextLine().trim();

        try {
            selectRepository.printByFilter(
                    tableName,
                    columnName,
                    value
            );

            AppLogger.info(
                    "Query completed successfully."
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid filter value.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to read data from the database.",
                    e
            );
        }
    }

    private void viewTableWithMultipleFilters() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        Integer count = readPositiveInt(
                "Number of filters: "
        );

        if (count == null) {
            return;
        }

        List<QueryFilter> filters =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            System.out.println(
                    "Filter " + i + ":"
            );

            String column =
                    readAllowedColumn(tableName);

            if (column == null) {
                return;
            }

            System.out.print("Value: ");
            String value =
                    scanner.nextLine().trim();

            filters.add(
                    new QueryFilter(
                            column,
                            value
                    )
            );
        }

        try {
            selectRepository.printByFilters(
                    tableName,
                    filters
            );

            AppLogger.info(
                    "Query completed successfully."
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid filter value.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to read data from the database.",
                    e
            );
        }
    }

    private void updateOneRow() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        Long id = readPositiveLong(
                "Record ID: "
        );

        if (id == null) {
            return;
        }

        Integer count = readPositiveInt(
                "Number of columns to update: "
        );

        if (count == null) {
            return;
        }

        if (count >
                SqlIdentifierValidator
                        .getUpdatableColumns(
                                tableName
                        )
                        .size()) {

            AppLogger.error(
                    "Too many columns selected."
            );

            return;
        }

        List<ColumnUpdate> updates =
                new ArrayList<>();

        Set<String> selectedColumns =
                new HashSet<>();

        for (int i = 1; i <= count; i++) {
            System.out.println(
                    "Update " + i + ":"
            );

            String column =
                    readUpdatableColumn(
                            tableName
                    );

            if (column == null) {
                return;
            }

            if (!selectedColumns.add(column)) {
                AppLogger.error(
                        "The same column cannot be selected twice."
                );

                return;
            }

            System.out.print("New value: ");
            String value =
                    scanner.nextLine().trim();

            updates.add(
                    new ColumnUpdate(
                            column,
                            value
                    )
            );
        }

        try {
            int affectedRows =
                    updateRepository.updateOne(
                            tableName,
                            id,
                            updates
                    );

            AppLogger.info(
                    "Query completed successfully. Updated rows: "
                            + affectedRows
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid value for one of the selected columns.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to update the record.",
                    e
            );
        }
    }

    private void updateMultipleRows() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        System.out.println(
                "Column to update:"
        );

        String updateColumn =
                readUpdatableColumn(
                        tableName
                );

        if (updateColumn == null) {
            return;
        }

        System.out.print("New value: ");
        String newValue =
                scanner.nextLine().trim();

        System.out.println(
                "Filter column:"
        );

        String filterColumn =
                readAllowedColumn(
                        tableName
                );

        if (filterColumn == null) {
            return;
        }

        Integer count = readPositiveInt(
                "Number of filter values: "
        );

        if (count == null) {
            return;
        }

        List<String> values =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            System.out.print(
                    "Filter value " + i + ": "
            );

            values.add(
                    scanner.nextLine().trim()
            );
        }

        try {
            int affectedRows =
                    updateRepository.updateMany(
                            tableName,
                            updateColumn,
                            newValue,
                            filterColumn,
                            values
                    );

            AppLogger.info(
                    "Query completed successfully. Updated rows: "
                            + affectedRows
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid value for update operation.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to update records.",
                    e
            );
        }
    }

    private void insertOneRow() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        Integer count = readPositiveInt(
                "Number of columns to insert: "
        );

        if (count == null) {
            return;
        }

        if (count >
                SqlIdentifierValidator
                        .getInsertableColumns(
                                tableName
                        )
                        .size()) {

            AppLogger.error(
                    "Too many columns selected."
            );

            return;
        }

        List<ColumnUpdate> values =
                new ArrayList<>();

        Set<String> selectedColumns =
                new HashSet<>();

        for (int i = 1; i <= count; i++) {
            System.out.println(
                    "Column " + i + ":"
            );

            String column =
                    readInsertableColumn(
                            tableName
                    );

            if (column == null) {
                return;
            }

            if (!selectedColumns.add(column)) {
                AppLogger.error(
                        "The same column cannot be selected twice."
                );

                return;
            }

            System.out.print("Value: ");
            String value =
                    scanner.nextLine().trim();

            values.add(
                    new ColumnUpdate(
                            column,
                            value
                    )
            );
        }

        try {
            long id =
                    insertRepository.insertOne(
                            tableName,
                            values
                    );

            AppLogger.info(
                    "Row inserted successfully. Generated ID: "
                            + id
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid value for inserted row.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to insert the row.",
                    e
            );
        }
    }

    private void createOrder() {
        OrderInput order =
                readOrderInput(1);

        if (order == null) {
            return;
        }

        try {
            long orderId =
                    orderService.createOrder(order);

            AppLogger.info(
                    "Order created successfully. Order ID: "
                            + orderId
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid order data.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to create the order.",
                    e
            );
        }
    }

    private void insertMultipleRows() {
        String tableName = readTable();

        if (tableName == null) {
            return;
        }

        Integer columnCount =
                readPositiveInt(
                        "Number of columns: "
                );

        if (columnCount == null) {
            return;
        }

        if (columnCount >
                SqlIdentifierValidator
                        .getInsertableColumns(
                                tableName
                        )
                        .size()) {

            AppLogger.error(
                    "Too many columns selected."
            );

            return;
        }

        List<String> columns =
                new ArrayList<>();

        Set<String> selectedColumns =
                new HashSet<>();

        for (int i = 1;
             i <= columnCount;
             i++) {

            System.out.println(
                    "Column " + i + ":"
            );

            String column =
                    readInsertableColumn(
                            tableName
                    );

            if (column == null) {
                return;
            }

            if (!selectedColumns.add(column)) {
                AppLogger.error(
                        "The same column cannot be selected twice."
                );

                return;
            }

            columns.add(column);
        }

        Integer rowCount =
                readPositiveInt(
                        "Number of rows: "
                );

        if (rowCount == null) {
            return;
        }

        List<List<String>> rows =
                new ArrayList<>();

        for (int row = 1;
             row <= rowCount;
             row++) {

            System.out.println(
                    "Row " + row + ":"
            );

            List<String> values =
                    new ArrayList<>();

            for (String column : columns) {
                System.out.print(
                        column + ": "
                );

                values.add(
                        scanner.nextLine().trim()
                );
            }

            rows.add(values);
        }

        try {
            int inserted =
                    insertRepository.insertMany(
                            tableName,
                            columns,
                            rows
                    );

            AppLogger.info(
                    "Rows inserted successfully: "
                            + inserted
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid batch insert data.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to insert rows.",
                    e
            );
        }
    }

    private void createMultipleOrders() {
        Integer count =
                readPositiveInt(
                        "Number of orders: "
                );

        if (count == null) {
            return;
        }

        List<OrderInput> orders =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            OrderInput order =
                    readOrderInput(i);

            if (order == null) {
                return;
            }

            orders.add(order);
        }

        try {
            int inserted =
                    orderService.createOrders(
                            orders
                    );

            AppLogger.info(
                    "Orders created successfully: "
                            + inserted
            );

        } catch (IllegalArgumentException e) {
            AppLogger.error(
                    "Invalid order data.",
                    e
            );

        } catch (SQLException e) {
            AppLogger.error(
                    "Unable to create orders.",
                    e
            );
        }
    }

    private OrderInput readOrderInput(
            int number
    ) {
        System.out.println();
        System.out.println(
                "Order " + number + ":"
        );

        Long customerId =
                readPositiveLong(
                        "Customer ID: "
                );

        if (customerId == null) {
            return null;
        }

        System.out.print(
                "Status (NEW, PAID, SHIPPED, COMPLETED, CANCELLED): "
        );

        String status =
                scanner.nextLine()
                        .trim()
                        .toUpperCase();

        if (status.isBlank()) {
            status = "NEW";
        }

        Integer itemCount =
                readPositiveInt(
                        "Number of order items: "
                );

        if (itemCount == null) {
            return null;
        }

        List<OrderItemInput> items =
                new ArrayList<>();

        for (int i = 1;
             i <= itemCount;
             i++) {

            System.out.println(
                    "Item " + i + ":"
            );

            Long bookId =
                    readPositiveLong(
                            "Book ID: "
                    );

            if (bookId == null) {
                return null;
            }

            Integer quantity =
                    readPositiveInt(
                            "Quantity: "
                    );

            if (quantity == null) {
                return null;
            }

            System.out.print("Price: ");
            String priceValue =
                    scanner.nextLine().trim();

            BigDecimal price;

            try {
                price =
                        new BigDecimal(
                                priceValue
                        );

            } catch (NumberFormatException e) {
                AppLogger.error(
                        "Price must be a number."
                );

                return null;
            }

            items.add(
                    new OrderItemInput(
                            bookId,
                            quantity,
                            price
                    )
            );
        }

        return new OrderInput(
                customerId,
                status,
                items
        );
    }

    private String readTable() {
        printAvailableTables();

        System.out.print("Table: ");
        String tableName =
                scanner.nextLine().trim();

        if (!SqlIdentifierValidator
                .isAllowedTable(tableName)) {

            AppLogger.error(
                    "Unknown table."
            );

            return null;
        }

        return tableName;
    }

    private String readAllowedColumn(
            String tableName
    ) {
        printAvailableColumns(tableName);

        System.out.print("Column: ");
        String column =
                scanner.nextLine().trim();

        if (!SqlIdentifierValidator
                .isAllowedColumn(
                        tableName,
                        column
                )) {

            AppLogger.error(
                    "Unknown column."
            );

            return null;
        }

        return column;
    }

    private String readUpdatableColumn(
            String tableName
    ) {
        printUpdatableColumns(tableName);

        System.out.print("Column: ");
        String column =
                scanner.nextLine().trim();

        if (!SqlIdentifierValidator
                .isUpdatableColumn(
                        tableName,
                        column
                )) {

            AppLogger.error(
                    "Unknown or protected column."
            );

            return null;
        }

        return column;
    }

    private String readInsertableColumn(
            String tableName
    ) {
        printInsertableColumns(tableName);

        System.out.print("Column: ");
        String column =
                scanner.nextLine().trim();

        if (!SqlIdentifierValidator
                .isInsertableColumn(
                        tableName,
                        column
                )) {

            AppLogger.error(
                    "Unknown or protected column."
            );

            return null;
        }

        return column;
    }

    private Integer readPositiveInt(
            String prompt
    ) {
        System.out.print(prompt);

        try {
            int value =
                    Integer.parseInt(
                            scanner.nextLine().trim()
                    );

            if (value <= 0) {
                throw new NumberFormatException();
            }

            return value;

        } catch (NumberFormatException e) {
            AppLogger.error(
                    "Value must be a positive integer."
            );

            return null;
        }
    }

    private Long readPositiveLong(
            String prompt
    ) {
        System.out.print(prompt);

        try {
            long value =
                    Long.parseLong(
                            scanner.nextLine().trim()
                    );

            if (value <= 0) {
                throw new NumberFormatException();
            }

            return value;

        } catch (NumberFormatException e) {
            AppLogger.error(
                    "Value must be a positive integer."
            );

            return null;
        }
    }

    private void printAvailableTables() {
        System.out.println(
                "Available tables:"
        );

        SqlIdentifierValidator
                .getAllowedTables()
                .forEach(table ->
                        System.out.println(
                                "- " + table
                        )
                );
    }

    private void printAvailableColumns(
            String tableName
    ) {
        System.out.println(
                "Available columns:"
        );

        SqlIdentifierValidator
                .getAllowedColumns(
                        tableName
                )
                .forEach(column ->
                        System.out.println(
                                "- " + column
                        )
                );
    }

    private void printUpdatableColumns(
            String tableName
    ) {
        System.out.println(
                "Updatable columns:"
        );

        SqlIdentifierValidator
                .getUpdatableColumns(
                        tableName
                )
                .forEach(column ->
                        System.out.println(
                                "- " + column
                        )
                );
    }

    private void printInsertableColumns(
            String tableName
    ) {
        System.out.println(
                "Insertable columns:"
        );

        SqlIdentifierValidator
                .getInsertableColumns(
                        tableName
                )
                .forEach(column ->
                        System.out.println(
                                "- " + column
                        )
                );
    }
}