package com.codealpha.chatbot;

import com.codealpha.chatbot.engine.ChatbotEngine;
import com.codealpha.chatbot.gui.ChatbotFrame;
import com.codealpha.chatbot.service.ChatHistoryService;
import com.codealpha.chatbot.service.FAQService;
import com.codealpha.chatbot.storage.CSVStorage;
import com.codealpha.chatbot.storage.FAQRepository;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main application entry point for CodeAlpha AI Chatbot.
 */
public class Main {

    public static final String DATA_DIRECTORY = "data";
    public static final String FAQS_FILE_PATH = "data/faqs.csv";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  CodeAlpha AI Chatbot");
        System.out.println("  Java-based Artificial Intelligence Chatbot");
        System.out.println("  Developed as a CodeAlpha Internship Project");
        System.out.println("=================================================");

        // 1. Ensure local data directory exists
        File dataDir = new File(DATA_DIRECTORY);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            System.out.println("Data directory created: " + created);
        }

        // 2. Initialize Repositories and Services
        File faqsFile = new File(FAQS_FILE_PATH);
        CSVStorage csvStorage = new CSVStorage();
        FAQRepository faqRepository = new FAQRepository(faqsFile, csvStorage);

        try {
            faqRepository.initialize();
            System.out.println("Knowledge base initialized with " + faqRepository.findAll().size() + " FAQs.");
        } catch (Exception e) {
            System.err.println("Warning: Error initializing FAQ repository: " + e.getMessage());
        }

        FAQService faqService = new FAQService(faqRepository);
        ChatHistoryService historyService = new ChatHistoryService();
        ChatbotEngine engine = new ChatbotEngine(faqService);

        // 3. Launch GUI on Swing Event Dispatch Thread
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in Headless mode (CLI interactive console fallback)");
            runHeadlessCli(engine);
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Use system UI properties where possible
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {}

                ChatbotFrame frame = new ChatbotFrame(engine, faqService, historyService);
                frame.setVisible(true);
            });
        }
    }

    private static void runHeadlessCli(ChatbotEngine engine) {
        System.out.println("Headless mode initialized. Type 'exit' to quit.");
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (true) {
                System.out.print("\nYou: ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                }
                var response = engine.processQuery(line);
                System.out.println("Bot: " + response.getContent());
                if (response.getConfidenceScore() > 0) {
                    System.out.printf("[Score: %.2f | Level: %s]\n", response.getConfidenceScore(), response.getConfidenceLevel());
                }
            }
        }
    }
}
