package com.example.aisupport.dto.ai;

import java.util.List;

/**
 * API response DTO for the ticket analysis endpoint.
 * Wraps the AI analysis result with the ticket ID for context.
 */
public class TicketAnalysisResponse {

    private Long ticketId;
    private String category;
    private String priority;
    private String summary;
    private String sentiment;
    private List<String> recommendedActions;
    private String suggestedResponse;

    public TicketAnalysisResponse() {
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
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
