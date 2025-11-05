package com.myapp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.ItemCarrito;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    // Buscar un ítem por ID de Carrito y ID de Producto (para actualizaciones/existencia)
    Optional<ItemCarrito> findByCarrito_IdCarritoAndProducto_IdProducto(Long idCarrito, Long idProducto);
    
    // Eliminar un ítem del carrito (útil para la interfaz)
    void deleteByCarrito_IdCarritoAndIdItemCarrito(Long idCarrito, Long idItemCarrito);
}