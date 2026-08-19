package com.example.aisupport.ai.service;

import com.example.aisupport.ai.exception.AiServiceException;
import com.example.aisupport.ai.exception.AiServiceException.AiErrorType;
import com.example.aisupport.ai.model.TicketAnalysisResult;
import com.example.aisupport.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAnalysisServiceTest {

    @Mock
    private ChatModel chatModel;

    private TicketAnalysisServiceImpl ticketAnalysisService;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        ticketAnalysisService = new TicketAnalysisServiceImpl(
                chatModel,
                new ClassPathResource("prompts/ticket-analysis.st")
        );

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Cannot login after password reset");
        testTicket.setDescription("I reset my password yesterday but I still cannot log into my account.");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setPriority(TicketPriority.MEDIUM);
        testTicket.setCategory(TicketCategory.ACCOUNT);
        testTicket.setCreatedAt(LocalDateTime.now());
        testTicket.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Successful analysis")
    class SuccessfulAnalysis {

        @Test
        @DisplayName("should return structured analysis when AI responds correctly")
        void shouldReturnAnalysisOnSuccess() {
            String validJson = """
                    {
                        "category": "ACCOUNT",
                        "priority": "HIGH",
                        "summary": "Customer cannot log in after resetting their password.",
                        "sentiment": "FRUSTRATED",
                        "recommendedActions": ["Verify password reset status", "Check account lock status"],
                        "suggestedResponse": "We apologize for the inconvenience. Let us check your account status."
                    }
                    """;

            mockChatModelResponse(validJson);

            TicketAnalysisResult result = ticketAnalysisService.analyzeTicket(testTicket);

            assertThat(result).isNotNull();
            assertThat(result.getCategory()).isEqualTo("ACCOUNT");
            assertThat(result.getPriority()).isEqualTo("HIGH");
            assertThat(result.getSummary()).contains("password");
            assertThat(result.getSentiment()).isEqualTo("FRUSTRATED");
            assertThat(result.getRecommendedActions()).hasSize(2);
            assertThat(result.getSuggestedResponse()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("AI provider failures")
    class ProviderFailures {

        @Test
        @DisplayName("should throw AiServiceException on timeout")
        void shouldThrowOnTimeout() {
            when(chatModel.call(any(Prompt.class)))
                    .thenThrow(new RuntimeException("Connection timed out"));

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.TIMEOUT);
                    });
        }

        @Test
        @DisplayName("should throw AiServiceException when provider is unavailable")
        void shouldThrowOnConnectionRefused() {
            when(chatModel.call(any(Prompt.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.PROVIDER_UNAVAILABLE);
                    });
        }

        @Test
        @DisplayName("should throw AiServiceException on unknown error")
        void shouldThrowOnUnknownError() {
            when(chatModel.call(any(Prompt.class)))
                    .thenThrow(new RuntimeException("Something unexpected happened"));

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.UNKNOWN);
                    });
        }
    }

    @Nested
    @DisplayName("Malformed AI responses")
    class MalformedResponses {

        @Test
        @DisplayName("should throw on empty AI response")
        void shouldThrowOnEmptyResponse() {
            mockChatModelResponse("");

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.MALFORMED_RESPONSE);
                    });
        }

        @Test
        @DisplayName("should throw on null content response")
        void shouldThrowOnNullContent() {
            Generation generation = new Generation((String) null);
            ChatResponse chatResponse = new ChatResponse(java.util.List.of(generation));

            when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.MALFORMED_RESPONSE);
                    });
        }
    }

    @Nested
    @DisplayName("Validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("should throw when summary is missing")
        void shouldThrowWhenSummaryMissing() {
            String json = """
                    {
                        "category": "ACCOUNT",
                        "priority": "HIGH",
                        "summary": "",
                        "sentiment": "FRUSTRATED",
                        "recommendedActions": ["Action 1"],
                        "suggestedResponse": "Response"
                    }
                    """;
            mockChatModelResponse(json);

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.VALIDATION_FAILURE);
                    });
        }

        @Test
        @DisplayName("should throw when recommended actions are empty")
        void shouldThrowWhenNoRecommendedActions() {
            String json = """
                    {
                        "category": "ACCOUNT",
                        "priority": "HIGH",
                        "summary": "Cannot login",
                        "sentiment": "FRUSTRATED",
                        "recommendedActions": [],
                        "suggestedResponse": "Response"
                    }
                    """;
            mockChatModelResponse(json);

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.VALIDATION_FAILURE);
                    });
        }

        @Test
        @DisplayName("should throw when category is missing")
        void shouldThrowWhenCategoryMissing() {
            String json = """
                    {
                        "category": "",
                        "priority": "HIGH",
                        "summary": "Cannot login",
                        "sentiment": "FRUSTRATED",
                        "recommendedActions": ["Action 1"],
                        "suggestedResponse": "Response"
                    }
                    """;
            mockChatModelResponse(json);

            assertThatThrownBy(() -> ticketAnalysisService.analyzeTicket(testTicket))
                    .isInstanceOf(AiServiceException.class)
                    .satisfies(ex -> {
                        AiServiceException aiEx = (AiServiceException) ex;
                        assertThat(aiEx.getErrorType()).isEqualTo(AiErrorType.VALIDATION_FAILURE);
                    });
        }
    }

    private void mockChatModelResponse(String content) {
        Generation generation = new Generation(content);
        ChatResponse chatResponse = new ChatResponse(java.util.List.of(generation));

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    }
}
