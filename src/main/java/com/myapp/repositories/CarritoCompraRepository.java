package com.myapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.CarritoCompra;

import java.util.Optional;

@Repository
public interface CarritoCompraRepository extends JpaRepository<CarritoCompra, Long> {

    // Obtener el carrito de un usuario específico (relación 1:1)
    Optional<CarritoCompra> findByUsuario_IdUsuario(Long idUsuario);
}

