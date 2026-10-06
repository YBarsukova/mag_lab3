package org.example;

import org.example.db.DatabaseConfig;
import org.example.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        DatabaseConnection databaseConnection = new DatabaseConnection(config);

        System.out.print("Database login: ");
        String username = scanner.nextLine();

        System.out.print("Database password: ");
        String password = scanner.nextLine();

        try (Connection connection = databaseConnection.connect(username, password)) {
            System.out.println("Connected to PostgreSQL successfully.");
        } catch (SQLException e) {
            System.err.println(
                    "Unable to connect to the database. " +
                            "Please check login, password and database availability."
            );
        }
    }
}