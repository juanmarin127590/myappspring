package com.myapp.repositories;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myapp.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Podemos definir métodos personalizados que Spring Data JPA implementará
    
    // automáticamente
    Usuario findByEmail(String email); // Buscar un usuario por su email

    // Buscar todos los usuarios activos
    List<Usuario> findByActivoTrue();

}
