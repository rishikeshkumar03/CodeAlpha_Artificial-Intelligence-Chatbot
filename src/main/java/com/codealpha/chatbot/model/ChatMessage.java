package com.codealpha.chatbot.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents an individual chat message in a conversation.
 */
public class ChatMessage {

    public enum Sender {
        USER,
        BOT,
        SYSTEM
    }

    public enum ConfidenceLevel {
        HIGH,
        MEDIUM,
        LOW,
        NONE
    }

    private final String id;
    private final Sender sender;
    private final String content;
    private final LocalDateTime timestamp;
    private double confidenceScore;
    private ConfidenceLevel confidenceLevel;
    private String matchedFaqId;
    private String category;

    public ChatMessage(Sender sender, String content) {
        this(UUID.randomUUID().toString(), sender, content, LocalDateTime.now(), 0.0, ConfidenceLevel.NONE, null, null);
    }

    public ChatMessage(Sender sender, String content, double confidenceScore, ConfidenceLevel confidenceLevel, String matchedFaqId, String category) {
        this(UUID.randomUUID().toString(), sender, content, LocalDateTime.now(), confidenceScore, confidenceLevel, matchedFaqId, category);
    }

    public ChatMessage(String id, Sender sender, String content, LocalDateTime timestamp,
                       double confidenceScore, ConfidenceLevel confidenceLevel,
                       String matchedFaqId, String category) {
        this.id = id;
        this.sender = sender;
        this.content = content != null ? content : "";
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.confidenceScore = confidenceScore;
        this.confidenceLevel = confidenceLevel != null ? confidenceLevel : ConfidenceLevel.NONE;
        this.matchedFaqId = matchedFaqId;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public Sender getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return timestamp.format(formatter);
    }

    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getMatchedFaqId() {
        return matchedFaqId;
    }

    public void setMatchedFaqId(String matchedFaqId) {
        this.matchedFaqId = matchedFaqId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isUser() {
        return sender == Sender.USER;
    }

    public boolean isBot() {
        return sender == Sender.BOT;
    }

    public boolean isSystem() {
        return sender == Sender.SYSTEM;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), sender, content);
    }
}
