package com.hbc.tickets.controller;

import com.hbc.tickets.model.Cart;
import com.hbc.tickets.model.CartItem;
import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.CartRepository;
import com.hbc.tickets.repository.EventRepository;
import com.hbc.tickets.repository.UserRepository;
import com.hbc.tickets.service.EmailService;
import com.hbc.tickets.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

	@Autowired
	private TicketService ticketService;
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private EmailService emailService; // Usamos el EmailService que ya tenías creado


    // Realizar la compra
 // Realizar la compra
    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getEvents().isEmpty()) {
            return ResponseEntity.status(404).body("El carrito está vacío.");
        }

        List<CartItem> eventsInCart = cart.getEvents();
        StringBuilder emailContent = new StringBuilder("Gracias por comprar las siguientes entradas:\n");

        eventsInCart.forEach(cartItem -> {
            emailContent.append(String.format("%s - %d entradas\n", cartItem.getEvent().getTitle(), cartItem.getQuantity()));
        });

        // Procesar la compra y actualizar los tickets disponibles y vendidos
        for (CartItem item : eventsInCart) {
            Event event = item.getEvent();
            
            // Comprobar si hay suficientes tickets disponibles
            if (event.getAvailableTickets() < item.getQuantity()) {
                return ResponseEntity.status(400).body("No hay suficientes tickets disponibles para el evento: " + event.getTitle());
            }

            // Restar los tickets disponibles y sumar los vendidos
            event.setAvailableTickets(event.getAvailableTickets() - item.getQuantity());
            event.setSoldTickets(event.getSoldTickets() + item.getQuantity());
            
            // Guardar los cambios en el evento
            eventRepository.save(event);
            
            // Generar los tickets para el usuario
            ticketService.generateTickets(user, event, item.getQuantity());
        }

        // Limpiar el carrito después de la compra
        cart.getEvents().clear();
        cartRepository.save(cart);

        // Enviar el correo de confirmación
        try {
            emailService.sendEmail(user.getEmail(), "Gracias por tu compra", emailContent.toString());
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Error al enviar el correo electrónico.");
        }

        return ResponseEntity.ok("Compra realizada con éxito. Te hemos enviado un correo.");
    }



    // Método para extraer el nombre de usuario del token (esto es solo un ejemplo, ajusta a tu implementación de JWT)
    private String extractUsernameFromToken(String token) {
        // Lógica para extraer el nombre de usuario del token (esto depende de cómo esté implementada tu seguridad JWT)
        return "nombreDeUsuario";  // Deberías extraerlo del token JWT.
    }
}

