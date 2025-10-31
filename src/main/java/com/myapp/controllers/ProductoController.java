package com.myapp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.models.Producto;
import com.myapp.services.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ----------------------------------------------------
    // ENDPOINTS PÚBLICOS (Catálogo)
    // ----------------------------------------------------
    
    // GET: /api/productos (Público - catálogo activo)
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerCatalogoActivo() {
        return ResponseEntity.ok(productoService.obtenerCatalogoActivo());
    }

    // GET: /api/productos/{id} (Público - detalle de producto)
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        return productoService.obtenerProductoPorId(id)
            .filter(Producto::getActivo) // Solo mostrar si está activo en el catálogo público
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ----------------------------------------------------
    // ENDPOINTS ADMINISTRATIVOS (Requieren ROLE_ADMINISTRADOR)
    // ----------------------------------------------------

    // GET: /api/productos/admin (ADMIN) - Listar todos, incluyendo inactivos
    @GetMapping("/admin")
    public ResponseEntity<List<Producto>> obtenerTodosLosProductosAdmin() {
        // La autorización a este endpoint debe ser configurada en SecurityConfig
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    // POST: /api/productos (ADMIN) - Crear
    @PostMapping
    public ResponseEntity<Object> crearProducto(@RequestBody Producto producto) {
        try {
            Producto nuevoProducto = productoService.crearProducto(producto);
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // PUT: /api/productos/{id} (ADMIN) - Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoDetalles) {
        try {
            Producto productoActualizado = productoService.actualizarProducto(id, productoDetalles);
            return ResponseEntity.ok(productoActualizado);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // DELETE: /api/productos/{id} (ADMIN) - Soft Delete (Desactivar)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build(); // 204 indica éxito sin contenido
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
