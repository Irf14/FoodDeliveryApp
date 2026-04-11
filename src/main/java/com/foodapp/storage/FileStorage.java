package com.foodapp.storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file reading and writing.
 * This ensures "Data Storage/Retrieval" requirement is cleanly separated.
 */
public class FileStorage {

    private static final String DATA_DIR = "data/";

    public static void initialize() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Reads all lines from a given file.
     */
    public static List<String> readAllLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + filename + ": " + e.getMessage());
        }
        return lines;
    }

    /**
     * Writes all lines to a given file, overwriting existing content.
     */
    public static void writeAllLines(String filename, List<String> lines) {
        File file = new File(DATA_DIR + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing " + filename + ": " + e.getMessage());
        }
    }
}
