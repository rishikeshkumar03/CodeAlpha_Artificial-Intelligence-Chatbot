package com.codealpha.chatbot.nlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    private Tokenizer tokenizer;

    @BeforeEach
    public void setUp() {
        tokenizer = new Tokenizer();
    }

    @Test
    public void testTokenizeSimpleSentence() {
        List<String> tokens = tokenizer.tokenize("Java is an object-oriented language.");
        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("is"));
        assertTrue(tokens.contains("an"));
        assertTrue(tokens.contains("object"));
        assertTrue(tokens.contains("oriented"));
        assertTrue(tokens.contains("language"));
    }

    @Test
    public void testTokenizeEmpty() {
        assertTrue(tokenizer.tokenize(null).isEmpty());
        assertTrue(tokenizer.tokenize("   ").isEmpty());
    }

    @Test
    public void testTokenizeToSet() {
        Set<String> set = tokenizer.tokenizeToSet("Java Java programming java");
        assertEquals(2, set.size());
        assertTrue(set.contains("java"));
        assertTrue(set.contains("programming"));
    }

    @Test
    public void testNGrams() {
        List<String> bigrams = tokenizer.generateNGrams("data structures and algorithms", 2);
        assertEquals(3, bigrams.size());
        assertEquals("data structures", bigrams.get(0));
        assertEquals("structures and", bigrams.get(1));
        assertEquals("and algorithms", bigrams.get(2));
    }
}
