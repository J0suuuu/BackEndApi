package com.hbc.tickets.repository;

import com.hbc.tickets.model.Cart;
import com.hbc.tickets.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Buscar carrito por usuario
    Optional<Cart> findByUser(User user);

    // Buscar carrito por el nombre de usuario (esto es útil si quieres buscar directamente por username)
    Optional<Cart> findByUserUsername(String username);

}
