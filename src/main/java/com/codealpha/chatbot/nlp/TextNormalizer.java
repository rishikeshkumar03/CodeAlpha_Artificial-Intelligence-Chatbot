package com.codealpha.chatbot.nlp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles text cleaning, case standardization, contraction expansion, and punctuation normalization.
 */
public class TextNormalizer {

    private static final Map<Pattern, String> CONTRACTIONS = new HashMap<>();
    private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION_CLEANUP = Pattern.compile("[^a-zA-Z0-9\\s]");

    static {
        addContraction("\\bwhat's\\b", "what is");
        addContraction("\\bwhats\\b", "what is");
        addContraction("\\bwho's\\b", "who is");
        addContraction("\\bwhere's\\b", "where is");
        addContraction("\\bwhen's\\b", "when is");
        addContraction("\\bhow's\\b", "how is");
        addContraction("\\bhows\\b", "how is");
        addContraction("\\bit's\\b", "it is");
        addContraction("\\bcan't\\b", "cannot");
        addContraction("\\bcant\\b", "cannot");
        addContraction("\\bdon't\\b", "do not");
        addContraction("\\bdont\\b", "do not");
        addContraction("\\bdoesn't\\b", "does not");
        addContraction("\\bdid't\\b", "did not");
        addContraction("\\bi'm\\b", "i am");
        addContraction("\\bim\\b", "i am");
        addContraction("\\byou're\\b", "you are");
        addContraction("\\bwe're\\b", "we are");
        addContraction("\\bthey're\\b", "they are");
        addContraction("\\bwon't\\b", "will not");
        addContraction("\\bisn't\\b", "is not");
        addContraction("\\baren't\\b", "are not");
        addContraction("\\bwasn't\\b", "was not");
        addContraction("\\bweren't\\b", "were not");
        addContraction("\\bshouldn't\\b", "should not");
        addContraction("\\bcouldn't\\b", "could not");
        addContraction("\\bwouldn't\\b", "would not");
        addContraction("\\bthat's\\b", "that is");
        addContraction("\\bthere's\\b", "there is");
    }

    private static void addContraction(String regex, String replacement) {
        CONTRACTIONS.put(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), replacement);
    }

    /**
     * Normalizes an input string:
     * 1. Null/empty safety
     * 2. Trimming & lowercasing
     * 3. Expanding standard English contractions
     * 4. Removing punctuation (keeping alphanumerics and spaces)
     * 5. Collapsing multiple spaces into single space
     */
    public String normalize(String text) {
        if (text == null) {
            return "";
        }

        String result = text.trim().toLowerCase();
        if (result.isEmpty()) {
            return "";
        }

        // Expand contractions
        for (Map.Entry<Pattern, String> entry : CONTRACTIONS.entrySet()) {
            result = entry.getKey().matcher(result).replaceAll(entry.getValue());
        }

        // Strip non-alphanumeric characters (except spaces)
        result = PUNCTUATION_CLEANUP.matcher(result).replaceAll(" ");

        // Collapse whitespace
        result = MULTI_WHITESPACE.matcher(result).replaceAll(" ").trim();

        return result;
    }

    /**
     * Light normalization preserving punctuation for display or sentence boundary checks.
     */
    public String cleanWhitespace(String text) {
        if (text == null) return "";
        return MULTI_WHITESPACE.matcher(text.trim()).replaceAll(" ");
    }
}
