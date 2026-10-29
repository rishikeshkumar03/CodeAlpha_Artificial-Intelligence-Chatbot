package com.codealpha.chatbot.model;

/**
 * Encapsulates the evaluation result of matching a user query against the FAQ knowledge base.
 */
public class MatchResult {

    private final FAQ faq;
    private final double score;
    private final ChatMessage.ConfidenceLevel confidenceLevel;
    private final String matchedPhrase;
    private final String matchStrategy;
    private final String explanation;

    public MatchResult(FAQ faq, double score, ChatMessage.ConfidenceLevel confidenceLevel,
                       String matchedPhrase, String matchStrategy, String explanation) {
        this.faq = faq;
        this.score = score;
        this.confidenceLevel = confidenceLevel != null ? confidenceLevel : ChatMessage.ConfidenceLevel.NONE;
        this.matchedPhrase = matchedPhrase != null ? matchedPhrase : "";
        this.matchStrategy = matchStrategy != null ? matchStrategy : "None";
        this.explanation = explanation != null ? explanation : "";
    }

    public static MatchResult noMatch(String explanation) {
        return new MatchResult(null, 0.0, ChatMessage.ConfidenceLevel.NONE, "", "None", explanation);
    }

    public boolean hasMatch() {
        return faq != null && score > 0.0;
    }

    public FAQ getFaq() {
        return faq;
    }

    public double getScore() {
        return score;
    }

    public ChatMessage.ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    public String getMatchedPhrase() {
        return matchedPhrase;
    }

    public String getMatchStrategy() {
        return matchStrategy;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return String.format("MatchResult{faq=%s, score=%.2f, level=%s, strategy='%s'}",
                faq != null ? faq.getId() : "None", score, confidenceLevel, matchStrategy);
    }
}
