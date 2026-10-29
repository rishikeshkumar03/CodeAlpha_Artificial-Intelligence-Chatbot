package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.service.FAQService;
import com.codealpha.chatbot.storage.CSVStorage;
import com.codealpha.chatbot.storage.FAQRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ChatbotEngineTest {

    private ChatbotEngine engine;
    private FAQService faqService;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws IOException {
        File dataFile = tempDir.resolve("faqs.csv").toFile();
        FAQRepository repo = new FAQRepository(dataFile, new CSVStorage());
        repo.initialize();
        faqService = new FAQService(repo);
        engine = new ChatbotEngine(faqService);
    }

    @Test
    public void testGreetingQuery() {
        ChatMessage response = engine.processQuery("Hello!");
        assertNotNull(response);
        assertEquals(ChatMessage.Sender.BOT, response.getSender());
        assertEquals(ChatMessage.ConfidenceLevel.HIGH, response.getConfidenceLevel());
        assertEquals("Greetings", response.getCategory());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    public void testPrimaryQuestionMatch() {
        ChatMessage response = engine.processQuery("What is artificial intelligence?");
        assertNotNull(response);
        assertEquals(ChatMessage.Sender.BOT, response.getSender());
        assertEquals("FAQ-001", response.getMatchedFaqId());
        assertTrue(response.getConfidenceScore() >= 0.75);
    }

    @Test
    public void testAlternativeQuestionMatch() {
        // "Explain AI in simple words" is an alternative phrasing for FAQ-001
        ChatMessage response = engine.processQuery("Explain AI in simple words");
        assertNotNull(response);
        assertEquals(ChatMessage.Sender.BOT, response.getSender());
        assertEquals("FAQ-001", response.getMatchedFaqId());
        assertTrue(response.getConfidenceScore() >= 0.70);
    }

    @Test
    public void testTypoTolerance() {
        // "artifcial intlligence" with typos
        ChatMessage response = engine.processQuery("What is artifcial intlligence?");
        assertNotNull(response);
        assertEquals(ChatMessage.Sender.BOT, response.getSender());
        assertEquals("FAQ-001", response.getMatchedFaqId());
    }

    @Test
    public void testFallbackOnUnknownQuery() {
        ChatMessage response = engine.processQuery("quantum flux capacitor hyperdrive 9988223");
        assertNotNull(response);
        assertEquals(ChatMessage.Sender.BOT, response.getSender());
        assertTrue(response.getContent().contains("I don't have enough confidence") || response.getContent().contains("Did you mean"));
    }

    @Test
    public void testEmptyQuery() {
        ChatMessage response = engine.processQuery("   ");
        assertNotNull(response);
        assertTrue(response.getContent().contains("Please type a question"));
    }
}
