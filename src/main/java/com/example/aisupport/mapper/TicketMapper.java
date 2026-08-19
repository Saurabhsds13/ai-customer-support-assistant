package com.example.aisupport.mapper;

import com.example.aisupport.dto.ticket.CreateTicketRequest;
import com.example.aisupport.dto.ticket.TicketResponse;
import com.example.aisupport.entity.Customer;
import com.example.aisupport.entity.Ticket;
import com.example.aisupport.entity.TicketStatus;
import com.example.aisupport.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public Ticket toEntity(CreateTicketRequest request, Customer customer) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setCategory(request.getCategory());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCustomer(customer);
        return ticket;
    }

    public TicketResponse toResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setCategory(ticket.getCategory());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setResolvedAt(ticket.getResolvedAt());

        if (ticket.getCustomer() != null) {
            TicketResponse.CustomerSummary customerSummary = new TicketResponse.CustomerSummary();
            customerSummary.setId(ticket.getCustomer().getId());
            customerSummary.setFirstName(ticket.getCustomer().getFirstName());
            customerSummary.setLastName(ticket.getCustomer().getLastName());
            customerSummary.setEmail(ticket.getCustomer().getEmail());
            response.setCustomer(customerSummary);
        }

        if (ticket.getAssignedAgent() != null) {
            TicketResponse.AgentSummary agentSummary = new TicketResponse.AgentSummary();
            agentSummary.setId(ticket.getAssignedAgent().getId());
            agentSummary.setUsername(ticket.getAssignedAgent().getUsername());
            agentSummary.setEmail(ticket.getAssignedAgent().getEmail());
            response.setAssignedAgent(agentSummary);
        }

        return response;
    }
}
