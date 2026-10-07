package org.example.repository;

import org.example.model.QueryFilter;
import org.example.security.SqlIdentifierValidator;
import org.example.security.SqlValueParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class SelectRepository {

    private final Connection connection;

    public SelectRepository(Connection connection) {
        this.connection = connection;
    }

    public void printAll(
            String tableName
    ) throws SQLException {

        validateTable(tableName);

        String sql =
                "SELECT * FROM " + tableName;

        try (
                Statement statement =
                        connection.createStatement();

                ResultSet resultSet =
                        statement.executeQuery(sql)
        ) {
            printResultSet(resultSet);
        }
    }

    public void printByFilter(
            String tableName,
            String columnName,
            String value
    ) throws SQLException {

        validateTable(tableName);
        validateColumn(
                tableName,
                columnName
        );

        String sql =
                "SELECT * FROM "
                        + tableName
                        + " WHERE "
                        + columnName
                        + " = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(
                    1,
                    SqlValueParser.parse(
                            tableName,
                            columnName,
                            value
                    )
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                printResultSet(resultSet);
            }
        }
    }

    public void printByFilters(
            String tableName,
            List<QueryFilter> filters
    ) throws SQLException {

        validateTable(tableName);

        if (filters == null || filters.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one filter is required."
            );
        }

        for (QueryFilter filter : filters) {
            validateColumn(
                    tableName,
                    filter.column()
            );
        }

        String conditions = filters.stream()
                .map(filter ->
                        filter.column() + " = ?"
                )
                .collect(
                        Collectors.joining(" AND ")
                );

        String sql =
                "SELECT * FROM "
                        + tableName
                        + " WHERE "
                        + conditions;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (int i = 0; i < filters.size(); i++) {
                QueryFilter filter =
                        filters.get(i);

                statement.setObject(
                        i + 1,
                        SqlValueParser.parse(
                                tableName,
                                filter.column(),
                                filter.value()
                        )
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                printResultSet(resultSet);
            }
        }
    }

    private void validateTable(
            String tableName
    ) {
        if (!SqlIdentifierValidator
                .isAllowedTable(tableName)) {

            throw new IllegalArgumentException(
                    "Unknown table."
            );
        }
    }

    private void validateColumn(
            String tableName,
            String columnName
    ) {
        if (!SqlIdentifierValidator
                .isAllowedColumn(
                        tableName,
                        columnName
                )) {

            throw new IllegalArgumentException(
                    "Unknown column."
            );
        }
    }

    private void printResultSet(
            ResultSet resultSet
    ) throws SQLException {

        ResultSetMetaData metaData =
                resultSet.getMetaData();

        int columnCount =
                metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            System.out.print(
                    metaData.getColumnName(i)
            );

            if (i < columnCount) {
                System.out.print(" | ");
            }
        }

        System.out.println();
        System.out.println("-".repeat(70));

        boolean hasRows = false;

        while (resultSet.next()) {
            hasRows = true;

            for (int i = 1; i <= columnCount; i++) {
                System.out.print(
                        resultSet.getObject(i)
                );

                if (i < columnCount) {
                    System.out.print(" | ");
                }
            }

            System.out.println();
        }

        if (!hasRows) {
            System.out.println(
                    "No records found."
            );
        }
    }
}