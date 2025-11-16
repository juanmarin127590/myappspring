package com.myapp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.models.Categoria;
import com.myapp.services.CategoriaService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // ----------------------------------------------------
    // ENDPOINTS PÚBLICOS (Catálogo)
    // ----------------------------------------------------

    // GET: /api/categorias (Público - para el catálogo)
    @GetMapping
    public ResponseEntity<List<Categoria>> obtenerTodasCategorias() {
        return ResponseEntity.ok(categoriaService.obtenerTodasCategorias());
    }

    // GET: /api/categorias/{id} (Público - para el detalle)
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Long id) {
        return categoriaService.obtenerCategoriaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ----------------------------------------------------
    // ENDPOINTS ADMINISTRATIVOS (Requieren ROLE_ADMINISTRADOR) CRUD
    // ----------------------------------------------------

    // Los endpoints de creación, actualización y eliminación se implementarían
    // aquí.

    // POST: /api/categorias ADMIN - Crear
    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaService.crearCategoria(categoria);
        return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
    }

    // PUT: /api/categorias/{id} (ADMIN) - Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizqrCategoria(@PathVariable Long id, @RequestBody Categoria categoriaDetalle){
        Categoria categoriaActualizada = categoriaService.actualizarCategoria(id, categoriaDetalle);
        return ResponseEntity.ok(categoriaActualizada);
    }

    // DELETE: /api/categorias/{id} (ADMIN) - Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

}
