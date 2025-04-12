package com.hbc.tickets.repository;

import com.hbc.tickets.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
