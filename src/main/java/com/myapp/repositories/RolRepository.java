package com.myapp.repositories;

import java.util.Optional;
import com.myapp.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Método para buscar un Rol por su nombre, esencial para el seeding de datos y lógica
    Optional<Rol> findByNombreRol(String nombreRol);
}