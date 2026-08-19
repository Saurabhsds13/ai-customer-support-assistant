package com.example.aisupport.dto.ticket;

import com.example.aisupport.entity.TicketCategory;
import com.example.aisupport.entity.TicketPriority;
import com.example.aisupport.entity.TicketStatus;
import jakarta.validation.constraints.Size;

public class UpdateTicketRequest {

    @Size(min = 1, max = 150, message = "Title must be between 1 and 150 characters")
    private String title;

    @Size(min = 1, max = 2000, message = "Description must be between 1 and 2000 characters")
    private String description;

    private TicketStatus status;

    private TicketPriority priority;

    private TicketCategory category;

    public UpdateTicketRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }
}
