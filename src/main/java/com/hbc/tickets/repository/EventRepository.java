package com.hbc.tickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbc.tickets.model.Event;

import java.util.Date;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Buscar eventos por título (insensible a mayúsculas/minúsculas)
    List<Event> findByTitleContainingIgnoreCase(String title);

    // Buscar eventos por descripción (insensible a mayúsculas/minúsculas)
    List<Event> findByDescriptionContainingIgnoreCase(String description);

    // Buscar eventos por fecha
    List<Event> findByDate(Date date);

    // Buscar eventos por localización (insensible a mayúsculas/minúsculas)
    List<Event> findByLocalizacionContainingIgnoreCase(String localizacion);
    
    List<Event> findAllByOrderByDateAsc();

	List<Event> findAllByOrderBySoldTicketsDesc();
   }
