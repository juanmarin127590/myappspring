package com.myapp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Método personalizado para buscar por el SKU único del producto.
    Optional<Producto> findBySku(String sku);
    
    // Método para obtener solo productos activos (para el catálogo público)
    List<Producto> findAllByActivoTrue();
    
    // Método para buscar productos dentro de una categoría específica
    List<Producto> findByCategoriaIdCategoria(Long idCategoria);
}
