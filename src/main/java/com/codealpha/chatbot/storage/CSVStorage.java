package com.codealpha.chatbot.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust RFC 4180 compliant CSV Reader and Writer.
 * Correctly handles commas, enclosed quotes, double quotes (""), and multiline fields.
 */
public class CSVStorage {

    /**
     * Reads all records from a CSV file.
     */
    public List<List<String>> readCSV(File file) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (InputStream in = new FileInputStream(file)) {
            return readCSV(in);
        }
    }

    /**
     * Reads all records from an InputStream.
     */
    public List<List<String>> readCSV(InputStream inputStream) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> currentRecord = new ArrayList<>();
            StringBuilder currentField = new StringBuilder();
            boolean insideQuotes = false;
            int ch;

            while ((ch = reader.read()) != -1) {
                char c = (char) ch;

                if (insideQuotes) {
                    if (c == '"') {
                        // Check next character for escaped quote ("")
                        reader.mark(1);
                        int next = reader.read();
                        if (next == '"') {
                            currentField.append('"');
                        } else {
                            insideQuotes = false;
                            if (next != -1) {
                                reader.reset();
                            }
                        }
                    } else {
                        currentField.append(c);
                    }
                } else {
                    if (c == '"') {
                        insideQuotes = true;
                    } else if (c == ',') {
                        currentRecord.add(currentField.toString().trim());
                        currentField.setLength(0);
                    } else if (c == '\r') {
                        // Handle \r\n or standalone \r
                        reader.mark(1);
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            reader.reset();
                        }
                        currentRecord.add(currentField.toString().trim());
                        currentField.setLength(0);
                        if (!isRecordEmpty(currentRecord)) {
                            records.add(new ArrayList<>(currentRecord));
                        }
                        currentRecord.clear();
                    } else if (c == '\n') {
                        currentRecord.add(currentField.toString().trim());
                        currentField.setLength(0);
                        if (!isRecordEmpty(currentRecord)) {
                            records.add(new ArrayList<>(currentRecord));
                        }
                        currentRecord.clear();
                    } else {
                        currentField.append(c);
                    }
                }
            }

            // Flush remaining field/record
            if (currentField.length() > 0 || !currentRecord.isEmpty()) {
                currentRecord.add(currentField.toString().trim());
                if (!isRecordEmpty(currentRecord)) {
                    records.add(new ArrayList<>(currentRecord));
                }
            }
        }
        return records;
    }

    private boolean isRecordEmpty(List<String> record) {
        if (record.isEmpty()) return true;
        for (String field : record) {
            if (!field.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Writes records to a CSV file safely.
     */
    public void writeCSV(File file, List<List<String>> records) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (List<String> record : records) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < record.size(); i++) {
                    if (i > 0) line.append(",");
                    line.append(escapeField(record.get(i)));
                }
                writer.write(line.toString());
                writer.newLine();
            }
            writer.flush();
        }
    }

    /**
     * Escapes a field according to RFC 4180 rules.
     */
    public String escapeField(String field) {
        if (field == null) {
            return "";
        }
        boolean needsQuotes = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r");
        if (!needsQuotes) {
            return field;
        }

        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
