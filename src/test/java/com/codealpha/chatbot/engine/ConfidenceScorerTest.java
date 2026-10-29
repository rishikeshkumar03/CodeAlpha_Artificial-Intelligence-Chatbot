package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.model.MatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfidenceScorerTest {

    private ConfidenceScorer scorer;

    @BeforeEach
    public void setUp() {
        scorer = new ConfidenceScorer();
    }

    @Test
    public void testThresholding() {
        FAQ faq = new FAQ("FAQ-001", "What is Java?", "Java is OOP language", "Java");

        MatchResult high = new MatchResult(faq, 0.85, ChatMessage.ConfidenceLevel.HIGH, "What is Java?", "Exact", "High match");
        assertTrue(scorer.isConfident(high));

        MatchResult medium = new MatchResult(faq, 0.50, ChatMessage.ConfidenceLevel.MEDIUM, "What is Java?", "Token", "Medium match");
        assertTrue(scorer.isConfident(medium));

        MatchResult low = new MatchResult(faq, 0.30, ChatMessage.ConfidenceLevel.LOW, "What is Java?", "None", "Low match");
        assertFalse(scorer.isConfident(low));
    }

    @Test
    public void testTieBreakingPriority() {
        FAQ faqLowWeight = new FAQ("FAQ-001", "What is Java?", List.of(), List.of(), "Java", "Ans 1", 1.0);
        FAQ faqHighWeight = new FAQ("FAQ-002", "What is Java?", List.of(), List.of(), "Java", "Ans 2", 1.5);

        MatchResult m1 = new MatchResult(faqLowWeight, 0.80, ChatMessage.ConfidenceLevel.HIGH, "What is Java?", "Exact", "Score 0.8");
        MatchResult m2 = new MatchResult(faqHighWeight, 0.80, ChatMessage.ConfidenceLevel.HIGH, "What is Java?", "Exact", "Score 0.8");

        MatchResult best = scorer.selectBestMatch(List.of(m1, m2));
        assertEquals("FAQ-002", best.getFaq().getId(), "Tie-breaker should prefer higher FAQ weight");
    }

    @Test
    public void testConfidenceLevels() {
        assertEquals(ChatMessage.ConfidenceLevel.HIGH, scorer.evaluateLevel(0.80));
        assertEquals(ChatMessage.ConfidenceLevel.MEDIUM, scorer.evaluateLevel(0.55));
        assertEquals(ChatMessage.ConfidenceLevel.LOW, scorer.evaluateLevel(0.30));
        assertEquals(ChatMessage.ConfidenceLevel.NONE, scorer.evaluateLevel(0.0));
    }
}
