package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.model.MatchResult;

import java.util.List;
import java.util.Random;

/**
 * Generates user-facing responses for standard intents, FAQ matches, and helpful fallbacks.
 */
public class ResponseGenerator {

    private final Random random = new Random();

    private static final String[] GREETING_RESPONSES = {
            "Hello! I am your CodeAlpha AI Assistant. How can I help you today?",
            "Hi there! Welcome to CodeAlpha AI Chatbot. Ask me anything about Java, AI, or Computer Science!",
            "Greetings! I am ready to help. What topic would you like to explore?",
            "Hey! Feel free to ask me questions on programming fundamentals, technology, or Java concepts."
    };

    private static final String[] FAREWELL_RESPONSES = {
            "Goodbye! Have a productive coding session. Feel free to come back whenever you have questions!",
            "Take care! Best of luck with your software development and learning journey.",
            "See you soon! Happy coding!"
    };

    public ChatMessage generateGreetingResponse() {
        String msg = GREETING_RESPONSES[random.nextInt(GREETING_RESPONSES.length)];
        return new ChatMessage(ChatMessage.Sender.BOT, msg, 1.0, ChatMessage.ConfidenceLevel.HIGH, null, "Greetings");
    }

    public ChatMessage generateFarewellResponse() {
        String msg = FAREWELL_RESPONSES[random.nextInt(FAREWELL_RESPONSES.length)];
        return new ChatMessage(ChatMessage.Sender.BOT, msg, 1.0, ChatMessage.ConfidenceLevel.HIGH, null, "Greetings");
    }

    public ChatMessage generateHelpResponse() {
        String help = "Here is how you can use the CodeAlpha AI Chatbot:\n\n" +
                "• Ask natural language questions about Java, AI, Data Structures, OOP, or Tech.\n" +
                "• Try alternative phrasings (e.g., 'What is AI?' or 'Explain artificial intelligence').\n" +
                "• Open the FAQ Knowledge Base drawer to view, add, edit, or search FAQs.\n" +
                "• Click on any Suggested Question chip to quickly get instant answers.\n" +
                "• Use 'Clear Chat' or 'New Chat' in the top header to reset the conversation.\n" +
                "• Use 'Export Chat' to save your conversation history.\n\n" +
                "Try asking: 'What is Java?' or 'What are the pillars of OOP?'";
        return new ChatMessage(ChatMessage.Sender.BOT, help, 1.0, ChatMessage.ConfidenceLevel.HIGH, null, "Application Help");
    }

    public ChatMessage generateCapabilitiesResponse(int totalFaqs) {
        String capabilities = "I am a local, explainable NLP-powered AI Chatbot built in Java for CodeAlpha!\n\n" +
                "Currently, I know about " + totalFaqs + " frequently asked topics across:\n" +
                "• Artificial Intelligence & Machine Learning\n" +
                "• Java Programming & JVM Internals\n" +
                "• Programming Fundamentals & Algorithms\n" +
                "• Object-Oriented Programming (OOP)\n" +
                "• Computer Science Concepts\n" +
                "• Modern Technology & Tools\n\n" +
                "You can also train me dynamically by adding new questions and answers in the FAQ Manager!";
        return new ChatMessage(ChatMessage.Sender.BOT, capabilities, 1.0, ChatMessage.ConfidenceLevel.HIGH, null, "General Knowledge");
    }

    public ChatMessage generateIdentityResponse() {
        String identity = "I am the CodeAlpha AI Chatbot!\n\n" +
                "• Developed as a CodeAlpha Internship Project\n" +
                "• Technology: 100% Java with Java Swing and custom local NLP\n" +
                "• Mode: Fully offline, privacy-first, zero cloud dependencies\n" +
                "• Purpose: Interactive AI learning and intelligent FAQ question answering\n\n" +
                "Internship Project at CodeAlpha | © 2026 CodeAlpha. All Rights Reserved.";
        return new ChatMessage(ChatMessage.Sender.BOT, identity, 1.0, ChatMessage.ConfidenceLevel.HIGH, null, "General Knowledge");
    }

    public ChatMessage generateFaqResponse(MatchResult match) {
        FAQ faq = match.getFaq();
        String answer = faq.getAnswer();
        return new ChatMessage(
                ChatMessage.Sender.BOT,
                answer,
                match.getScore(),
                match.getConfidenceLevel(),
                faq.getId(),
                faq.getCategory()
        );
    }

    public ChatMessage generateFallbackResponse(String userQuery, List<MatchResult> closeCandidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("I'm sorry, I don't have enough confidence to answer that question accurately yet.\n\n");

        if (closeCandidates != null && !closeCandidates.isEmpty()) {
            sb.append("Did you mean one of these topics?\n");
            int count = 0;
            for (MatchResult candidate : closeCandidates) {
                if (candidate.hasMatch() && count < 3) {
                    sb.append("• ").append(candidate.getFaq().getPrimaryQuestion()).append("\n");
                    count++;
                }
            }
            sb.append("\n");
        }

        sb.append("Tip: You can rephrase your question, click a suggested question on the right, or teach me this answer using the FAQ Manager!");

        return new ChatMessage(
                ChatMessage.Sender.BOT,
                sb.toString(),
                closeCandidates != null && !closeCandidates.isEmpty() ? closeCandidates.get(0).getScore() : 0.0,
                ChatMessage.ConfidenceLevel.LOW,
                null,
                "Fallback"
        );
    }

    public ChatMessage generateEmptyInputResponse() {
        return new ChatMessage(
                ChatMessage.Sender.BOT,
                "Please type a question or message so I can assist you!",
                0.0,
                ChatMessage.ConfidenceLevel.NONE,
                null,
                "System"
        );
    }
}
