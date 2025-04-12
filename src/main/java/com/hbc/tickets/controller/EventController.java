package com.hbc.tickets.controller;

import com.hbc.tickets.dto.CategoryDTO;
import com.hbc.tickets.dto.EventDTO;
import com.hbc.tickets.model.Category;
import com.hbc.tickets.model.Event;
import com.hbc.tickets.model.Role;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.CategoryRepository;
import com.hbc.tickets.repository.EventRepository;
import com.hbc.tickets.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private CategoryRepository categoryRepository;


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
        List<EventDTO> eventDTOs = events.stream().map(event -> {
            List<CategoryDTO> categoryDTOs = event.getCategories().stream()
                    .map(category -> new CategoryDTO(category.getId(), category.getName()))  // Mapeamos las categorías a CategoryDTO
                    .collect(Collectors.toList());

            return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getSoldTickets(),
                event.getAvailableTickets(),
                event.getImageUrl(),
                event.getLocalizacion(),
                event.getPrice(),
                event.getOrganizer().getUsername(),
                event.getEventUrl(),
                categoryDTOs  // Asignamos las categorías mapeadas
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/filter/bypopular")
    public ResponseEntity<?> filterEventsByPopularity() {
        List<Event> events = eventRepository.findAllByOrderBySoldTicketsDesc();

        if (events.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron eventos.");
        }

        // Mapear eventos a DTO
        List<EventDTO> eventDTOs = events.stream().map(event -> {
            List<CategoryDTO> categoryDTOs = event.getCategories().stream()
                    .map(category -> new CategoryDTO(category.getId(), category.getName()))  // Mapeamos las categorías a CategoryDTO
                    .collect(Collectors.toList());

            return new EventDTO(
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    event.getDate(),
                    event.getSoldTickets(),
                    event.getAvailableTickets(),
                    event.getImageUrl(),
                    event.getLocalizacion(),
                    event.getPrice(),
                    event.getOrganizer().getUsername(),
                    event.getEventUrl(),
                    categoryDTOs  // Asignamos las categorías mapeadas
                );
            }).collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterEvents(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "localizacion", required = false) String localizacion,
            @RequestParam(value = "category", required = false) Long categoryId) throws ParseException {

        // Lista para almacenar los eventos que coincidan con el filtro
        List<Event> filteredEvents = new ArrayList<>();

        if (title == null && description == null && date == null && localizacion == null && categoryId == null) {
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
            if (categoryId != null) {
                // Filtrar por categoría
                List<Event> eventsByCategory = eventRepository.findByCategoriesId(categoryId);
                filteredEvents.addAll(eventsByCategory);
            }
        }

        if (filteredEvents.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron eventos con los parámetros especificados.");
        }

        // Mapear los eventos a DTOs antes de devolverlos
        List<EventDTO> eventDTOs = filteredEvents.stream().map(event -> {
            List<CategoryDTO> categoryDTOs = event.getCategories().stream()
                    .map(category -> new CategoryDTO(category.getId(), category.getName())) // Extraemos los nombres de las categorías
                    .collect(Collectors.toList());

            return new EventDTO(
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    event.getDate(),
                    event.getSoldTickets(),
                    event.getAvailableTickets(),
                    event.getImageUrl(),
                    event.getLocalizacion(),
                    event.getPrice(),
                    event.getOrganizer().getUsername(),
                    event.getEventUrl(),
                    categoryDTOs  // Asignamos las categorías mapeadas
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(eventDTOs);
    }


    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam("available_tickets") int availableTickets,
            @RequestParam("localizacion") String localizacion,
            @RequestParam("price") int price,
            @RequestParam(value = "event_url", required = false) String eventUrl,  // Campo opcional
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "categories", required = false) List<Long> categoryIds,  // IDs de categorías
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
        } catch (Exception e) {
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
        event.setPrice(price);

        List<Category> categories = new ArrayList<>();
        if (categoryIds != null) {
            for (Long categoryId : categoryIds) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        
        event.setCategories(categories);  // Asignar las categorías al evento
        event.setEventUrl(eventUrl); 
        
        // Guardar la imagen si se recibe
        if (image != null && !image.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());

            // Crear la carpeta si no existe
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs(); // Crea la carpeta si no existe
            }

            // Definir la ruta de destino dentro de "static/images"
            Path targetLocation = Paths.get(uploadDir, originalFileName);

            // Si el archivo ya existe, agregar un sufijo numérico
            int fileCounter = 1;
            String fileName = originalFileName;
            while (Files.exists(targetLocation)) {
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                String baseFileName = fileName.substring(0, fileName.lastIndexOf("."));
                fileName = baseFileName + fileCounter + fileExtension;
                targetLocation = Paths.get(uploadDir, fileName);
                fileCounter++;
            }

            // Guardar la imagen
            try {
                Files.copy(image.getInputStream(), targetLocation);
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
    @PutMapping("/edit/{eventId}")
    public ResponseEntity<?> editEvent(
            @PathVariable Long eventId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam("available_tickets") int availableTickets,
            @RequestParam("localizacion") String localizacion,
            @RequestParam("price") int price,
            @RequestParam(value = "image", required = false) MultipartFile image, 
            @RequestParam(value = "categories", required = false) List<Long> categoryIds, // Recibir categorías como lista de IDs
            @RequestHeader("Authorization") String token) throws Exception {

        // Extraer el nombre de usuario del token JWT
        String username = jwtUtil.extractUsername(token.substring(7)); // Eliminar "Bearer "

        // Buscar el usuario con el nombre de usuario extraído del token
        User user = userRepository.findByUsername(username);

        // Verificar si el usuario existe
        if (user == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado.");
        }

        // Buscar el evento
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        // Verificar si el usuario es el organizador o administrador
        if (event.getOrganizer().getId() != user.getId() && user.getRole() != Role.ADMINISTRADOR) {
            return ResponseEntity.status(403).body("No tienes permisos para editar este evento.");
        }

        // Actualizar los datos del evento
        event.setTitle(title);
        event.setDescription(description);
        event.setAvailableTickets(availableTickets);
        event.setLocalizacion(localizacion);
        event.setPrice(price);

        // Actualizar la fecha del evento
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date eventDate = sdf.parse(date);
        event.setDate(eventDate);

        // Actualizar la imagen si se proporciona
        if (image != null && !image.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());

            // Eliminar la imagen anterior si existe
            File oldImage = new File(uploadDir + event.getImageUrl());
            if (oldImage.exists()) {
                oldImage.delete();
            }

            // Guardar la nueva imagen
            Path targetLocation = Paths.get(uploadDir, originalFileName);
            try {
                Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Error al guardar la imagen.");
            }

            event.setImageUrl(originalFileName);
        }

        // Si se recibieron categorías, actualizamos las categorías del evento
        if (categoryIds != null) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            event.setCategories(categories);
        }

        // Guardar los cambios en la base de datos
        eventRepository.save(event);

        return ResponseEntity.ok("Evento editado exitosamente.");
    }


    // Eliminar evento
    


    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId, @RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado.");
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        // Verificar si el usuario tiene permisos para eliminar
        if (user.getRole() == Role.ADMINISTRADOR || event.getOrganizer().equals(user)) {
            eventRepository.delete(event);
            return ResponseEntity.ok("Evento eliminado correctamente.");
        }

        return ResponseEntity.status(403).body("Acceso no autorizado.");
    }

    
    @GetMapping("/admin/panel")
    public ResponseEntity<?> getEventsForAdmin(@RequestHeader("Authorization") String token) {
        // Extraer el nombre de usuario del token JWT
        String username = jwtUtil.extractUsername(token.substring(7)); // Eliminar "Bearer "

        // Buscar el usuario con el nombre de usuario extraído del token
        User user = userRepository.findByUsername(username);

        // Verificar si el usuario existe
        if (user == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado.");
        }

        // Si el usuario es un administrador, se devuelven todos los eventos
        if (user.getRole() == Role.ADMINISTRADOR) {
            List<Event> events = eventRepository.findAll();
            List<EventDTO> eventDTOs = events.stream().map(event -> mapEventToDTO(event)).collect(Collectors.toList());
            return ResponseEntity.ok(eventDTOs);
        }

        // Si el usuario es un organizador, solo se devuelven los eventos creados por él
        if (user.getRole() == Role.ORGANIZADOR) {
            List<Event> events = eventRepository.findByOrganizer(user);
            List<EventDTO> eventDTOs = events.stream().map(event -> mapEventToDTO(event)).collect(Collectors.toList());
            return ResponseEntity.ok(eventDTOs);
        }

        return ResponseEntity.status(403).body("Acceso no autorizado.");
    }

    // Mapeo del evento a DTO
    private EventDTO mapEventToDTO(Event event) {
        List<CategoryDTO> categoryDTOs = event.getCategories().stream()
            .map(category -> new CategoryDTO(category.getId(), category.getName()))
            .collect(Collectors.toList());

        return new EventDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getSoldTickets(),
            event.getAvailableTickets(),
            event.getImageUrl(),
            event.getLocalizacion(),
            event.getPrice(),
            event.getOrganizer().getUsername(),
            event.getEventUrl(),
            categoryDTOs
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable Long eventId) {
        // Buscar el evento por su ID
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.status(404).body("Evento no encontrado.");
        }

        // Mapear el evento a DTO
        EventDTO eventDTO = mapEventToDTO(event);

        return ResponseEntity.ok(eventDTO);
    }
    
}

