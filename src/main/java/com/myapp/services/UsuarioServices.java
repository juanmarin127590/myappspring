package com.myapp.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myapp.models.Rol;
import com.myapp.models.Usuario;
import com.myapp.repositories.RolRepository;
import com.myapp.repositories.UsuarioRepository;

@Service
public class UsuarioServices {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;


    public UsuarioServices(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Crear un nuevo usuario
    public Usuario registrarNuevoCliente(Usuario usuario) {
        // 1. Verificación del email (Lógica de Negocio)
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado: " + usuario.getEmail());
        }
        
        //2. Encriptar la contraseña antes de guardar
        String hashedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(hashedPassword);

        // 3. Asignar rol por defecto (Lógica de Negocio)
        Rol clienteRol = rolRepository.findByNombreRol("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol 'CLIENTE' no encontrado. Ejecute el DataSeeder."));

        // Usar Collections.singleton para crear un Set inmutable con un solo elemento
        usuario.setRoles(Collections.singleton(clienteRol));    
        
        // 4. Establecer valores por defecto (si no vienen en el request)
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        // 5. Persistencia
        return usuarioRepository.save(usuario);
    }

   // Método para crear usuario con roles específicos (Para uso administrativo)
    @Transactional
    public Usuario crearUsuarioAdmin(Usuario usuario, Set<String> nombresRoles) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())){
            throw new IllegalArgumentException("El email ya se encuentra registrado: " + usuario.getEmail());
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        Set<Rol> roles = nombresRoles.stream()
                .map(nombre -> rolRepository.findByNombreRol(nombre)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombre)))
                .collect(Collectors.toSet());
        
        usuario.setRoles(roles);
        usuario.setActivo(true);
        
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
