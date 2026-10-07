package org.example.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class AppLogger {

    private AppLogger() {
    }

    public static void info(String message) {
        System.out.println(message);
        writeToFile("INFO", message, null);
    }

    public static void error(String message) {
        System.err.println(message);
        writeToFile("ERROR", message, null);
    }

    public static void error(
            String message,
            Throwable throwable
    ) {
        System.err.println(message);
        writeToFile("ERROR", message, throwable);
    }

    private static void writeToFile(
            String level,
            String message,
            Throwable throwable
    ) {
        String logFilePath =
                System.getenv("LOG_FILE_PATH");

        if (logFilePath == null || logFilePath.isBlank()) {
            return;
        }

        Path path = Path.of(logFilePath);

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 path,
                                 StandardOpenOption.CREATE,
                                 StandardOpenOption.APPEND
                         )) {

                writer.write(
                        "%s [%s] %s%n"
                                .formatted(
                                        LocalDateTime.now(),
                                        level,
                                        message
                                )
                );

                if (throwable != null) {
                    StringWriter stringWriter =
                            new StringWriter();

                    throwable.printStackTrace(
                            new PrintWriter(stringWriter)
                    );

                    writer.write(stringWriter.toString());
                }

                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println(
                    "Unable to write application log file."
            );
        }
    }
}