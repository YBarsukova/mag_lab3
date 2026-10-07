package org.example.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlIdentifierValidator {

    private static final Map<String, Set<String>>
            ALLOWED_COLUMNS = Map.of(
            "authors", columns(
                    "id",
                    "name",
                    "country"
            ),
            "books", columns(
                    "id",
                    "title",
                    "author_id",
                    "price",
                    "stock"
            ),
            "customers", columns(
                    "id",
                    "name",
                    "email"
            ),
            "orders", columns(
                    "id",
                    "customer_id",
                    "created_at",
                    "status"
            ),
            "order_items", columns(
                    "id",
                    "order_id",
                    "book_id",
                    "quantity",
                    "price"
            )
    );

    private SqlIdentifierValidator() {
    }

    public static boolean isAllowedTable(
            String tableName
    ) {
        return ALLOWED_COLUMNS.containsKey(tableName);
    }

    public static boolean isAllowedColumn(
            String tableName,
            String columnName
    ) {
        Set<String> columns =
                ALLOWED_COLUMNS.get(tableName);

        return columns != null
                && columns.contains(columnName);
    }

    public static boolean isUpdatableColumn(
            String tableName,
            String columnName
    ) {
        return isAllowedColumn(
                tableName,
                columnName
        ) && !"id".equals(columnName);
    }

    public static boolean isInsertableColumn(
            String tableName,
            String columnName
    ) {
        return isAllowedColumn(
                tableName,
                columnName
        ) && !"id".equals(columnName);
    }

    public static Set<String> getAllowedTables() {
        return ALLOWED_COLUMNS.keySet();
    }

    public static Set<String> getAllowedColumns(
            String tableName
    ) {
        return ALLOWED_COLUMNS.getOrDefault(
                tableName,
                Set.of()
        );
    }

    public static Set<String> getUpdatableColumns(
            String tableName
    ) {
        return withoutId(
                getAllowedColumns(tableName)
        );
    }

    public static Set<String> getInsertableColumns(
            String tableName
    ) {
        return withoutId(
                getAllowedColumns(tableName)
        );
    }

    private static Set<String> withoutId(
            Set<String> source
    ) {
        LinkedHashSet<String> result =
                new LinkedHashSet<>(source);

        result.remove("id");

        return result;
    }

    private static Set<String> columns(
            String... names
    ) {
        return Collections.unmodifiableSet(
                new LinkedHashSet<>(
                        List.of(names)
                )
        );
    }
}