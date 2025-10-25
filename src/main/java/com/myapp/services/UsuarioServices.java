package com.myapp.services;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.myapp.models.Usuario;
import com.myapp.repositories.UsuarioRepository;

@Service
public class UsuarioServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Crear un nuevo usuario
    public Usuario crearUsuario(Usuario usuario) {
        // Lógica de negocio: Por ejemplo, encriptar la contraseña (requeriría un
        // BCryptPasswordEncoder)
        // if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
        // throw new RuntimeException("El email ya está registrado.");
        // }
        // usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    // 2. Leer todos los usuarios
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findByActivoTrue();
    }

    // 3. Leer un usuario por ID
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }


    // 4. Actualizar un usuario existente
    public Usuario actualizarUsuario(Long id, Usuario detalleUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();
            usuarioExistente.setNombre(detalleUsuario.getNombre());
            usuarioExistente.setApellidos(detalleUsuario.getApellidos());
            usuarioExistente.setTelefono(detalleUsuario.getTelefono());
            // No actualizar la contraseña aquí por simplicidad
            return usuarioRepository.save(usuarioExistente);
        } else {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
    }

    // 5. Eliminar un usuario (lógica de negocio: desactivar en lugar de borrar)
    public void eliminarUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();
            usuarioExistente.setActivo(false);
            usuarioRepository.save(usuarioExistente);
        } else {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);      
        }
    }

}
