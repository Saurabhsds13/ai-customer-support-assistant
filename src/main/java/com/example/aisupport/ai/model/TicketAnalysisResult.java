package com.example.aisupport.ai.model;

import java.util.List;

/**
 * Structured output DTO that the LLM is instructed to produce.
 * Spring AI's BeanOutputConverter generates a JSON schema from this class
 * and includes it in the prompt so the model returns well-formed JSON.
 */
public class TicketAnalysisResult {

    private String category;
    private String priority;
    private String summary;
    private String sentiment;
    private List<String> recommendedActions;
    private String suggestedResponse;

    public TicketAnalysisResult() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public String getSuggestedResponse() {
        return suggestedResponse;
    }

    public void setSuggestedResponse(String suggestedResponse) {
        this.suggestedResponse = suggestedResponse;
    }
}
