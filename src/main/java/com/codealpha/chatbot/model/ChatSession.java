package com.codealpha.chatbot.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates a complete chat session with message history and session statistics.
 */
public class ChatSession {

    private final String sessionId;
    private final LocalDateTime startTime;
    private final List<ChatMessage> messages;

    public ChatSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
        this.messages = new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(ChatMessage message) {
        if (message != null) {
            messages.add(message);
        }
    }

    public void clearMessages() {
        messages.clear();
    }

    public int getTotalMessageCount() {
        return messages.size();
    }

    public long getUserMessageCount() {
        return messages.stream().filter(ChatMessage::isUser).count();
    }

    public long getBotMessageCount() {
        return messages.stream().filter(ChatMessage::isBot).count();
    }

    public double getAverageConfidence() {
        List<ChatMessage> botMessages = messages.stream()
                .filter(m -> m.isBot() && m.getConfidenceScore() > 0)
                .toList();
        if (botMessages.isEmpty()) {
            return 0.0;
        }
        double sum = botMessages.stream().mapToDouble(ChatMessage::getConfidenceScore).sum();
        return sum / botMessages.size();
    }
}
