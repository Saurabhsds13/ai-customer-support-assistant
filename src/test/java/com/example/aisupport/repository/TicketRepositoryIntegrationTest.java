package com.example.aisupport.repository;

import com.example.aisupport.BaseIntegrationTest;
import com.example.aisupport.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TicketRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private Customer savedCustomer;
    private User savedAgent;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail("jane.smith@example.com");
        savedCustomer = customerRepository.save(customer);

        User agent = new User();
        agent.setUsername("agent1");
        agent.setEmail("agent1@example.com");
        agent.setPassword("hashedpassword");
        agent.setRole(Role.SUPPORT_AGENT);
        savedAgent = userRepository.save(agent);
    }

    @Test
    @DisplayName("should save and retrieve ticket")
    void shouldSaveAndRetrieveTicket() {
        Ticket ticket = createTicket("Test Issue", TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);

        Optional<Ticket> found = ticketRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Issue");
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should find tickets by status")
    void shouldFindTicketsByStatus() {
        ticketRepository.save(createTicket("Open Ticket 1", TicketStatus.OPEN));
        ticketRepository.save(createTicket("Open Ticket 2", TicketStatus.OPEN));
        ticketRepository.save(createTicket("Closed Ticket", TicketStatus.CLOSED));

        List<Ticket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
        List<Ticket> closedTickets = ticketRepository.findByStatus(TicketStatus.CLOSED);

        assertThat(openTickets).hasSize(2);
        assertThat(closedTickets).hasSize(1);
    }

    @Test
    @DisplayName("should find tickets by customer ID")
    void shouldFindTicketsByCustomerId() {
        ticketRepository.save(createTicket("Ticket for customer", TicketStatus.OPEN));

        List<Ticket> tickets = ticketRepository.findByCustomerId(savedCustomer.getId());

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getTitle()).isEqualTo("Ticket for customer");
    }

    @Test
    @DisplayName("should find tickets by assigned agent ID")
    void shouldFindTicketsByAssignedAgentId() {
        Ticket ticket = createTicket("Assigned Ticket", TicketStatus.IN_PROGRESS);
        ticket.setAssignedAgent(savedAgent);
        ticketRepository.save(ticket);

        List<Ticket> tickets = ticketRepository.findByAssignedAgentId(savedAgent.getId());

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getTitle()).isEqualTo("Assigned Ticket");
    }

    @Test
    @DisplayName("should return empty list when no tickets match status")
    void shouldReturnEmptyListWhenNoMatch() {
        List<Ticket> tickets = ticketRepository.findByStatus(TicketStatus.RESOLVED);

        assertThat(tickets).isEmpty();
    }

    private Ticket createTicket(String title, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription("Description for " + title);
        ticket.setStatus(status);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setCategory(TicketCategory.TECHNICAL);
        ticket.setCustomer(savedCustomer);
        return ticket;
    }
}
