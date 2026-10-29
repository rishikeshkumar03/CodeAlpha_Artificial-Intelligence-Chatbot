package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.Intent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntentRecognizerTest {

    private IntentRecognizer recognizer;

    @BeforeEach
    public void setUp() {
        recognizer = new IntentRecognizer();
    }

    @Test
    public void testGreetings() {
        assertEquals(Intent.GREETING, recognizer.recognizeIntent("Hello"));
        assertEquals(Intent.GREETING, recognizer.recognizeIntent("Hi there"));
        assertEquals(Intent.GREETING, recognizer.recognizeIntent("good morning!"));
        assertEquals(Intent.GREETING, recognizer.recognizeIntent("Hey!"));
    }

    @Test
    public void testFarewells() {
        assertEquals(Intent.FAREWELL, recognizer.recognizeIntent("Bye"));
        assertEquals(Intent.FAREWELL, recognizer.recognizeIntent("goodbye"));
        assertEquals(Intent.FAREWELL, recognizer.recognizeIntent("see you later"));
    }

    @Test
    public void testHelp() {
        assertEquals(Intent.HELP, recognizer.recognizeIntent("help"));
        assertEquals(Intent.HELP, recognizer.recognizeIntent("Can you help me?"));
        assertEquals(Intent.HELP, recognizer.recognizeIntent("how do i use this"));
    }

    @Test
    public void testCapabilities() {
        assertEquals(Intent.CAPABILITIES, recognizer.recognizeIntent("what can you do"));
        assertEquals(Intent.CAPABILITIES, recognizer.recognizeIntent("what are your features"));
    }

    @Test
    public void testIdentity() {
        assertEquals(Intent.BOT_IDENTITY, recognizer.recognizeIntent("Who are you?"));
        assertEquals(Intent.BOT_IDENTITY, recognizer.recognizeIntent("What is your name?"));
    }

    @Test
    public void testFaqQuery() {
        assertEquals(Intent.FAQ_QUERY, recognizer.recognizeIntent("What is polymorphism in Java?"));
        assertEquals(Intent.FAQ_QUERY, recognizer.recognizeIntent("Explain bubble sort algorithm"));
    }

    @Test
    public void testEmptyFallback() {
        assertEquals(Intent.FALLBACK, recognizer.recognizeIntent(""));
        assertEquals(Intent.FALLBACK, recognizer.recognizeIntent("   "));
    }
}
