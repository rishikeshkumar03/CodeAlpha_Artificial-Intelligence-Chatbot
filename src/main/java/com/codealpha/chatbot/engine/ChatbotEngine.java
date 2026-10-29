package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.model.Intent;
import com.codealpha.chatbot.model.MatchResult;
import com.codealpha.chatbot.nlp.SimilarityMatcher;
import com.codealpha.chatbot.nlp.StopWordFilter;
import com.codealpha.chatbot.nlp.TextNormalizer;
import com.codealpha.chatbot.nlp.Tokenizer;
import com.codealpha.chatbot.service.FAQService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Core Chatbot Engine orchestrating the complete NLP pipeline:
 * Input -> Normalization -> Tokenization -> Intent detection -> FAQ matching -> Confidence scoring -> Response generation.
 */
public class ChatbotEngine {

    private final TextNormalizer normalizer;
    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;
    private final SimilarityMatcher similarityMatcher;
    private final IntentRecognizer intentRecognizer;
    private final ConfidenceScorer confidenceScorer;
    private final ResponseGenerator responseGenerator;
    private FAQService faqService;

    public ChatbotEngine(FAQService faqService) {
        this.normalizer = new TextNormalizer();
        this.tokenizer = new Tokenizer(this.normalizer);
        this.stopWordFilter = new StopWordFilter();
        this.similarityMatcher = new SimilarityMatcher(this.normalizer, this.tokenizer, this.stopWordFilter);
        this.intentRecognizer = new IntentRecognizer(this.normalizer);
        this.confidenceScorer = new ConfidenceScorer();
        this.responseGenerator = new ResponseGenerator();
        this.faqService = faqService;
    }

    public ChatbotEngine(TextNormalizer normalizer, Tokenizer tokenizer, StopWordFilter stopWordFilter,
                         SimilarityMatcher similarityMatcher, IntentRecognizer intentRecognizer,
                         ConfidenceScorer confidenceScorer, ResponseGenerator responseGenerator,
                         FAQService faqService) {
        this.normalizer = normalizer != null ? normalizer : new TextNormalizer();
        this.tokenizer = tokenizer != null ? tokenizer : new Tokenizer(this.normalizer);
        this.stopWordFilter = stopWordFilter != null ? stopWordFilter : new StopWordFilter();
        this.similarityMatcher = similarityMatcher != null ? similarityMatcher : new SimilarityMatcher(this.normalizer, this.tokenizer, this.stopWordFilter);
        this.intentRecognizer = intentRecognizer != null ? intentRecognizer : new IntentRecognizer(this.normalizer);
        this.confidenceScorer = confidenceScorer != null ? confidenceScorer : new ConfidenceScorer();
        this.responseGenerator = responseGenerator != null ? responseGenerator : new ResponseGenerator();
        this.faqService = faqService;
    }

    public FAQService getFaqService() {
        return faqService;
    }

    public void setFaqService(FAQService faqService) {
        this.faqService = faqService;
    }

    public ConfidenceScorer getConfidenceScorer() {
        return confidenceScorer;
    }

    public SimilarityMatcher getSimilarityMatcher() {
        return similarityMatcher;
    }

    public TextNormalizer getNormalizer() {
        return normalizer;
    }

    /**
     * Processes a user question and produces an intelligent bot response.
     */
    public ChatMessage processQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return responseGenerator.generateEmptyInputResponse();
        }

        // 1. Text normalization & intent classification
        Intent intent = intentRecognizer.recognizeIntent(userQuery);

        switch (intent) {
            case GREETING:
                return responseGenerator.generateGreetingResponse();
            case FAREWELL:
                return responseGenerator.generateFarewellResponse();
            case HELP:
                return responseGenerator.generateHelpResponse();
            case CAPABILITIES:
                int count = faqService != null ? faqService.getAllFAQs().size() : 0;
                return responseGenerator.generateCapabilitiesResponse(count);
            case BOT_IDENTITY:
                return responseGenerator.generateIdentityResponse();
            case CLEAR_CHAT:
                return new ChatMessage(ChatMessage.Sender.SYSTEM, "Conversation cleared.", 1.0, ChatMessage.ConfidenceLevel.HIGH, null, "System");
            case FAQ_QUERY:
            default:
                return matchAndGenerateFAQResponse(userQuery);
        }
    }

    /**
     * Matches user query against the FAQ repository, scores confidence, and returns the response.
     */
    public ChatMessage matchAndGenerateFAQResponse(String userQuery) {
        List<FAQ> faqs = (faqService != null) ? faqService.getAllFAQs() : List.of();

        if (faqs.isEmpty()) {
            return responseGenerator.generateFallbackResponse(userQuery, List.of());
        }

        List<MatchResult> candidates = new ArrayList<>();
        for (FAQ faq : faqs) {
            MatchResult result = similarityMatcher.matchQueryAgainstFaq(userQuery, faq);
            candidates.add(result);
        }

        // Sort candidate matches by score descending
        candidates.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());

        MatchResult bestMatch = confidenceScorer.selectBestMatch(candidates);

        if (confidenceScorer.isConfident(bestMatch)) {
            return responseGenerator.generateFaqResponse(bestMatch);
        } else {
            // Find close candidates (score > 0.20) for suggestions
            List<MatchResult> closeCandidates = candidates.stream()
                    .filter(m -> m.getScore() >= 0.20)
                    .limit(3)
                    .toList();
            return responseGenerator.generateFallbackResponse(userQuery, closeCandidates);
        }
    }
}
