package com.example.aisupport.controller;

import com.example.aisupport.ai.model.TicketAnalysisResult;
import com.example.aisupport.ai.service.TicketAnalysisService;
import com.example.aisupport.dto.ai.TicketAnalysisResponse;
import com.example.aisupport.entity.Ticket;
import com.example.aisupport.exception.ResourceNotFoundException;
import com.example.aisupport.repository.TicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketAiController {

    private final TicketRepository ticketRepository;
    private final TicketAnalysisService ticketAnalysisService;

    public TicketAiController(TicketRepository ticketRepository,
                              TicketAnalysisService ticketAnalysisService) {
        this.ticketRepository = ticketRepository;
        this.ticketAnalysisService = ticketAnalysisService;
    }

    @PostMapping("/{id}/ai/analyze")
    public ResponseEntity<TicketAnalysisResponse> analyzeTicket(@PathVariable Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        TicketAnalysisResult result = ticketAnalysisService.analyzeTicket(ticket);

        TicketAnalysisResponse response = mapToResponse(id, result);
        return ResponseEntity.ok(response);
    }

    private TicketAnalysisResponse mapToResponse(Long ticketId, TicketAnalysisResult result) {
        TicketAnalysisResponse response = new TicketAnalysisResponse();
        response.setTicketId(ticketId);
        response.setCategory(result.getCategory());
        response.setPriority(result.getPriority());
        response.setSummary(result.getSummary());
        response.setSentiment(result.getSentiment());
        response.setRecommendedActions(result.getRecommendedActions());
        response.setSuggestedResponse(result.getSuggestedResponse());
        return response;
    }
}
