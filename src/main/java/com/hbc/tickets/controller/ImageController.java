package com.hbc.tickets.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    @GetMapping("/uploaded-images/{imageName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String imageName) {
        try {
            // Cargar la imagen desde la carpeta static/images
            Resource resource = new ClassPathResource("static/images/" + imageName);
            if (resource.exists() || resource.isReadable()) {
                // Determina el tipo de contenido de la imagen
                String contentType = "image/jpeg"; // Ajusta esto según el tipo de archivo (JPG, PNG, etc.)

                // Configurar los encabezados de la respuesta
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, contentType);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + imageName);
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");  // CORS habilitado

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
