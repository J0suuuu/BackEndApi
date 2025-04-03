package com.hbc.tickets.controller;

import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.User;
import com.hbc.tickets.model.Cart;
import com.hbc.tickets.repository.EventRepository;
import com.hbc.tickets.repository.UserRepository;
import com.hbc.tickets.repository.CartRepository; // Si decides usar un modelo Cart
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@RestController
@RequestMapping("/api/events/cart")
public class CartController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CartRepository cartRepository; // Si usas un modelo Cart

    // Agregar un evento al carrito
    @PostMapping("/add/{id}")
    public ResponseEntity<?> addToCart(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // Obtener el usuario autenticado usando el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Obtiene el nombre de usuario desde el token

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        Optional<Event> eventOptional = eventRepository.findById(id);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        Event event = eventOptional.get();

        // Si usas un modelo Cart, puedes buscar o crear un carrito para el usuario
        Cart cart = cartRepository.findByUser(user).orElse(new Cart());
        cart.getEvents().add(event);

        // Guardar el carrito actualizado
        cartRepository.save(cart);

        return ResponseEntity.ok("Evento agregado al carrito.");
    }

    // Eliminar un evento del carrito
    @PostMapping("/remove/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // Obtener el usuario autenticado usando el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Obtiene el nombre de usuario desde el token

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        Optional<Event> eventOptional = eventRepository.findById(id);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        Event event = eventOptional.get();

        // Buscar el carrito del usuario
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cart.getEvents().remove(event);
            cartRepository.save(cart);
            return ResponseEntity.ok("Evento eliminado del carrito.");
        }

        return ResponseEntity.status(404).body("Carrito no encontrado.");
    }

    // Obtener los eventos en el carrito
    @GetMapping("/view")
    public ResponseEntity<?> viewCart(@RequestHeader("Authorization") String token) {
        // Obtener el usuario autenticado usando el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Obtiene el nombre de usuario desde el token

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Buscar el carrito del usuario
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getEvents().isEmpty()) {
            return ResponseEntity.status(404).body("El carrito está vacío.");
        }

        return ResponseEntity.ok(cart.getEvents());  // Devolver los eventos en el carrito
    }
}
