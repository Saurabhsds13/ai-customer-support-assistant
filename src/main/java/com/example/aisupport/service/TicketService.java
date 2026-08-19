package com.example.aisupport.service;

import com.example.aisupport.dto.ticket.CreateTicketRequest;
import com.example.aisupport.dto.ticket.TicketResponse;
import com.example.aisupport.dto.ticket.UpdateTicketRequest;
import com.example.aisupport.entity.Customer;
import com.example.aisupport.entity.Ticket;
import com.example.aisupport.entity.TicketStatus;
import com.example.aisupport.exception.ResourceNotFoundException;
import com.example.aisupport.mapper.TicketMapper;
import com.example.aisupport.repository.CustomerRepository;
import com.example.aisupport.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository,
                         CustomerRepository customerRepository,
                         TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Ticket ticket = ticketMapper.toEntity(request, customer);
        Ticket saved = ticketRepository.save(ticket);

        log.debug("Created ticket with id={} for customer id={}", saved.getId(), customer.getId());
        return ticketMapper.toResponse(saved);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = findTicketOrThrow(id);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = findTicketOrThrow(id);

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        }

        Ticket updated = ticketRepository.save(ticket);
        log.debug("Updated ticket id={}", id);
        return ticketMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = findTicketOrThrow(id);
        ticketRepository.delete(ticket);
        log.debug("Deleted ticket id={}", id);
    }

    private Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
    }
}
