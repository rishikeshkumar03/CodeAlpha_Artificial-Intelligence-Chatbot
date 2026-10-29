package com.codealpha.chatbot.nlp;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.model.MatchResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calculates similarity scores between user queries and FAQ records using
 * Jaccard token similarity, Levenshtein edit distance, keyword overlap, and phrase matching.
 */
public class SimilarityMatcher {

    private final TextNormalizer normalizer;
    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;

    // Weight coefficients for composite similarity scoring
    private static final double WEIGHT_JACCARD_ALL = 0.25;
    private static final double WEIGHT_JACCARD_KEYWORDS = 0.45;
    private static final double WEIGHT_LEVENSHTEIN = 0.30;

    public SimilarityMatcher() {
        this.normalizer = new TextNormalizer();
        this.tokenizer = new Tokenizer(this.normalizer);
        this.stopWordFilter = new StopWordFilter();
    }

    public SimilarityMatcher(TextNormalizer normalizer, Tokenizer tokenizer, StopWordFilter stopWordFilter) {
        this.normalizer = normalizer != null ? normalizer : new TextNormalizer();
        this.tokenizer = tokenizer != null ? tokenizer : new Tokenizer(this.normalizer);
        this.stopWordFilter = stopWordFilter != null ? stopWordFilter : new StopWordFilter();
    }

    /**
     * Calculates Jaccard similarity between two sets of strings: |A ∩ B| / |A ∪ B|.
     */
    public double calculateJaccardSimilarity(Set<String> setA, Set<String> setB) {
        if (setA == null || setB == null || (setA.isEmpty() && setB.isEmpty())) {
            return 0.0;
        }
        if (setA.isEmpty() || setB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        return (double) intersection.size() / (double) union.size();
    }

    /**
     * Calculates Levenshtein (edit) distance between two strings.
     */
    public int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[len1][len2];
    }

    /**
     * Normalized Levenshtein similarity in the range [0.0, 1.0].
     */
    public double calculateLevenshteinSimilarity(String s1, String s2) {
        String n1 = normalizer.normalize(s1);
        String n2 = normalizer.normalize(s2);

        if (n1.isEmpty() && n2.isEmpty()) return 1.0;
        if (n1.isEmpty() || n2.isEmpty()) return 0.0;
        if (n1.equals(n2)) return 1.0;

        int maxLen = Math.max(n1.length(), n2.length());
        int distance = calculateLevenshteinDistance(n1, n2);
        return Math.max(0.0, 1.0 - ((double) distance / maxLen));
    }

