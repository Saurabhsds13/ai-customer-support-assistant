package com.example.aisupport.service;

import com.example.aisupport.dto.ticket.CreateTicketRequest;
import com.example.aisupport.dto.ticket.TicketResponse;
import com.example.aisupport.dto.ticket.UpdateTicketRequest;
import com.example.aisupport.entity.*;
import com.example.aisupport.exception.ResourceNotFoundException;
import com.example.aisupport.mapper.TicketMapper;
import com.example.aisupport.repository.CustomerRepository;
import com.example.aisupport.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    private Customer testCustomer;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john@example.com");

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("Test Description");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setPriority(TicketPriority.MEDIUM);
        testTicket.setCategory(TicketCategory.TECHNICAL);
        testTicket.setCustomer(testCustomer);
        testTicket.setCreatedAt(LocalDateTime.now());
        testTicket.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createTicket")
    class CreateTicketTests {

        @Test
        @DisplayName("should create ticket when customer exists")
        void shouldCreateTicketWhenCustomerExists() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("New Ticket");
            request.setDescription("New Description");
            request.setCustomerId(1L);
            request.setPriority(TicketPriority.HIGH);
            request.setCategory(TicketCategory.BILLING);

            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

            TicketResponse response = ticketService.createTicket(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            verify(customerRepository).findById(1L);
            verify(ticketRepository).save(any(Ticket.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when customer does not exist")
        void shouldThrowWhenCustomerNotFound() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("New Ticket");
            request.setDescription("New Description");
            request.setCustomerId(999L);
            request.setPriority(TicketPriority.HIGH);
            request.setCategory(TicketCategory.BILLING);

            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.createTicket(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer");

            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllTickets")
    class GetAllTicketsTests {

        @Test
        @DisplayName("should return all tickets")
        void shouldReturnAllTickets() {
            when(ticketRepository.findAll()).thenReturn(List.of(testTicket));

            List<TicketResponse> tickets = ticketService.getAllTickets();

            assertThat(tickets).hasSize(1);
            assertThat(tickets.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty list when no tickets exist")
        void shouldReturnEmptyList() {
            when(ticketRepository.findAll()).thenReturn(List.of());

            List<TicketResponse> tickets = ticketService.getAllTickets();

            assertThat(tickets).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTicketById")
    class GetTicketByIdTests {

        @Test
        @DisplayName("should return ticket when found")
        void shouldReturnTicketWhenFound() {
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

            TicketResponse response = ticketService.getTicketById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Ticket");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ticket not found")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.getTicketById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Ticket");
        }
    }

    @Nested
    @DisplayName("updateTicket")
    class UpdateTicketTests {

        @Test
        @DisplayName("should update mutable fields")
        void shouldUpdateMutableFields() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Updated Title");
            request.setPriority(TicketPriority.CRITICAL);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

            TicketResponse response = ticketService.updateTicket(1L, request);

            assertThat(response).isNotNull();
            verify(ticketRepository).save(any(Ticket.class));
        }

        @Test
        @DisplayName("should set resolvedAt when status changes to RESOLVED")
        void shouldSetResolvedAtWhenResolved() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setStatus(TicketStatus.RESOLVED);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ticketService.updateTicket(1L, request);

            assertThat(testTicket.getResolvedAt()).isNotNull();
            assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ticket not found")
        void shouldThrowWhenTicketNotFoundOnUpdate() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Updated");

            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.updateTicket(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteTicket")
    class DeleteTicketTests {

        @Test
        @DisplayName("should delete ticket when found")
        void shouldDeleteTicketWhenFound() {
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

            ticketService.deleteTicket(1L);

            verify(ticketRepository).delete(testTicket);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when ticket not found")
        void shouldThrowWhenTicketNotFoundOnDelete() {
            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.deleteTicket(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
