package com.codealpha.chatbot.model;

/**
 * Enumeration of recognized user intents in natural language dialogues.
 */
public enum Intent {
    GREETING("Greeting"),
    FAREWELL("Farewell"),
    HELP("Help & Instructions"),
    CAPABILITIES("Bot Capabilities"),
    BOT_IDENTITY("Bot Identity & About"),
    CLEAR_CHAT("Clear Chat"),
    FAQ_QUERY("FAQ Knowledge Base Query"),
    FALLBACK("Fallback / Unrecognized");

    private final String description;

    Intent(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