    /**
     * Calculates composite similarity between two text sentences.
     */
    public double calculateSentenceSimilarity(String query, String target) {
        String normQuery = normalizer.normalize(query);
        String normTarget = normalizer.normalize(target);

        if (normQuery.isEmpty() || normTarget.isEmpty()) {
            return 0.0;
        }

        // Exact match
        if (normQuery.equals(normTarget)) {
            return 1.0;
        }

        // Substring containment check
        if (normQuery.contains(normTarget) || normTarget.contains(normQuery)) {
            double lengthRatio = (double) Math.min(normQuery.length(), normTarget.length()) /
                                 (double) Math.max(normQuery.length(), normTarget.length());
            if (lengthRatio > 0.6) {
                return 0.90 + (0.10 * lengthRatio);
            }
        }

        // 1. All tokens Jaccard
        Set<String> allQueryTokens = tokenizer.tokenizeToSet(normQuery);
        Set<String> allTargetTokens = tokenizer.tokenizeToSet(normTarget);
        double jaccardAll = calculateJaccardSimilarity(allQueryTokens, allTargetTokens);

        // 2. Keyword matching (content tokens)
        Set<String> queryKeywords = stopWordFilter.extractKeywords(normQuery, tokenizer);
        Set<String> targetKeywords = stopWordFilter.extractKeywords(normTarget, tokenizer);
        double jaccardKeywords = calculateJaccardSimilarity(queryKeywords, targetKeywords);

        // Fuzzy keyword overlap for typo resilience (e.g. "artifcial" -> "artificial")
        double fuzzyKeywordScore = calculateFuzzyKeywordOverlap(queryKeywords, targetKeywords);
        double effectiveKeywordScore = Math.max(jaccardKeywords, fuzzyKeywordScore);

        // If keywords match 100% or high fuzzy match
        if (!queryKeywords.isEmpty() && effectiveKeywordScore >= 0.90) {
            double lev = calculateLevenshteinSimilarity(normQuery, normTarget);
            return Math.max(0.85, (0.6 * effectiveKeywordScore) + (0.4 * lev));
        }

        // 3. Normalized Levenshtein
        double levenshtein = calculateLevenshteinSimilarity(normQuery, normTarget);

        // Weighted blend
        double score = (WEIGHT_JACCARD_ALL * jaccardAll) +
                       (WEIGHT_JACCARD_KEYWORDS * effectiveKeywordScore) +
                       (WEIGHT_LEVENSHTEIN * levenshtein);

        // Keyword overlap bonus
        if (!queryKeywords.isEmpty() && !targetKeywords.isEmpty()) {
            if (effectiveKeywordScore >= 0.70) {
                score = Math.max(score, 0.65 + (0.30 * effectiveKeywordScore));
            }
        }

        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * Calculates fuzzy keyword overlap between query keywords and target keywords,
     * allowing slight spelling typos (Levenshtein similarity >= 0.78).
     */
    public double calculateFuzzyKeywordOverlap(Set<String> queryKeywords, Set<String> targetKeywords) {
        if (queryKeywords == null || targetKeywords == null || queryKeywords.isEmpty() || targetKeywords.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        for (String qk : queryKeywords) {
            double bestMatch = 0.0;
            for (String tk : targetKeywords) {
                if (qk.equals(tk)) {
                    bestMatch = 1.0;
                    break;
                }
                double sim = calculateLevenshteinSimilarity(qk, tk);
                if (sim > bestMatch) {
                    bestMatch = sim;
                }
            }
            if (bestMatch >= 0.78) {
                totalScore += bestMatch;
            }
        }
        return totalScore / (double) queryKeywords.size();
    }

    /**
     * Evaluates a user query against a single FAQ entry, checking primary question,
     * alternative phrasings, and declared keywords.
     */
    public MatchResult matchQueryAgainstFaq(String query, FAQ faq) {
        if (query == null || faq == null) {
            return MatchResult.noMatch("Invalid input or FAQ");
        }

        String bestPhrase = faq.getPrimaryQuestion();
        String bestStrategy = "Primary Question Match";
        double maxScore = calculateSentenceSimilarity(query, faq.getPrimaryQuestion());

        // Check each alternative question
        for (String altQuestion : faq.getAlternativeQuestions()) {
            double altScore = calculateSentenceSimilarity(query, altQuestion);
            if (altScore > maxScore) {
                maxScore = altScore;
                bestPhrase = altQuestion;
                bestStrategy = "Alternative Question Match";
            }
        }

        // Check declared FAQ keywords
        Set<String> queryKeywords = stopWordFilter.extractKeywords(query, tokenizer);
        if (!queryKeywords.isEmpty() && !faq.getKeywords().isEmpty()) {
            Set<String> faqKeywords = new HashSet<>();
            for (String kw : faq.getKeywords()) {
                faqKeywords.add(normalizer.normalize(kw));
            }

            Set<String> matchedKeywords = new HashSet<>(queryKeywords);
            matchedKeywords.retainAll(faqKeywords);

            if (!matchedKeywords.isEmpty()) {
                double kwScore = (double) matchedKeywords.size() / (double) Math.max(1, queryKeywords.size());
                // Boost with keyword match
                double potentialScore = 0.50 + (0.45 * kwScore);
                if (potentialScore > maxScore) {
                    maxScore = potentialScore;
                    bestPhrase = "Keywords: " + String.join(", ", matchedKeywords);
                    bestStrategy = "Keyword Match";
                }
            }
        }

        // Apply FAQ weight multiplier (capped at 1.0)
        double finalScore = Math.min(1.0, maxScore * faq.getWeight());

        ChatMessage.ConfidenceLevel level;
        if (finalScore >= 0.75) {
            level = ChatMessage.ConfidenceLevel.HIGH;
        } else if (finalScore >= 0.45) {
            level = ChatMessage.ConfidenceLevel.MEDIUM;
        } else if (finalScore > 0.20) {
            level = ChatMessage.ConfidenceLevel.LOW;
        } else {
            level = ChatMessage.ConfidenceLevel.NONE;
        }

        String explanation = String.format("Matched '%s' with score %.2f via %s",
                bestPhrase, finalScore, bestStrategy);

        return new MatchResult(faq, finalScore, level, bestPhrase, bestStrategy, explanation);
    }
}
