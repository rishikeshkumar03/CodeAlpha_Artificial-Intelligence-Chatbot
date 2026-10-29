package com.codealpha.chatbot.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVStorageTest {

    private CSVStorage csvStorage;

    @BeforeEach
    public void setUp() {
        csvStorage = new CSVStorage();
    }

    @Test
    public void testEscapeField() {
        assertEquals("simple", csvStorage.escapeField("simple"));
        assertEquals("\"hello, world\"", csvStorage.escapeField("hello, world"));
        assertEquals("\"quotes \"\"inside\"\"\"", csvStorage.escapeField("quotes \"inside\""));
        assertEquals("\"line1\nline2\"", csvStorage.escapeField("line1\nline2"));
    }

    @Test
    public void testWriteAndReadRecords(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("test_records.csv").toFile();

        List<List<String>> original = List.of(
                Arrays.asList("ID", "Category", "Question", "Answer"),
                Arrays.asList("FAQ-001", "AI", "What is AI?", "AI is artificial intelligence."),
                Arrays.asList("FAQ-002", "Java", "What is Java?", "Java is \"WORA\", general purpose."),
                Arrays.asList("FAQ-003", "Tech", "What is Docker?", "Docker uses containers,\nallowing easy deployment.")
        );

        csvStorage.writeCSV(file, original);
        assertTrue(file.exists());

        List<List<String>> read = csvStorage.readCSV(file);
        assertEquals(4, read.size());

        assertEquals("FAQ-001", read.get(1).get(0));
        assertEquals("AI", read.get(1).get(1));

        // Test quotes preservation
        assertEquals("Java is \"WORA\", general purpose.", read.get(2).get(3));

        // Test multiline preservation
        assertEquals("Docker uses containers,\nallowing easy deployment.", read.get(3).get(3));
    }
}
