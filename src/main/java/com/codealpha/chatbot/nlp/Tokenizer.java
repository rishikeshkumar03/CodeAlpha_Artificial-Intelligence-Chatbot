package com.codealpha.chatbot.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tokenizes normalized text into word tokens, unique token sets, and word n-grams.
 */
public class Tokenizer {

    private final TextNormalizer normalizer;

    public Tokenizer() {
        this.normalizer = new TextNormalizer();
    }

    public Tokenizer(TextNormalizer normalizer) {
        this.normalizer = normalizer != null ? normalizer : new TextNormalizer();
    }

    /**
     * Splits text into individual word tokens after normalization.
     */
    public List<String> tokenize(String text) {
        if (text == null) {
            return Collections.emptyList();
        }

        String normalized = normalizer.normalize(text);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = normalized.split("\\s+");
        List<String> tokens = new ArrayList<>(parts.length);
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    /**
     * Extracts unique word tokens as a Set for set-based operations (like Jaccard similarity).
     */
    public Set<String> tokenizeToSet(String text) {
        return new HashSet<>(tokenize(text));
    }

    /**
     * Generates word-level n-grams (e.g., bigrams for n=2, trigrams for n=3).
     */
    public List<String> generateNGrams(String text, int n) {
        List<String> tokens = tokenize(text);
        if (tokens.size() < n || n <= 0) {
            return Collections.emptyList();
        }

        List<String> nGrams = new ArrayList<>();
        for (int i = 0; i <= tokens.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(" ");
                sb.append(tokens.get(i + j));
            }
            nGrams.add(sb.toString());
        }
        return nGrams;
    }
}
