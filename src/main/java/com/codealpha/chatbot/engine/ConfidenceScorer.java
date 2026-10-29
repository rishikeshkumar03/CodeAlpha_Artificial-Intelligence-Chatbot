package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.MatchResult;

import java.util.Comparator;
import java.util.List;

/**
 * Evaluates, thresholds, and performs deterministic tie-breaking on similarity scores.
 */
public class ConfidenceScorer {

    public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.45;
    public static final double HIGH_CONFIDENCE_THRESHOLD = 0.75;
    public static final double MEDIUM_CONFIDENCE_THRESHOLD = 0.45;

    private double threshold;

    public ConfidenceScorer() {
        this.threshold = DEFAULT_CONFIDENCE_THRESHOLD;
    }

    public ConfidenceScorer(double threshold) {
        setThreshold(threshold);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        if (threshold >= 0.0 && threshold <= 1.0) {
            this.threshold = threshold;
        } else {
            this.threshold = DEFAULT_CONFIDENCE_THRESHOLD;
        }
    }

    /**
     * Checks if the match score meets or exceeds the acceptance threshold.
     */
    public boolean isConfident(MatchResult result) {
        return result != null && result.hasMatch() && result.getScore() >= threshold;
    }

    /**
     * Evaluates the qualitative confidence level for a given score.
     */
    public ChatMessage.ConfidenceLevel evaluateLevel(double score) {
        if (score >= HIGH_CONFIDENCE_THRESHOLD) {
            return ChatMessage.ConfidenceLevel.HIGH;
        } else if (score >= MEDIUM_CONFIDENCE_THRESHOLD) {
            return ChatMessage.ConfidenceLevel.MEDIUM;
        } else if (score > 0.0) {
            return ChatMessage.ConfidenceLevel.LOW;
        }
        return ChatMessage.ConfidenceLevel.NONE;
    }

    /**
     * Selects the single best match from candidate matches with deterministic tie-breaking.
     */
    public MatchResult selectBestMatch(List<MatchResult> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return MatchResult.noMatch("No candidate matches evaluated");
        }

        // Deterministic comparator:
        // 1. Highest score
        // 2. Highest FAQ weight
        // 3. Lower FAQ ID (lexicographical)
        Comparator<MatchResult> comparator = (m1, m2) -> {
            int scoreComp = Double.compare(m2.getScore(), m1.getScore());
            if (scoreComp != 0) return scoreComp;

            double w1 = m1.getFaq() != null ? m1.getFaq().getWeight() : 1.0;
            double w2 = m2.getFaq() != null ? m2.getFaq().getWeight() : 1.0;
            int weightComp = Double.compare(w2, w1);
            if (weightComp != 0) return weightComp;

            String id1 = m1.getFaq() != null ? m1.getFaq().getId() : "";
            String id2 = m2.getFaq() != null ? m2.getFaq().getId() : "";
            return id1.compareTo(id2);
        };

        return candidates.stream().min(comparator).orElse(MatchResult.noMatch("No match selected"));
    }
}
