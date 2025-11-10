package com.myapp.controllers;

import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.models.CarritoCompra;
import com.myapp.util.security.CustomUserDetails;
import com.myapp.services.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }
    
    // Placeholder para obtener el ID de usuario (debes usar tu lógica de Spring Security)
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Asegúrate de que el principal es una instancia de tu CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getIdUsuario();
    }

    // ----------------------------------------------------
    // ENDPOINTS DE USUARIO AUTENTICADO
    // ----------------------------------------------------
    
    // GET: /api/carrito - Obtener el carrito del usuario
    @GetMapping
    public ResponseEntity<CarritoCompra> obtenerMiCarrito() {
        Long idUsuario = getAuthenticatedUserId();
        // Siempre se devuelve el carrito, incluso si está vacío (se crea si no existe)
        CarritoCompra carrito = carritoService.obtenerOCrearCarrito(idUsuario);
        return carritoService.obtenerCarritoPorUsuario(idUsuario)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.ok(carrito)); // Fallback: retornar el carrito recién creado/vacío
    }

    // POST: /api/carrito - Agregar un ítem o actualizar cantidad
    @PostMapping
    public ResponseEntity<Object> agregarItemAlCarrito(@RequestBody Map<String, Object> request) {
        Long idUsuario = getAuthenticatedUserId();
        try {
            Long idProducto = Long.valueOf(request.get("idProducto").toString());
            Integer cantidad = (Integer) request.get("cantidad");
            
            CarritoCompra carritoActualizado = carritoService.agregarOActualizarItem(idUsuario, idProducto, cantidad);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
             return new ResponseEntity<>("Error al procesar la solicitud: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE: /api/carrito/{idItemCarrito} - Eliminar un ítem específico
    @DeleteMapping("/{idItemCarrito}")
    public ResponseEntity<Void> eliminarItemDelCarrito(@PathVariable Long idItemCarrito) {
        Long idUsuario = getAuthenticatedUserId();
        try {
            carritoService.eliminarItem(idUsuario, idItemCarrito);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // DELETE: /api/carrito/limpiar - Limpiar completamente el carrito
    @DeleteMapping("/limpiar")
    public ResponseEntity<Void> limpiarCarritoCompleto() {
        Long idUsuario = getAuthenticatedUserId();
        carritoService.limpiarCarrito(idUsuario);
        return ResponseEntity.noContent().build();
    }
}
