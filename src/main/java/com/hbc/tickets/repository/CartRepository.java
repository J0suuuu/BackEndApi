package com.hbc.tickets.repository;

import com.hbc.tickets.model.Cart;
import com.hbc.tickets.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
