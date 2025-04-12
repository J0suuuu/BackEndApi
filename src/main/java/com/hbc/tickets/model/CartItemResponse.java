package com.hbc.tickets.model;

public class CartItemResponse {
    private Long eventId;
    private int quantity;

    public CartItemResponse(Long eventId, int quantity) {
        this.eventId = eventId;
        this.quantity = quantity;
    }

    // Getters y Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
