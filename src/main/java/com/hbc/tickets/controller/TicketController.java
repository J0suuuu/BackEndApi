package com.hbc.tickets.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hbc.tickets.dto.TicketDTO;
import com.hbc.tickets.model.Ticket;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.UserRepository;
import com.hbc.tickets.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    // ✅ Ver tickets del usuario autenticado
    @GetMapping("/view")
    public ResponseEntity<?> viewTickets(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No se ha proporcionado un token válido.");
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no encontrado.");
        }

        List<Ticket> tickets = ticketService.getUserTickets(user);

        // Convertir a TicketDTO para evitar la recursividad y agregar el eventId
        List<TicketDTO> ticketDTOs = tickets.stream()
                .map(ticket -> new TicketDTO(ticket))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ticketDTOs);
    }




    // ✅ Verificar si un código de ticket es válido
    @PostMapping("/check")
    public ResponseEntity<String> checkTicket(@RequestParam String code) {
        boolean valid = ticketService.validateTicketCode(code);
        if (valid) {
            return ResponseEntity.ok("Ticket válido");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Código no válido");
        }
    }
}
