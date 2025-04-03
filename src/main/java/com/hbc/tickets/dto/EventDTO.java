package com.hbc.tickets.dto;

import java.util.Date;

public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private Date date;
    private int soldTickets;
    private int availableTickets;
    private String imageUrl;
    private String localizacion;
    private String organizerUsername;  // Solo el nombre de usuario del organizador

    public EventDTO(Long id, String title, String description, Date date, int soldTickets, int availableTickets, String imageUrl, String localizacion, String organizerUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.soldTickets = soldTickets;
        this.availableTickets = availableTickets;
        this.imageUrl = imageUrl;
        this.localizacion = localizacion;
        this.organizerUsername = organizerUsername;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(int soldTickets) {
        this.soldTickets = soldTickets;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getOrganizerUsername() {
        return organizerUsername;
    }

    public void setOrganizerUsername(String organizerUsername) {
        this.organizerUsername = organizerUsername;
    }
}
