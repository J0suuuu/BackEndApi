package com.hbc.tickets.controller;

import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.User;
import com.hbc.tickets.model.Cart;
import com.hbc.tickets.model.CartItem;
import com.hbc.tickets.model.CartItemResponse;
import com.hbc.tickets.repository.EventRepository;
import com.hbc.tickets.repository.UserRepository;
import com.hbc.tickets.repository.CartRepository;
import com.hbc.tickets.repository.CartItemRepository; // Añadir el repositorio de CartItem
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events/cart")
public class CartController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;  // Repositorio para CartItem

    // Agregar un evento al carrito
    @PostMapping("/add/{id}/{quantity}")
    public ResponseEntity<?> addToCart(@PathVariable Long id, @PathVariable int quantity, @RequestHeader("Authorization") String token) {
        // Obtener el usuario autenticado usando el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

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

        if (event.getPrice() <= 0) {
            return ResponseEntity.status(400).body("El evento no tiene un precio válido.");
        }

        if (quantity <= 0 || quantity > event.getAvailableTickets()) {
            return ResponseEntity.status(400).body("Cantidad de entradas no válida.");
        }

        // Buscar el carrito del usuario o crear uno nuevo si no existe
        Cart cart = cartRepository.findByUser(user).orElse(new Cart());
        if (cart.getUser() == null) {
            cart.setUser(user);
        }

        // Verificar si el evento ya está en el carrito
        Optional<CartItem> existingCartItemOpt = cart.getItems().stream()
            .filter(item -> item.getEvent().getId().equals(id))
            .findFirst();

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();

            int newQuantity = existingCartItem.getQuantity() + quantity;

            if (newQuantity > event.getAvailableTickets()) {
                return ResponseEntity.status(400).body("Cantidad total supera las entradas disponibles.");
            }

            existingCartItem.setQuantity(newQuantity);
            cartItemRepository.save(existingCartItem);
        } else {
            // Crear un nuevo CartItem
            CartItem cartItem = new CartItem();
            cartItem.setEvent(event);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            cart.getItems().add(cartItem);
        }

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
            cart.getItems().removeIf(cartItem -> cartItem.getEvent().getId().equals(id));
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
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.status(404).body("El carrito está vacío.");
        }

        // Crear una lista de respuestas con solo la ID del evento y la cantidad
        List<CartItemResponse> response = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            response.add(new CartItemResponse(cartItem.getEvent().getId(), cartItem.getQuantity()));
        }

        return ResponseEntity.ok(response);  // Devolver solo la ID del evento y la cantidad
    }
 // Método para actualizar la cantidad de entradas en el carrito
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateQuantityInCart(@PathVariable Long id, 
                                                  @RequestParam int quantity, 
                                                  @RequestHeader("Authorization") String token) {
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
        if (cart == null) {
            return ResponseEntity.status(404).body("Carrito no encontrado.");
        }

        // Buscar el CartItem que corresponde al evento
        CartItem cartItem = cart.getItems().stream()
                                 .filter(item -> item.getEvent().getId().equals(id))
                                 .findFirst()
                                 .orElse(null);

        if (cartItem == null) {
            return ResponseEntity.status(404).body("Evento no encontrado en el carrito.");
        }

        // Verificar que la cantidad no sea mayor a las entradas disponibles
        if (quantity > cartItem.getEvent().getAvailableTickets() || quantity < 1) {
            return ResponseEntity.status(400).body("Cantidad no válida.");
        }

        // Actualizar la cantidad de entradas
        cartItem.setQuantity(quantity);

        // Guardar la actualización en el carrito
        cartRepository.save(cart);

        return ResponseEntity.ok("Cantidad de entradas actualizada.");
    }
    
}
