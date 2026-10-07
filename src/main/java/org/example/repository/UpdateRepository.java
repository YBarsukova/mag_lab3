package org.example.repository;

import org.example.model.ColumnUpdate;
import org.example.security.SqlIdentifierValidator;
import org.example.security.SqlValueParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateRepository {

    private final Connection connection;

    public UpdateRepository(Connection connection) {
        this.connection = connection;
    }

    public int updateOne(
            String tableName,
            long id,
            List<ColumnUpdate> updates
    ) throws SQLException {

        validateTable(tableName);

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one update is required."
            );
        }

        Set<String> columns = new HashSet<>();

        for (ColumnUpdate update : updates) {
            validateUpdatableColumn(
                    tableName,
                    update.column()
            );

            if (!columns.add(update.column())) {
                throw new IllegalArgumentException(
                        "Duplicate column."
                );
            }
        }

        String setClause = updates.stream()
                .map(update ->
                        update.column() + " = ?"
                )
                .collect(
                        Collectors.joining(", ")
                );

        String sql =
                "UPDATE "
                        + tableName
                        + " SET "
                        + setClause
                        + " WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (int i = 0; i < updates.size(); i++) {
                ColumnUpdate update =
                        updates.get(i);

                statement.setObject(
                        i + 1,
                        SqlValueParser.parse(
                                tableName,
                                update.column(),
                                update.value()
                        )
                );
            }

            statement.setLong(
                    updates.size() + 1,
                    id
            );

            return statement.executeUpdate();
        }
    }

    public int updateMany(
            String tableName,
            String updateColumn,
            String newValue,
            String filterColumn,
            List<String> filterValues
    ) throws SQLException {

        validateTable(tableName);

        validateUpdatableColumn(
                tableName,
                updateColumn
        );

        if (!SqlIdentifierValidator.isAllowedColumn(
                tableName,
                filterColumn
        )) {
            throw new IllegalArgumentException(
                    "Unknown filter column."
            );
        }

        if (filterValues == null
                || filterValues.isEmpty()) {

            throw new IllegalArgumentException(
                    "Filter values are required."
            );
        }

        String placeholders =
                filterValues.stream()
                        .map(value -> "?")
                        .collect(
                                Collectors.joining(", ")
                        );

        String sql =
                "UPDATE "
                        + tableName
                        + " SET "
                        + updateColumn
                        + " = ? WHERE "
                        + filterColumn
                        + " IN ("
                        + placeholders
                        + ")";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setObject(
                    1,
                    SqlValueParser.parse(
                            tableName,
                            updateColumn,
                            newValue
                    )
            );

            for (int i = 0;
                 i < filterValues.size();
                 i++) {

                statement.setObject(
                        i + 2,
                        SqlValueParser.parse(
                                tableName,
                                filterColumn,
                                filterValues.get(i)
                        )
                );
            }

            return statement.executeUpdate();
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

    private void validateUpdatableColumn(
            String tableName,
            String columnName
    ) {
        if (!SqlIdentifierValidator
                .isUpdatableColumn(
                        tableName,
                        columnName
                )) {

            throw new IllegalArgumentException(
                    "Column cannot be updated."
            );
        }
    }
}