package com.hbc.tickets.controller;

import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.EventRepository;
import com.hbc.tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events/favorites")
public class FavoriteController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    // Agregar un evento a favoritos
    @PostMapping("/add/{id}")
    public ResponseEntity<?> addFavorite(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // Obtener el usuario autenticado usando el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Obtiene el nombre de usuario desde el token

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username);  // Aquí no usamos Optional
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        Optional<Event> eventOptional = eventRepository.findById(id);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        Event event = eventOptional.get();
        user.getFavorites().add(event);  // Agregar evento a los favoritos del usuario
        userRepository.save(user);  // Guardar usuario con el evento agregado a favoritos

        return ResponseEntity.ok("Evento agregado a favoritos.");
    }

    // Eliminar un evento de favoritos
    @PostMapping("/remove/{id}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // Obtener el usuario autenticado usando el token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Obtiene el nombre de usuario desde el token

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username);  // Aquí no usamos Optional
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        Optional<Event> eventOptional = eventRepository.findById(id);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        Event event = eventOptional.get();
        user.getFavorites().remove(event);  // Eliminar evento de los favoritos del usuario
        userRepository.save(user);  // Guardar usuario con el evento eliminado de favoritos

        return ResponseEntity.ok("Evento eliminado de favoritos.");
    }

    // Obtener los eventos favoritos del usuario
    @GetMapping("/list")
    public ResponseEntity<?> getFavorites(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Obtiene el nombre de usuario desde el token

        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username);  // Aquí no usamos Optional
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Devuelve solo los IDs de los eventos favoritos
        List<Long> favoriteEventIds = user.getFavorites().stream()
            .map(Event::getId)
            .collect(Collectors.toList());

        return ResponseEntity.ok(favoriteEventIds);  // Lista de IDs
    }

}
