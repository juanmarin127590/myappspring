package com.myapp.repositories;

import java.util.Locale.Category;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Category, Long> {
    // Método personalizado para buscar por el nombre único de la categoría.
    Optional<Categoria> findByNombreCategoria(String nombre);
}
