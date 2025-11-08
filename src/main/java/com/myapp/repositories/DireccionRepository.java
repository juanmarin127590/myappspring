package com.myapp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.Direccion;


@Repository
public interface DireccionRepository  extends JpaRepository<Direccion, Long>{
    // Buscar todas las direcciones de un usuario específico
    List<Direccion> findByUsuario_IdUsuario(Long idUsuario);

    // Buscar la dirección principal de un usuario
    Optional<Direccion> findByUsuario_IdUsuarioAndPrincipalEnvioTrue(Long idUsuario);
    
    // Buscar una dirección específica por ID y por usuario (para asegurar propiedad)
    Optional<Direccion> findByIdDireccionAndUsuario_IdUsuario(Long idDireccion, Long idUsuario);
}
