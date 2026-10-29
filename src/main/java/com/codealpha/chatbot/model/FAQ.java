package com.codealpha.chatbot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Knowledge Base FAQ (Frequently Asked Question) record.
 */
public class FAQ {

    private String id;
    private String primaryQuestion;
    private List<String> alternativeQuestions;
    private List<String> keywords;
    private String category;
    private String answer;
    private double weight;

    public FAQ() {
        this.alternativeQuestions = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.weight = 1.0;
        this.category = "General Knowledge";
    }

    public FAQ(String id, String primaryQuestion, List<String> alternativeQuestions,
               List<String> keywords, String category, String answer, double weight) {
        this.id = id != null ? id.trim() : "";
        this.primaryQuestion = primaryQuestion != null ? primaryQuestion.trim() : "";
        this.alternativeQuestions = alternativeQuestions != null ? new ArrayList<>(alternativeQuestions) : new ArrayList<>();
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
        this.category = category != null ? category.trim() : "General Knowledge";
        this.answer = answer != null ? answer.trim() : "";
        this.weight = weight > 0 ? weight : 1.0;
    }

    public FAQ(String id, String primaryQuestion, String answer, String category) {
        this(id, primaryQuestion, new ArrayList<>(), new ArrayList<>(), category, answer, 1.0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    public String getPrimaryQuestion() {
        return primaryQuestion;
    }

    public void setPrimaryQuestion(String primaryQuestion) {
        this.primaryQuestion = primaryQuestion != null ? primaryQuestion.trim() : "";
    }

    public List<String> getAlternativeQuestions() {
        return Collections.unmodifiableList(alternativeQuestions);
    }

    public void setAlternativeQuestions(List<String> alternativeQuestions) {
        this.alternativeQuestions = alternativeQuestions != null ? new ArrayList<>(alternativeQuestions) : new ArrayList<>();
    }

    public void addAlternativeQuestion(String question) {
        if (question != null && !question.trim().isEmpty() && !alternativeQuestions.contains(question.trim())) {
            alternativeQuestions.add(question.trim());
        }
    }

    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && !keywords.contains(keyword.trim().toLowerCase())) {
            keywords.add(keyword.trim().toLowerCase());
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : "General Knowledge";
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer != null ? answer.trim() : "";
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight > 0 ? weight : 1.0;
    }

    /**
     * Returns a consolidated list containing the primary question and all alternative phrasings.
     */
    public List<String> getAllQuestions() {
        List<String> all = new ArrayList<>();
        if (primaryQuestion != null && !primaryQuestion.isEmpty()) {
            all.add(primaryQuestion);
        }
        all.addAll(alternativeQuestions);
        return all;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FAQ faq)) return false;
        return Objects.equals(id, faq.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FAQ{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", question='" + primaryQuestion + '\'' +
                '}';
    }
}
