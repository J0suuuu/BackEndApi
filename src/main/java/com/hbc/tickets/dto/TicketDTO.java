package com.hbc.tickets.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hbc.tickets.model.Ticket;

public class TicketDTO {
    @JsonProperty("eventTitle") // Opcional: Anotación para asegurar que se serializa con el nombre correcto
    private String eventTitle;

    @JsonProperty("code") // Opcional: Anotación para asegurar que se serializa con el nombre correcto
    private String code;

	private Long eventId;

    // Constructor
    public TicketDTO(Ticket ticket) {
    	this.eventId = ticket.getEvent().getId();
        this.eventTitle = ticket.getEvent().getTitle();
        this.code = ticket.getCode();
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    // Getters y Setters
    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
