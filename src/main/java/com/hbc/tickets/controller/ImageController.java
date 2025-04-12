package com.hbc.tickets.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;

@RestController
public class ImageController {

    @Value("${upload.dir}")
    private String uploadDir; // Ruta donde se guardan las imágenes

    @GetMapping("/uploaded-images/{imageName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String imageName) {
        try {
            // Cargar la imagen desde la carpeta "static/images"
            Resource resource = new FileSystemResource(Paths.get(uploadDir, imageName).toFile());
            if (resource.exists() || resource.isReadable()) {
                String contentType = "image/jpeg"; // Ajusta según el tipo de archivo

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, contentType);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + imageName);

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
