package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private final DatabaseConfig config;

    public DatabaseConnection(DatabaseConfig config) {
        this.config = config;
    }

    public Connection connect(String username, String password) throws SQLException {
        return DriverManager.getConnection(
                config.jdbcUrl(),
                username,
                password
        );
    }
}