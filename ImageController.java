package com.myapp.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Controlador para manejar la carga y descarga de imágenes de productos
 * 
 * Endpoints:
 * - POST /api/upload/product-image: Sube una imagen y retorna la URL
 * - GET /api/images/products/{filename}: Sirve la imagen
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permitir acceso desde Flutter
public class ImageController {

    // Directorio donde se guardarán las imágenes
    // Configurable desde application.properties
    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    // URL base del servidor (configurable)
    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;

    /**
     * Endpoint para subir imágenes de productos
     * 
     * @param file Archivo de imagen
     * @return URL completa de la imagen subida
     */
    @PostMapping("/upload/product-image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                return ResponseEntity.badRequest()
                    .body("Tipo de archivo no válido. Solo se permiten: JPG, PNG, WebP");
            }

            // Validar tamaño (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body("El archivo es demasiado grande. Máximo 5MB");
            }

            // Crear directorio si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para evitar colisiones
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Construir URL completa
            String imageUrl = serverUrl + "/api/images/products/" + uniqueFilename;

            // Retornar respuesta con la URL
            return ResponseEntity.ok().body(new ImageUploadResponse(imageUrl, uniqueFilename));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error al guardar la imagen: " + e.getMessage());
        }
    }

    /**
     * Endpoint para servir imágenes
     * 
     * @param filename Nombre del archivo
     * @return Imagen como recurso
     */
    @GetMapping("/images/products/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determinar el tipo de contenido
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Valida si el tipo de archivo es una imagen válida
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/webp") ||
            contentType.equals("image/jpg")
        );
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * DTO para la respuesta de carga de imagen
     */
    public static class ImageUploadResponse {
        private String url;
        private String filename;

        public ImageUploadResponse(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }
}
