package com.hbc.tickets.controller;

import com.hbc.tickets.dto.EventDTO;
import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.Role;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.EventRepository;
import com.hbc.tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${upload.dir}")
    private String uploadDir; // Ruta donde se guardarán las imágenes

    // Endpoint para obtener eventos por fecha
    @GetMapping("/filter/bydate")
    public ResponseEntity<?> filterEventsByDate() {
        List<Event> events = eventRepository.findAllByOrderByDateAsc();

        if (events.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron eventos.");
        }

        // Mapear eventos a DTO
        List<EventDTO> eventDTOs = events.stream().map(event -> new EventDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getSoldTickets(),
            event.getAvailableTickets(),
            event.getImageUrl(),
            event.getLocalizacion(),
            event.getOrganizer().getUsername()  // Solo el nombre de usuario del organizador
        )).collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/filter/bypopular")
    public ResponseEntity<?> filterEventsByPopularity() {
        List<Event> events = eventRepository.findAllByOrderBySoldTicketsDesc();

        if (events.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron eventos.");
        }

        // Mapear eventos a DTO
        List<EventDTO> eventDTOs = events.stream().map(event -> new EventDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getSoldTickets(),
            event.getAvailableTickets(),
            event.getImageUrl(),
            event.getLocalizacion(),
            event.getOrganizer().getUsername()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterEvents(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "localizacion", required = false) String localizacion) throws ParseException {

        // Lista para almacenar los eventos que coincidan con el filtro
        List<Event> filteredEvents = new ArrayList<>();

        if (title == null && description == null && date == null && localizacion == null) {
            filteredEvents = eventRepository.findAll();
        } else {
            if (title != null) {
                filteredEvents.addAll(eventRepository.findByTitleContainingIgnoreCase(title));
            }
            if (description != null) {
                filteredEvents.addAll(eventRepository.findByDescriptionContainingIgnoreCase(description));
            }
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date eventDate = sdf.parse(date);
                filteredEvents.addAll(eventRepository.findByDate(eventDate));
            }
            if (localizacion != null) {
                filteredEvents.addAll(eventRepository.findByLocalizacionContainingIgnoreCase(localizacion));
            }
        }

        if (filteredEvents.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron eventos con los parámetros especificados.");
        }

        // Mapear los eventos a DTOs antes de devolverlos
        List<EventDTO> eventDTOs = filteredEvents.stream().map(event -> new EventDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getSoldTickets(),
            event.getAvailableTickets(),
            event.getImageUrl(),
            event.getLocalizacion(),
            event.getOrganizer().getUsername()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam("available_tickets") int availableTickets,
            @RequestParam("localizacion") String localizacion, // nuevo parámetro
            @RequestParam("image") MultipartFile image, 
            @RequestHeader("Authorization") String token) throws Exception {

        // Extraer el nombre de usuario del token JWT
        String username = jwtUtil.extractUsername(token.substring(7)); // Eliminar "Bearer "

        // Buscar el usuario con el nombre de usuario extraído del token
        User user = userRepository.findByUsername(username);

        // Verificar si el usuario existe
        if (user == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado.");
        }

        // Verificar si el usuario tiene el rol adecuado (Organizador o Administrador)
        if (user.getRole() != Role.ORGANIZADOR && user.getRole() != Role.ADMINISTRADOR) {
            return ResponseEntity.status(403).body("No tienes permisos para crear eventos.");
        }

        // Convertir la fecha del evento
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date eventDate;
        try {
            eventDate = sdf.parse(date);
        } catch (ParseException e) {
            return ResponseEntity.status(400).body("Formato de fecha inválido.");
        }

        // Crear el evento
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setDate(eventDate);
        event.setAvailableTickets(availableTickets);
        event.setSoldTickets(0);
        event.setLocalizacion(localizacion); // Asignar la localización

        // Guardar la imagen si se recibe
        if (image != null && !image.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());

            // Definir la ruta de destino dentro de "static/images"
            Path targetLocation = Path.of(new ClassPathResource("static/images").getFile().getAbsolutePath(), originalFileName);

            // Si el archivo ya existe, agregar un sufijo numérico
            int fileCounter = 1;
            String fileName = originalFileName;
            while (Files.exists(targetLocation)) {
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                String baseFileName = fileName.substring(0, fileName.lastIndexOf("."));
                fileName = baseFileName + fileCounter + fileExtension;
                targetLocation = Path.of(new ClassPathResource("static/images").getFile().getAbsolutePath(), fileName);
                fileCounter++;
            }

            // Guardar la imagen
            try {
                Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Error al guardar la imagen.");
            }

            // Guardar solo el nombre del archivo (no la ruta completa)
            event.setImageUrl(fileName);
        }

        // Asociar el evento con el organizador (el usuario que lo crea)
        event.setOrganizer(user);
        eventRepository.save(event);

        return ResponseEntity.ok("Evento creado exitosamente.");
    }

}
