package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.storage.CSVStorage;
import com.codealpha.chatbot.storage.FAQRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FAQServiceTest {

    private FAQService faqService;
    private FAQRepository repository;

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws IOException {
        File dataFile = tempDir.resolve("faqs.csv").toFile();
        repository = new FAQRepository(dataFile, new CSVStorage());
        repository.initialize();
        faqService = new FAQService(repository);
    }

    @Test
    public void testInitialSeededFaqsCount() {
        List<FAQ> all = faqService.getAllFAQs();
        assertTrue(all.size() >= 30, "Knowledge base should seed at least 30 starter FAQs");
    }

    @Test
    public void testAddAndGetFaq() throws IOException {
        FAQ newFaq = new FAQ("FAQ-999", "What is Quantum Computing?", List.of("Explain quantum computers"),
                List.of("quantum", "qubits"), "Technology", "Quantum computing uses quantum mechanics.", 1.2);

        faqService.addFAQ(newFaq);

        assertTrue(faqService.existsById("FAQ-999"));
        var retrieved = faqService.getFAQById("FAQ-999");
        assertTrue(retrieved.isPresent());
        assertEquals("What is Quantum Computing?", retrieved.get().getPrimaryQuestion());
        assertEquals("Technology", retrieved.get().getCategory());
    }

    @Test
    public void testUpdateFaq() throws IOException {
        var opt = faqService.getFAQById("FAQ-001");
        assertTrue(opt.isPresent());
        FAQ faq = opt.get();
        faq.setAnswer("Updated Artificial Intelligence Answer");
        faqService.updateFAQ(faq);

        var updated = faqService.getFAQById("FAQ-001");
        assertTrue(updated.isPresent());
        assertEquals("Updated Artificial Intelligence Answer", updated.get().getAnswer());
    }

    @Test
    public void testDeleteFaq() throws IOException {
        assertTrue(faqService.existsById("FAQ-001"));
        boolean deleted = faqService.deleteFAQ("FAQ-001");
        assertTrue(deleted);
        assertFalse(faqService.existsById("FAQ-001"));
    }

    @Test
    public void testSearchAndFilter() {
        List<FAQ> javaFaqs = faqService.searchFAQs("java", "Java Programming");
        assertFalse(javaFaqs.isEmpty());
        for (FAQ f : javaFaqs) {
            assertEquals("Java Programming", f.getCategory());
        }

        List<FAQ> searchKeywords = faqService.searchFAQs("polymorphism", "All Categories");
        assertFalse(searchKeywords.isEmpty());
    }
}
