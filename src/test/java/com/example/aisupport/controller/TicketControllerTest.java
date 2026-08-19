package com.example.aisupport.controller;

import com.example.aisupport.dto.ticket.CreateTicketRequest;
import com.example.aisupport.dto.ticket.TicketResponse;
import com.example.aisupport.dto.ticket.UpdateTicketRequest;
import com.example.aisupport.entity.TicketCategory;
import com.example.aisupport.entity.TicketPriority;
import com.example.aisupport.entity.TicketStatus;
import com.example.aisupport.exception.GlobalExceptionHandler;
import com.example.aisupport.exception.ResourceNotFoundException;
import com.example.aisupport.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@Import({GlobalExceptionHandler.class, com.example.aisupport.config.SecurityConfig.class})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    private TicketResponse createSampleResponse() {
        TicketResponse response = new TicketResponse();
        response.setId(1L);
        response.setTitle("Test Ticket");
        response.setDescription("Test Description");
        response.setStatus(TicketStatus.OPEN);
        response.setPriority(TicketPriority.MEDIUM);
        response.setCategory(TicketCategory.TECHNICAL);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        TicketResponse.CustomerSummary customer = new TicketResponse.CustomerSummary();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");
        response.setCustomer(customer);

        return response;
    }

    @Nested
    @DisplayName("POST /api/tickets")
    class CreateTicketEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should create ticket and return 201")
        void shouldCreateTicket() throws Exception {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("New Ticket");
            request.setDescription("New Description");
            request.setCustomerId(1L);
            request.setPriority(TicketPriority.HIGH);
            request.setCategory(TicketCategory.BILLING);

            TicketResponse response = createSampleResponse();
            when(ticketService.createTicket(any(CreateTicketRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Ticket"))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleBlank() throws Exception {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("");
            request.setDescription("Description");
            request.setCustomerId(1L);
            request.setPriority(TicketPriority.HIGH);
            request.setCategory(TicketCategory.BILLING);

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when required fields are missing")
        void shouldReturn400WhenFieldsMissing() throws Exception {
            CreateTicketRequest request = new CreateTicketRequest();

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("New Ticket");
            request.setDescription("New Description");
            request.setCustomerId(999L);
            request.setPriority(TicketPriority.HIGH);
            request.setCategory(TicketCategory.BILLING);

            when(ticketService.createTicket(any(CreateTicketRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Customer", "id", 999L));

            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Customer not found with id: '999'"));
        }
    }

    @Nested
    @DisplayName("GET /api/tickets")
    class GetAllTicketsEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should return all tickets with 200")
        void shouldReturnAllTickets() throws Exception {
            TicketResponse response = createSampleResponse();
            when(ticketService.getAllTickets()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/tickets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("OPEN"))
                    .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                    .andExpect(jsonPath("$[0].category").value("TECHNICAL"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return empty list when no tickets")
        void shouldReturnEmptyList() throws Exception {
            when(ticketService.getAllTickets()).thenReturn(List.of());

            mockMvc.perform(get("/api/tickets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/tickets/{id}")
    class GetTicketByIdEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should return ticket with 200 when found")
        void shouldReturnTicket() throws Exception {
            TicketResponse response = createSampleResponse();
            when(ticketService.getTicketById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/tickets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.customer.firstName").value("John"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when ticket not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(ticketService.getTicketById(999L))
                    .thenThrow(new ResourceNotFoundException("Ticket", "id", 999L));

            mockMvc.perform(get("/api/tickets/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }

    @Nested
    @DisplayName("PUT /api/tickets/{id}")
    class UpdateTicketEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should update ticket and return 200")
        void shouldUpdateTicket() throws Exception {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Updated Title");
            request.setPriority(TicketPriority.CRITICAL);

            TicketResponse response = createSampleResponse();
            response.setTitle("Updated Title");
            response.setPriority(TicketPriority.CRITICAL);

            when(ticketService.updateTicket(eq(1L), any(UpdateTicketRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/tickets/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.priority").value("CRITICAL"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when ticket not found on update")
        void shouldReturn404WhenNotFoundOnUpdate() throws Exception {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Updated");

            when(ticketService.updateTicket(eq(999L), any(UpdateTicketRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Ticket", "id", 999L));

            mockMvc.perform(put("/api/tickets/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/tickets/{id}")
    class DeleteTicketEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should delete ticket and return 204")
        void shouldDeleteTicket() throws Exception {
            doNothing().when(ticketService).deleteTicket(1L);

            mockMvc.perform(delete("/api/tickets/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when ticket not found on delete")
        void shouldReturn404WhenNotFoundOnDelete() throws Exception {
            when(ticketService.getTicketById(999L))
                    .thenThrow(new ResourceNotFoundException("Ticket", "id", 999L));

            // Use doThrow for void methods
            org.mockito.Mockito.doThrow(new ResourceNotFoundException("Ticket", "id", 999L))
                    .when(ticketService).deleteTicket(999L);

            mockMvc.perform(delete("/api/tickets/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @WithMockUser
        @DisplayName("should return 400 for malformed JSON")
        void shouldReturn400ForMalformedJson() throws Exception {
            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Malformed request body"));
        }
    }
}
