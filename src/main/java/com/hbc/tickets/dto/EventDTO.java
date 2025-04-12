package com.hbc.tickets.dto;

import java.util.Date;
import java.util.List;

public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private Date date;
    private int soldTickets;
    private int availableTickets;
    private String imageUrl;
    private String localizacion;
    private int price;
    private String organizerUsername;
    private String eventUrl;
    private List<CategoryDTO> categories;  // Lista de nombres de categorías

    // Constructor actualizado
    public EventDTO(Long id, String title, String description, Date date, int soldTickets, int availableTickets, String imageUrl, String localizacion, int price, String organizerUsername, String eventUrl, List<CategoryDTO> categories) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.soldTickets = soldTickets;
        this.availableTickets = availableTickets;
        this.imageUrl = imageUrl;
        this.localizacion = localizacion;
        this.price = price;
        this.organizerUsername = organizerUsername;
        this.eventUrl = eventUrl;
        this.categories = categories;
    }

    // Getters y setters
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getOrganizerUsername() {
        return organizerUsername;
    }

    public void setOrganizerUsername(String organizerUsername) {
        this.organizerUsername = organizerUsername;
    }

    public String getEventUrl() {
        return eventUrl;
    }

    public void setEventUrl(String eventUrl) {
        this.eventUrl = eventUrl;
    }

    public List<CategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }
}
