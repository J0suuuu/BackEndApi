package com.hbc.tickets.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.Ticket;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.TicketRepository;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public Ticket generateTicket(User user, Event event) {
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setCode(UUID.randomUUID().toString()); // Código aleatorio
        return ticketRepository.save(ticket);
    }

    public void generateTickets(User user, Event event, int quantity) {
        for (int i = 0; i < quantity; i++) {
            generateTicket(user, event);
        }
    }

    public List<Ticket> getUserTickets(User user) {
        return ticketRepository.findAllByUser(user);
    }

    public boolean validateTicketCode(String code) {
        return ticketRepository.findByCode(code).isPresent();
    }
}
