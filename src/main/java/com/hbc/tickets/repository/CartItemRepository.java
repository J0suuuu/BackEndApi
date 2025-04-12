package com.hbc.tickets.repository;

import com.hbc.tickets.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Aquí puedes agregar métodos personalizados si es necesario
}
