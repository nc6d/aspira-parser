package org.leonbet.util;

import org.leonbet.config.AppConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PrinterUtils {
    private static PrintWriter fileWriter;
    private static final String INDENT = "\t";
    private static final Object lock = new Object();
    
    private PrinterUtils() {} // Prevent instantiation

    public static void initFile(String fileName) {
        try {
            Path reportsPath = Paths.get(AppConfig.getReportsDir());
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }

            Path reportFile = reportsPath.resolve(fileName);
            fileWriter = new PrintWriter(new FileWriter(reportFile.toFile()));
        } catch (IOException e) {
            System.err.println("Failed to create output file: " + e.getMessage());
        }
    }

    public static void closeFile() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    public static void print(int indentLevel, String message) {
        String indentedMessage = INDENT.repeat(indentLevel) + message;
        synchronized (lock) {
            System.out.println(indentedMessage);
            
            if (AppConfig.isPrintToFile() && fileWriter != null) {
                fileWriter.println(indentedMessage);
                fileWriter.flush();
            }
        }
    }
} 