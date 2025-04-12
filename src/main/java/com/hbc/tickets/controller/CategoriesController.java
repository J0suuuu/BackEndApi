package com.hbc.tickets.controller;

import com.hbc.tickets.dto.CategoryDTO;
import com.hbc.tickets.model.Category;
import com.hbc.tickets.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CategoriesController {

    @Autowired
    private CategoryRepository categoryRepository;

    // Endpoint para obtener todas las categorías
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        // Convertir las categorías a DTOs
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName())) // Mapear solo id y name
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }
}
