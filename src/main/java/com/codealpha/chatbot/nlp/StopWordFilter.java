package com.codealpha.chatbot.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Filters out common non-discriminative English stop words to isolate content keywords.
 */
public class StopWordFilter {

    private final Set<String> stopWords;

    private static final String[] DEFAULT_STOP_WORDS = {
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
        "any", "are", "as", "at", "be", "because", "been", "before", "being", "below",
        "between", "both", "but", "by", "can", "could", "did", "do", "does", "doing",
        "down", "during", "each", "few", "for", "from", "further", "had", "has", "have",
        "having", "he", "her", "here", "hers", "herself", "him", "himself", "his",
        "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me",
        "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off", "on",
        "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own",
        "same", "she", "should", "so", "some", "such", "than", "that", "the", "their",
        "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those",
        "through", "to", "too", "under", "until", "up", "very", "was", "we", "were",
        "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with",
        "would", "you", "your", "yours", "yourself", "yourselves", "tell", "please", "can",
        "give", "know", "want"
    };

    public StopWordFilter() {
        this.stopWords = new HashSet<>(Arrays.asList(DEFAULT_STOP_WORDS));
    }

    public StopWordFilter(Set<String> customStopWords) {
        this.stopWords = new HashSet<>(customStopWords);
    }

    public boolean isStopWord(String word) {
        if (word == null) return false;
        return stopWords.contains(word.trim().toLowerCase());
    }

    public void addStopWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            stopWords.add(word.trim().toLowerCase());
        }
    }

    public void removeStopWord(String word) {
        if (word != null) {
            stopWords.remove(word.trim().toLowerCase());
        }
    }

    public Set<String> getStopWords() {
        return Collections.unmodifiableSet(stopWords);
    }

    /**
     * Filters a list of tokens, removing stop words and tokens shorter than 2 characters.
     */
    public List<String> filterTokens(List<String> tokens) {
        if (tokens == null) return Collections.emptyList();
        List<String> filtered = new ArrayList<>();
        for (String token : tokens) {
            String lower = token.trim().toLowerCase();
            if (lower.length() > 1 && !stopWords.contains(lower)) {
                filtered.add(lower);
            }
        }
        return filtered;
    }

    /**
     * Extracts content keywords from input text using a tokenizer and stop word elimination.
     */
    public Set<String> extractKeywords(String text, Tokenizer tokenizer) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptySet();
        }
        List<String> tokens = tokenizer.tokenize(text);
        List<String> filtered = filterTokens(tokens);
        return new HashSet<>(filtered);
    }
}
