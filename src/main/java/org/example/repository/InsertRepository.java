package org.example.repository;

import org.example.model.ColumnUpdate;
import org.example.security.SqlIdentifierValidator;
import org.example.security.SqlValueParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InsertRepository {

    private final Connection connection;

    public InsertRepository(Connection connection) {
        this.connection = connection;
    }

    public long insertOne(
            String tableName,
            List<ColumnUpdate> values
    ) throws SQLException {

        validateTable(tableName);
        validateValues(tableName, values);

        String columns = values.stream()
                .map(ColumnUpdate::column)
                .collect(
                        Collectors.joining(", ")
                );

        String placeholders = values.stream()
                .map(value -> "?")
                .collect(
                        Collectors.joining(", ")
                );

        String sql =
                "INSERT INTO "
                        + tableName
                        + " ("
                        + columns
                        + ") VALUES ("
                        + placeholders
                        + ") RETURNING id";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (int i = 0; i < values.size(); i++) {
                ColumnUpdate value =
                        values.get(i);

                statement.setObject(
                        i + 1,
                        SqlValueParser.parse(
                                tableName,
                                value.column(),
                                value.value()
                        )
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    throw new SQLException(
                            "Database did not return generated ID."
                    );
                }

                return resultSet.getLong(1);
            }
        }
    }

    public int insertMany(
            String tableName,
            List<String> columns,
            List<List<String>> rows
    ) throws SQLException {

        validateTable(tableName);

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException(
                    "Columns are required."
            );
        }

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Rows are required."
            );
        }

        Set<String> uniqueColumns =
                new HashSet<>();

        for (String column : columns) {
            if (!SqlIdentifierValidator
                    .isInsertableColumn(
                            tableName,
                            column
                    )) {

                throw new IllegalArgumentException(
                        "Column cannot be inserted."
                );
            }

            if (!uniqueColumns.add(column)) {
                throw new IllegalArgumentException(
                        "Duplicate column."
                );
            }
        }

        for (List<String> row : rows) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException(
                        "Invalid number of values."
                );
            }
        }

        String columnList =
                String.join(", ", columns);

        String placeholders =
                columns.stream()
                        .map(column -> "?")
                        .collect(
                                Collectors.joining(", ")
                        );

        String sql =
                "INSERT INTO "
                        + tableName
                        + " ("
                        + columnList
                        + ") VALUES ("
                        + placeholders
                        + ")";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (List<String> row : rows) {

                for (int i = 0;
                     i < columns.size();
                     i++) {

                    statement.setObject(
                            i + 1,
                            SqlValueParser.parse(
                                    tableName,
                                    columns.get(i),
                                    row.get(i)
                            )
                    );
                }

                statement.addBatch();
            }

            statement.executeBatch();

            return rows.size();
        }
    }

    private void validateValues(
            String tableName,
            List<ColumnUpdate> values
    ) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                    "Values are required."
            );
        }

        Set<String> columns =
                new HashSet<>();

        for (ColumnUpdate value : values) {

            if (!SqlIdentifierValidator
                    .isInsertableColumn(
                            tableName,
                            value.column()
                    )) {

                throw new IllegalArgumentException(
                        "Column cannot be inserted."
                );
            }

            if (!columns.add(value.column())) {
                throw new IllegalArgumentException(
                        "Duplicate column."
                );
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
}