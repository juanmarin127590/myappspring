package com.myapp.util.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.myapp.models.Usuario;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {
    
    private final Long idUsuario;
    private final String nombre;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean activo;

    public CustomUserDetails(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.password = usuario.getPassword();
        this.activo = usuario.getActivo();
        // Mapear los roles del usuario a GrantedAuthority de Spring Security
        this.authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombreRol().trim().toUpperCase()))
                .collect(Collectors.toList());
    }

    // ... Implementación de los métodos de UserDetails ...

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() { // Spring Security usa getUsername para el campo de login (en este caso, el email)
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo; // Usamos el campo 'activo' del modelo Usuario
    }
    
}
