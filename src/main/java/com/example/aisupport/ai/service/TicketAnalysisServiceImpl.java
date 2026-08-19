package com.example.aisupport.ai.service;

import com.example.aisupport.ai.exception.AiServiceException;
import com.example.aisupport.ai.exception.AiServiceException.AiErrorType;
import com.example.aisupport.ai.model.TicketAnalysisResult;
import com.example.aisupport.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Spring AI implementation of ticket analysis.
 * <p>
 * This class uses Spring AI's ChatModel interface to communicate with an LLM.
 * The ChatModel is injected by Spring — which concrete provider is used
 * (OpenAI, Anthropic, etc.) depends solely on which starter is on the classpath
 * and the configuration in application.yml.
 * <p>
 * Key Spring AI concepts demonstrated here:
 * - ChatModel: the abstraction for calling any LLM
 * - PromptTemplate: parameterized prompt loaded from a resource file
 * - BeanOutputConverter: converts a Java class into a JSON schema for structured output
 */
@Service
public class TicketAnalysisServiceImpl implements TicketAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TicketAnalysisServiceImpl.class);

    private final ChatModel chatModel;
    private final Resource promptResource;

    public TicketAnalysisServiceImpl(
            ChatModel chatModel,
            @Value("classpath:prompts/ticket-analysis.st") Resource promptResource) {
        this.chatModel = chatModel;
        this.promptResource = promptResource;
    }

    @Override
    public TicketAnalysisResult analyzeTicket(Ticket ticket) {
        log.debug("Starting AI analysis for ticket id={}", ticket.getId());

        // BeanOutputConverter generates a JSON schema from TicketAnalysisResult
        // and provides format instructions that we inject into the prompt
        BeanOutputConverter<TicketAnalysisResult> outputConverter =
                new BeanOutputConverter<>(TicketAnalysisResult.class);

        // Build the prompt from the template file with ticket data
        PromptTemplate promptTemplate = new PromptTemplate(promptResource);
        Prompt prompt = promptTemplate.create(Map.of(
                "title", ticket.getTitle(),
                "description", ticket.getDescription(),
                "currentCategory", ticket.getCategory().name(),
                "currentPriority", ticket.getPriority().name(),
                "currentStatus", ticket.getStatus().name(),
                "format", outputConverter.getFormat()
        ));

        try {
            ChatResponse response = chatModel.call(prompt);

            String content = response.getResult().getOutput().getContent();
            if (content == null || content.isBlank()) {
                throw new AiServiceException(
                        "AI returned empty response for ticket id=" + ticket.getId(),
                        AiErrorType.MALFORMED_RESPONSE);
            }

            // Parse the structured JSON output into our Java DTO
            TicketAnalysisResult result = outputConverter.convert(content);
            validateResult(result, ticket.getId());

            log.debug("AI analysis complete for ticket id={}", ticket.getId());
            return result;

        } catch (AiServiceException e) {
            throw e; // re-throw our own exceptions
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Unknown AI error";

            if (isTimeout(e)) {
                throw new AiServiceException(
                        "AI service timed out for ticket id=" + ticket.getId(),
                        AiErrorType.TIMEOUT, e);
            }
            if (isConnectionError(e)) {
                throw new AiServiceException(
                        "AI service unavailable for ticket id=" + ticket.getId(),
                        AiErrorType.PROVIDER_UNAVAILABLE, e);
            }

            throw new AiServiceException(
                    "AI analysis failed for ticket id=" + ticket.getId() + ": " + message,
                    AiErrorType.UNKNOWN, e);
        }
    }

    private void validateResult(TicketAnalysisResult result, Long ticketId) {
        if (result == null) {
            throw new AiServiceException(
                    "AI returned null result for ticket id=" + ticketId,
                    AiErrorType.MALFORMED_RESPONSE);
        }
        if (result.getSummary() == null || result.getSummary().isBlank()) {
            throw new AiServiceException(
                    "AI returned result with missing summary for ticket id=" + ticketId,
                    AiErrorType.VALIDATION_FAILURE);
        }
        if (result.getCategory() == null || result.getCategory().isBlank()) {
            throw new AiServiceException(
                    "AI returned result with missing category for ticket id=" + ticketId,
                    AiErrorType.VALIDATION_FAILURE);
        }
        if (result.getPriority() == null || result.getPriority().isBlank()) {
            throw new AiServiceException(
                    "AI returned result with missing priority for ticket id=" + ticketId,
                    AiErrorType.VALIDATION_FAILURE);
        }
        if (result.getSentiment() == null || result.getSentiment().isBlank()) {
            throw new AiServiceException(
                    "AI returned result with missing sentiment for ticket id=" + ticketId,
                    AiErrorType.VALIDATION_FAILURE);
        }
        if (result.getRecommendedActions() == null || result.getRecommendedActions().isEmpty()) {
            throw new AiServiceException(
                    "AI returned result with no recommended actions for ticket id=" + ticketId,
                    AiErrorType.VALIDATION_FAILURE);
        }
    }

    private boolean isTimeout(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("timeout") || msg.contains("timed out")
                || e.getCause() instanceof java.net.SocketTimeoutException;
    }

    private boolean isConnectionError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("connection refused") || msg.contains("connect")
                || e.getCause() instanceof java.net.ConnectException;
    }
}
