package com.codealpha.chatbot.service;

import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.storage.FAQRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * High-level business service providing FAQ management, querying, filtering, and data portability.
 */
public class FAQService {

    private final FAQRepository repository;

    public FAQService(FAQRepository repository) {
        this.repository = repository;
    }

    public List<FAQ> getAllFAQs() {
        return repository.findAll();
    }

    public Optional<FAQ> getFAQById(String id) {
        return repository.findById(id);
    }

    public void addFAQ(FAQ faq) throws IOException {
        if (faq == null) {
            throw new IllegalArgumentException("FAQ cannot be null");
        }
        if (faq.getId() == null || faq.getId().trim().isEmpty()) {
            faq.setId(repository.generateNextId());
        }
        repository.save(faq);
    }

    public void updateFAQ(FAQ faq) throws IOException {
        if (faq == null || faq.getId() == null || !repository.existsById(faq.getId())) {
            throw new IllegalArgumentException("Cannot update non-existing FAQ: " + (faq != null ? faq.getId() : "null"));
        }
        repository.save(faq);
    }

    public boolean deleteFAQ(String id) throws IOException {
        return repository.deleteById(id);
    }

    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    public String generateNextId() {
        return repository.generateNextId();
    }

    /**
     * Searches and filters FAQs by search term and optional category.
     */
    public List<FAQ> searchFAQs(String query, String categoryFilter) {
        List<FAQ> all = repository.findAll();
        String q = query != null ? query.trim().toLowerCase() : "";
        boolean filterCategory = categoryFilter != null && !categoryFilter.equalsIgnoreCase("All Categories") && !categoryFilter.trim().isEmpty();

        return all.stream()
                .filter(faq -> {
                    if (filterCategory && !faq.getCategory().equalsIgnoreCase(categoryFilter.trim())) {
                        return false;
                    }
                    if (q.isEmpty()) {
                        return true;
                    }
                    boolean matchesPrimary = faq.getPrimaryQuestion().toLowerCase().contains(q);
                    boolean matchesAnswer = faq.getAnswer().toLowerCase().contains(q);
                    boolean matchesCategory = faq.getCategory().toLowerCase().contains(q);
                    boolean matchesId = faq.getId().toLowerCase().contains(q);
                    boolean matchesAlts = faq.getAlternativeQuestions().stream().anyMatch(alt -> alt.toLowerCase().contains(q));
                    boolean matchesKeywords = faq.getKeywords().stream().anyMatch(kw -> kw.toLowerCase().contains(q));

                    return matchesPrimary || matchesAnswer || matchesCategory || matchesId || matchesAlts || matchesKeywords;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves unique categories present in the current knowledge base.
     */
    public List<String> getCategories() {
        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (FAQ faq : repository.findAll()) {
            if (faq.getCategory() != null && !faq.getCategory().trim().isEmpty()) {
                categories.add(faq.getCategory().trim());
            }
        }
        return new ArrayList<>(categories);
    }

    public int importFAQs(File file) throws IOException {
        return repository.importFromCSV(file);
    }

    public void exportFAQs(File file) throws IOException {
        repository.exportToCSV(file);
    }

    public void resetToDefaults() throws IOException {
        repository.resetToDefaults();
    }
}
