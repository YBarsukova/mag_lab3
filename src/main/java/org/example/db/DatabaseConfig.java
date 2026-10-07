package org.example.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record DatabaseConfig(
        String host,
        int port,
        String database
) {

    private static final String CONFIG_FILE =
            "application.properties";

    public static DatabaseConfig fromEnvironment() {
        Properties properties = loadProperties();

        String host = getEnvironmentOrDefault(
                "DB_HOST",
                properties.getProperty("db.host", "localhost")
        );

        String portValue = getEnvironmentOrDefault(
                "DB_PORT",
                properties.getProperty("db.port", "5432")
        );

        String database = getEnvironmentOrDefault(
                "DB_NAME",
                properties.getProperty("db.name", "bookstore")
        );

        int port;

        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid database port.",
                    e
            );
        }

        return new DatabaseConfig(
                host,
                port,
                database
        );
    }

    public String jdbcUrl() {
        return "jdbc:postgresql://%s:%d/%s"
                .formatted(host, port, database);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream input =
                     DatabaseConfig.class
                             .getClassLoader()
                             .getResourceAsStream(CONFIG_FILE)) {

            if (input != null) {
                properties.load(input);
            }

            return properties;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to read configuration.",
                    e
            );
        }
    }

    private static String getEnvironmentOrDefault(
            String name,
            String defaultValue
    ) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}