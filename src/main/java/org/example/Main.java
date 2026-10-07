package org.example;

import org.example.cli.ConsoleApplication;
import org.example.db.DatabaseConfig;
import org.example.db.DatabaseConnection;
import org.example.logging.AppLogger;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (
                Terminal terminal = TerminalBuilder.builder()
                        .system(true)
                        .build()
        ) {
            LineReader credentialReader =
                    LineReaderBuilder.builder()
                            .terminal(terminal)
                            .build();

            String username = credentialReader
                    .readLine("Database login: ")
                    .trim();

            String password = credentialReader
                    .readLine(
                            "Database password: ",
                            '*'
                    );

            DatabaseConfig config =
                    DatabaseConfig.fromEnvironment();

            DatabaseConnection databaseConnection =
                    new DatabaseConnection(config);

            Scanner scanner = new Scanner(System.in);

            try (
                    Connection connection =
                            databaseConnection.connect(
                                    username,
                                    password
                            )
            ) {
                AppLogger.info(
                        "Connected to PostgreSQL successfully."
                );

                ConsoleApplication application =
                        new ConsoleApplication(
                                connection,
                                scanner
                        );

                application.run();

            } catch (SQLException e) {
                AppLogger.error(
                        "Unable to connect to the database. " +
                                "Please check login, password and database availability.",
                        e
                );
            }

        } catch (IOException e) {
            AppLogger.error(
                    "Unable to initialize console input.",
                    e
            );

        } catch (RuntimeException e) {
            AppLogger.error(
                    "Unable to start the application.",
                    e
            );
        }
    }
}