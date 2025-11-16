package com.myapp.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.models.Direccion;
import com.myapp.util.security.CustomUserDetails;
import com.myapp.services.DireccionService;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService){
        this.direccionService = direccionService;
    }

    // Usaremos un placeholder Long idUsuario = 1L; para simular la obtención del ID si no tienes el UserDetails configurado, 
    // pero la estructura del método debe ser:

    private Long getAuthenticatedUserId() {
        // *** ESTE ES UN PLACEHOLDER. IMPLEMENTAR LA OBTENCIÓN DEL ID REAL DEL USUARIO AUTENTICADO. ***
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Asegúrate de que el principal es una instancia de tu CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getIdUsuario();
    }

    // ----------------------------------------------------
    // ENDPOINTS DE USUARIO (Acceso al propio recurso)
    // ----------------------------------------------------
    
    // GET: /api/direcciones - Obtener todas las direcciones del usuario autenticado
    @GetMapping
    public ResponseEntity<List<Direccion>> obtenerMisDirecciones() {
        Long idUsuario = getAuthenticatedUserId(); 
        List<Direccion> direcciones = direccionService.obtenerDireccionesPorUsuario(idUsuario);
        return ResponseEntity.ok(direcciones);
    }

    // GET: /api/direcciones/{idDireccion} - Obtener una dirección específica del usuario
    @GetMapping("/{idDireccion}")
    public ResponseEntity<Direccion> obtenerMiDireccionPorId(@PathVariable Long idDireccion) {
        Long idUsuario = getAuthenticatedUserId(); 
        return direccionService.obtenerDireccionPorIdYUsuario(idDireccion, idUsuario)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // 404 si no existe O no le pertenece
    }

    // POST: /api/direcciones - Crear una nueva dirección para el usuario
    @PostMapping
    public ResponseEntity<Direccion> crearDireccion(@RequestBody Direccion direccion) {
        Long idUsuario = getAuthenticatedUserId(); 
        Direccion nuevaDireccion = direccionService.crearDireccion(idUsuario, direccion);
        return new ResponseEntity<>(nuevaDireccion, HttpStatus.CREATED);
    }

    // PUT: /api/direcciones/{idDireccion} - Actualizar una dirección del usuario
    @PutMapping("/{idDireccion}")
    public ResponseEntity<Direccion> actualizarDireccion(@PathVariable Long idDireccion, @RequestBody Direccion direccionDetalles) {
        Long idUsuario = getAuthenticatedUserId(); 
        Direccion direccionActualizada = direccionService.actualizarDireccion(idDireccion, idUsuario, direccionDetalles);
        return ResponseEntity.ok(direccionActualizada);
    }

    // DELETE: /api/direcciones/{idDireccion} - Eliminar una dirección del usuario
    @DeleteMapping("/{idDireccion}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Long idDireccion) {
        Long idUsuario = getAuthenticatedUserId(); 
        direccionService.eliminarDireccion(idDireccion, idUsuario);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------
    // ENDPOINTS DE ADMINISTRADOR (Acceso global)
    // ----------------------------------------------------

    // GET: /api/direcciones/admin/all - Listar todas las direcciones (solo ADMIN)
    @GetMapping("/admin/all")
    public ResponseEntity<List<Direccion>> obtenerTodasDireccionesAdmin() {
        // Este método será protegido por ROLE_ADMINISTRADOR en SecurityConfig
        return ResponseEntity.ok(direccionService.obtenerTodasDirecciones());
    }

    // GET: /api/direcciones/admin/{idDireccion} - Obtener cualquier dirección por ID (solo ADMIN)
    @GetMapping("/admin/{idDireccion}")
    public ResponseEntity<Direccion> obtenerDireccionPorIdAdmin(@PathVariable Long idDireccion) {
        // Este método será protegido por ROLE_ADMINISTRADOR en SecurityConfig
        return direccionService.obtenerDireccionPorId(idDireccion)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
}
