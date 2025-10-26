package com.myapp.services;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.myapp.models.Usuario;
import com.myapp.repositories.UsuarioRepository;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Buscar Usuario por email (username en este contexto)
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !usuario.getActivo()) {
            // Lanza una excepción si el usuario no existe o está inactivo
            throw new UsernameNotFoundException("Usuario no encontrado o inactivo con email: " + email);
        }

        // 2. Mapear Roles a GrantedAuthorities de Spring Security
        Collection<? extends GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol())) 
                .collect(Collectors.toList());

        // 3. Crear y retornar el objeto UserDetails
        return new User(
            usuario.getEmail(),           // Username (email)
            usuario.getPassword(),        // Contraseña (¡debe estar hasheada!)
            usuario.getActivo(),          // Cuenta activa
            true, true, true,             // Cuenta no expirada, credenciales no expiradas, cuenta no bloqueada
            authorities                   // Roles/Autoridades
        );
    }
}
