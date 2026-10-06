package org.example.db;

public record DatabaseConfig(
        String host,
        int port,
        String database
) {

    public static DatabaseConfig fromEnvironment() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        int port = Integer.parseInt(
                System.getenv().getOrDefault("DB_PORT", "5432")
        );
        String database = System.getenv().getOrDefault("DB_NAME", "bookstore");

        return new DatabaseConfig(host, port, database);
    }

    public String jdbcUrl() {
        return "jdbc:postgresql://%s:%d/%s"
                .formatted(host, port, database);
    }
}