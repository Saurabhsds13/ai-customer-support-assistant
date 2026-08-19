package com.example.aisupport.repository;

import com.example.aisupport.entity.Ticket;
import com.example.aisupport.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByAssignedAgentId(Long agentId);

    List<Ticket> findByCustomerId(Long customerId);
}
