package com.example.aisupport.controller;

import com.example.aisupport.ai.exception.AiServiceException;
import com.example.aisupport.ai.exception.AiServiceException.AiErrorType;
import com.example.aisupport.ai.model.TicketAnalysisResult;
import com.example.aisupport.ai.service.TicketAnalysisService;
import com.example.aisupport.config.SecurityConfig;
import com.example.aisupport.entity.*;
import com.example.aisupport.exception.GlobalExceptionHandler;
import com.example.aisupport.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketAiController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class TicketAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private TicketAnalysisService ticketAnalysisService;

    private Ticket createTestTicket() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Cannot login");
        ticket.setDescription("I reset my password but still cannot log in.");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setCategory(TicketCategory.ACCOUNT);
        ticket.setCustomer(customer);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticket;
    }

    private TicketAnalysisResult createSuccessResult() {
        TicketAnalysisResult result = new TicketAnalysisResult();
        result.setCategory("ACCOUNT");
        result.setPriority("HIGH");
        result.setSummary("Customer cannot log in after password reset.");
        result.setSentiment("FRUSTRATED");
        result.setRecommendedActions(List.of("Verify password reset", "Check account lock"));
        result.setSuggestedResponse("We apologize for the inconvenience.");
        return result;
    }

    @Nested
    @DisplayName("POST /api/tickets/{id}/ai/analyze")
    class AnalyzeTicketEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with analysis on success")
        void shouldReturnAnalysis() throws Exception {
            Ticket ticket = createTestTicket();
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketAnalysisService.analyzeTicket(any(Ticket.class))).thenReturn(createSuccessResult());

            mockMvc.perform(post("/api/tickets/1/ai/analyze"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ticketId").value(1))
                    .andExpect(jsonPath("$.category").value("ACCOUNT"))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.summary").value("Customer cannot log in after password reset."))
                    .andExpect(jsonPath("$.sentiment").value("FRUSTRATED"))
                    .andExpect(jsonPath("$.recommendedActions").isArray())
                    .andExpect(jsonPath("$.recommendedActions[0]").value("Verify password reset"))
                    .andExpect(jsonPath("$.suggestedResponse").isNotEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenTicketNotFound() throws Exception {
            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/tickets/999/ai/analyze"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Ticket not found with id: '999'"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 503 when AI provider is unavailable")
        void shouldReturn503WhenProviderUnavailable() throws Exception {
            Ticket ticket = createTestTicket();
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketAnalysisService.analyzeTicket(any(Ticket.class)))
                    .thenThrow(new AiServiceException("AI service unavailable", AiErrorType.PROVIDER_UNAVAILABLE));

            mockMvc.perform(post("/api/tickets/1/ai/analyze"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("AI Service Error"))
                    .andExpect(jsonPath("$.message").value("AI service unavailable"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 503 when AI times out")
        void shouldReturn503WhenTimeout() throws Exception {
            Ticket ticket = createTestTicket();
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketAnalysisService.analyzeTicket(any(Ticket.class)))
                    .thenThrow(new AiServiceException("AI service timed out", AiErrorType.TIMEOUT));

            mockMvc.perform(post("/api/tickets/1/ai/analyze"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("AI Service Error"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 503 when AI response is malformed")
        void shouldReturn503WhenMalformedResponse() throws Exception {
            Ticket ticket = createTestTicket();
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketAnalysisService.analyzeTicket(any(Ticket.class)))
                    .thenThrow(new AiServiceException("AI returned malformed response", AiErrorType.MALFORMED_RESPONSE));

            mockMvc.perform(post("/api/tickets/1/ai/analyze"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("AI Service Error"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 503 when AI response fails validation")
        void shouldReturn503WhenValidationFails() throws Exception {
            Ticket ticket = createTestTicket();
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketAnalysisService.analyzeTicket(any(Ticket.class)))
                    .thenThrow(new AiServiceException("Missing summary", AiErrorType.VALIDATION_FAILURE));

            mockMvc.perform(post("/api/tickets/1/ai/analyze"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("AI Service Error"));
        }
    }
}
