package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.ChatSession;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Manages active chat session state, message logging, and history export.
 */
public class ChatHistoryService {

    private ChatSession currentSession;

    public ChatHistoryService() {
        this.currentSession = new ChatSession();
    }

    public ChatSession getCurrentSession() {
        return currentSession;
    }

    public void recordMessage(ChatMessage message) {
        if (message != null) {
            currentSession.addMessage(message);
        }
    }

    public void startNewSession() {
        this.currentSession = new ChatSession();
    }

    public void clearHistory() {
        currentSession.clearMessages();
    }

    /**
     * Exports the current conversation history to a clean markdown or text file.
     */
    public void exportConversation(File targetFile) throws IOException {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write("# CodeAlpha AI Chatbot - Conversation Export");
            writer.newLine();
            writer.write("Session ID: " + currentSession.getSessionId());
            writer.newLine();
            writer.write("Session Started: " + currentSession.getStartTime().format(dtf));
            writer.newLine();
            writer.write("Total Messages: " + currentSession.getTotalMessageCount());
            writer.newLine();
            writer.write("--------------------------------------------------------------------------------");
            writer.newLine();
            writer.newLine();

            for (ChatMessage msg : currentSession.getMessages()) {
                String senderTag = msg.isUser() ? "[USER]" : (msg.isBot() ? "[AI BOT]" : "[SYSTEM]");
                writer.write(String.format("%s (%s)", senderTag, msg.getFormattedTime()));
                if (msg.isBot() && msg.getConfidenceScore() > 0) {
                    writer.write(String.format(" [Score: %.2f | %s]", msg.getConfidenceScore(), msg.getConfidenceLevel()));
                }
                writer.newLine();
                writer.write(msg.getContent());
                writer.newLine();
                writer.newLine();
            }

            writer.write("--------------------------------------------------------------------------------");
            writer.newLine();
            writer.write("Exported from CodeAlpha AI Chatbot | Developed as a CodeAlpha Internship Project");
            writer.newLine();
            writer.flush();
        }
    }
}
