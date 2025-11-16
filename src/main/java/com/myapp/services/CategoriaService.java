package com.myapp.services;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.myapp.models.Categoria;
import com.myapp.repositories.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // CREATE (C) - Crear una nueva categoría
    public Categoria crearCategoria(Categoria categoria) {
        // Lógica de negocio: asegurar que el nombre de la categoría es único.
        Optional<Categoria> existingCategoria = categoriaRepository.findByNombreCategoria(categoria.getNombreCategoria());
        
        if (existingCategoria.isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + categoria.getNombreCategoria());
        }
        return categoriaRepository.save(categoria);
    }

    // READ ALL (R) - Obtener todas las categorías
    public List<Categoria> obtenerTodasCategorias() {
        return categoriaRepository.findAll();
    }

    // READ BY ID (R) - Obtener una categoría por ID
    public Optional<Categoria> obtenerCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    // UPDATE (U) - Actualizar una categoría existente
    public Categoria actualizarCategoria(Integer id, Categoria categoriaDetalles) {
        return categoriaRepository.findById(id)
            .map(categoria -> {
                if (!categoria.getNombreCategoria().equals(categoriaDetalles.getNombreCategoria())) {
                    // Verificar si el nuevo nombre ya existe
                    Optional<Categoria> existingCategoria = categoriaRepository.findByNombreCategoria(categoriaDetalles.getNombreCategoria());
                    if (existingCategoria.isPresent()) {
                        throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + categoriaDetalles.getNombreCategoria());
                    }
                }       

            categoria.setNombreCategoria(categoriaDetalles.getNombreCategoria());
            categoria.setDescripcion(categoriaDetalles.getDescripcion());
            return categoriaRepository.save(categoria);
            }).orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
    }

    // DELETE (D) - Eliminar una categoría
    public void eliminarCategoria(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoría no encontrada con ID: " + id);
        }
        
        categoriaRepository.deleteById(id);
    }
}