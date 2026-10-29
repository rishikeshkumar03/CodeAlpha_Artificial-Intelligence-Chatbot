package com.codealpha.chatbot.nlp;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.model.MatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SimilarityMatcherTest {

    private SimilarityMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new SimilarityMatcher();
    }

    @Test
    public void testJaccardSimilarity() {
        Set<String> setA = Set.of("java", "programming", "language");
        Set<String> setB = Set.of("java", "language");
        double similarity = matcher.calculateJaccardSimilarity(setA, setB);
        // intersection: 2 ("java", "language"), union: 3 -> 2/3 ≈ 0.666
        assertEquals(2.0 / 3.0, similarity, 0.001);
    }

    @Test
    public void testLevenshteinDistanceAndSimilarity() {
        int dist = matcher.calculateLevenshteinDistance("kitten", "sitting");
        assertEquals(3, dist);

        double exact = matcher.calculateLevenshteinSimilarity("artificial", "artificial");
        assertEquals(1.0, exact, 0.001);

        double typo = matcher.calculateLevenshteinSimilarity("artifcial", "artificial");
        assertTrue(typo > 0.85);
    }

    @Test
    public void testExactMatchAgainstFaq() {
        FAQ faq = new FAQ("FAQ-001", "What is artificial intelligence?", "AI is smart software", "AI");
        MatchResult result = matcher.matchQueryAgainstFaq("what is artificial intelligence", faq);

        assertTrue(result.hasMatch());
        assertTrue(result.getScore() >= 0.90);
        assertEquals(ChatMessage.ConfidenceLevel.HIGH, result.getConfidenceLevel());
    }

    @Test
    public void testAlternativeQuestionMatch() {
        FAQ faq = new FAQ("FAQ-005", "What is Java?",
                List.of("Explain Java programming", "Tell me about Java"),
                List.of("java", "jvm"),
                "Java Programming", "Java is a language", 1.0);

        MatchResult result = matcher.matchQueryAgainstFaq("tell me about java", faq);
        assertTrue(result.hasMatch());
        assertTrue(result.getScore() >= 0.85);
        assertEquals("Alternative Question Match", result.getMatchStrategy());
    }

    @Test
    public void testKeywordMatch() {
        FAQ faq = new FAQ("FAQ-006", "Explain OOP",
                List.of(),
                List.of("encapsulation", "inheritance", "polymorphism", "abstraction"),
                "Java Programming", "OOP pillars", 1.0);

        MatchResult result = matcher.matchQueryAgainstFaq("tell me about encapsulation and polymorphism", faq);
        assertTrue(result.hasMatch());
        assertTrue(result.getScore() >= 0.50);
    }
}
