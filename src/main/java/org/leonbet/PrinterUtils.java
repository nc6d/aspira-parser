package org.leonbet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PrinterUtils {

    private static BufferedWriter writer;

    public static void printToConsole(int indentLevel, String message) {
        System.out.println(indent(indentLevel) + message);
    }


    public static void printToFile(int indentLevel, String message) {
        try {
            if (writer != null) {
                writer.write(indent(indentLevel) + message);
                writer.newLine();
            } else {
                throw new IllegalStateException("File writer not initialized");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file", e);
        }
    }

    public static void print(int indentLevel, String message, boolean toFile) {
        if (toFile) {
            printToFile(indentLevel, message);
        } else {
            printToConsole(indentLevel, message);
        }
    }

    public static void initFile(String filePath) {
        try {
            writer = new BufferedWriter(new FileWriter(filePath, false));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file for writing", e);
        }
    }

    public static void closeFile() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                // Log or ignore
            }
        }
    }


    private static String indent(int level) {
        return "\t".repeat(level);
    }

}
