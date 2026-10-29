package com.codealpha.chatbot.nlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextNormalizerTest {

    private TextNormalizer normalizer;

    @BeforeEach
    public void setUp() {
        normalizer = new TextNormalizer();
    }

    @Test
    public void testNullAndEmpty() {
        assertEquals("", normalizer.normalize(null));
        assertEquals("", normalizer.normalize(""));
        assertEquals("", normalizer.normalize("   "));
    }

    @Test
    public void testLowercasingAndWhitespace() {
        assertEquals("what is java", normalizer.normalize("  WHAT   IS   JAVA  "));
    }

    @Test
    public void testPunctuationRemoval() {
        assertEquals("what is artificial intelligence", normalizer.normalize("What is artificial intelligence?!,;"));
    }

    @Test
    public void testContractionExpansion() {
        assertEquals("what is your name", normalizer.normalize("What's your name?"));
        assertEquals("i am a programmer", normalizer.normalize("I'm a programmer!"));
        assertEquals("i cannot do that", normalizer.normalize("I can't do that"));
        assertEquals("do not give up", normalizer.normalize("Don't give up"));
    }
}
