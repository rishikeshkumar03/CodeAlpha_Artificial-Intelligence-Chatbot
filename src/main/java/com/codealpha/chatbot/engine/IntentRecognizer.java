package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.Intent;
import com.codealpha.chatbot.nlp.TextNormalizer;

import java.util.Arrays;
import java.util.List;

/**
 * Classifies user input into communicative intents using linguistic pattern recognition.
 */
public class IntentRecognizer {

    private final TextNormalizer normalizer;

    private static final List<String> GREETING_PATTERNS = Arrays.asList(
            "hi", "hello", "hey", "heya", "howdy", "good morning", "good afternoon",
            "good evening", "greetings", "sup", "what is up", "whats up", "hiya"
    );

    private static final List<String> FAREWELL_PATTERNS = Arrays.asList(
            "bye", "goodbye", "see you", "see ya", "farewell", "cya", "have a good day",
            "take care", "exit", "quit"
    );

    private static final List<String> HELP_PATTERNS = Arrays.asList(
            "help", "how to use", "need help", "can you help me", "instructions",
            "user guide", "how does this work", "how do i use this"
    );

    private static final List<String> CAPABILITY_PATTERNS = Arrays.asList(
            "what can you do", "features", "capabilities", "what are your features",
            "what do you know", "what topics do you cover", "what questions can i ask"
    );

    private static final List<String> IDENTITY_PATTERNS = Arrays.asList(
            "who are you", "what is your name", "what are you", "who made you",
            "who created you", "tell me about yourself", "are you an ai", "are you a bot"
    );

    private static final List<String> CLEAR_PATTERNS = Arrays.asList(
            "clear chat", "clear conversation", "reset chat", "start over", "new chat"
    );

    public IntentRecognizer() {
        this.normalizer = new TextNormalizer();
    }

    public IntentRecognizer(TextNormalizer normalizer) {
        this.normalizer = normalizer != null ? normalizer : new TextNormalizer();
    }

    /**
     * Determines user intent from the normalized input text.
     */
    public Intent recognizeIntent(String rawText) {
        String normalized = normalizer.normalize(rawText);

        if (normalized.isEmpty()) {
            return Intent.FALLBACK;
        }

        // Direct exact or close matches on common intents
        if (matchesAny(normalized, CLEAR_PATTERNS)) {
            return Intent.CLEAR_CHAT;
        }

        if (matchesAny(normalized, IDENTITY_PATTERNS)) {
            return Intent.BOT_IDENTITY;
        }

        if (matchesAny(normalized, HELP_PATTERNS)) {
            return Intent.HELP;
        }

        if (matchesAny(normalized, CAPABILITY_PATTERNS)) {
            return Intent.CAPABILITIES;
        }

        if (matchesAny(normalized, FAREWELL_PATTERNS)) {
            return Intent.FAREWELL;
        }

        if (matchesAny(normalized, GREETING_PATTERNS)) {
            return Intent.GREETING;
        }

        // Otherwise treat as FAQ question to be answered by the knowledge base
        return Intent.FAQ_QUERY;
    }

    private boolean matchesAny(String text, List<String> patterns) {
        for (String pattern : patterns) {
            String normPattern = normalizer.normalize(pattern);
            if (text.equals(normPattern) || text.startsWith(normPattern + " ") || text.endsWith(" " + normPattern)) {
                return true;
            }
        }
        return false;
    }
}
