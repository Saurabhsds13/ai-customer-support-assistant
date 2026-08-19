package com.example.aisupport.ai.service;

import com.example.aisupport.ai.model.TicketAnalysisResult;
import com.example.aisupport.entity.Ticket;

/**
 * Application-level abstraction for AI ticket analysis.
 * <p>
 * This interface decouples the business logic from any specific AI provider.
 * The implementation can use OpenAI, Anthropic, Ollama, or any other
 * provider supported by Spring AI without changing the callers.
 */
public interface TicketAnalysisService {

    /**
     * Analyzes a support ticket using AI and returns structured analysis.
     *
     * @param ticket the ticket to analyze
     * @return structured analysis including category, priority, sentiment, etc.
     * @throws com.example.aisupport.ai.exception.AiServiceException if the AI service fails
     */
    TicketAnalysisResult analyzeTicket(Ticket ticket);
}
