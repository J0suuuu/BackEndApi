package com.hbc.tickets.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    // This is the correct list of CartItems
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> events = new ArrayList<>(); // This is where the cart items are stored

    // Getter and Setter for events (cart items)
    public List<CartItem> getEvents() {
        return events;  // This returns the list of events or cart items
    }

    public void setEvents(List<CartItem> events) {
        this.events = events;
    }

    // You can rename getEvents to getItems() if you prefer that name
    public List<CartItem> getItems() {
        return events;  // This returns the same list, just under a different method name
    }

    public void setItems(List<CartItem> items) {
        this.events = items;
    }

    // Additional getters and setters for Cart ID and User
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
