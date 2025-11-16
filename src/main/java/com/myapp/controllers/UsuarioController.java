package com.myapp.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.models.Usuario;
import com.myapp.services.UsuarioServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServices usuarioService;

    // POST: /api/usuarios
    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.registrarNuevoCliente(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    // GET: /api/usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        List<Usuario> listaUsuarios = usuarioService.obtenerTodosLosUsuarios();
        return new ResponseEntity<>(listaUsuarios, HttpStatus.OK); // Devuelve 200 OK
    }

    // GET: /api/usuarios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    // PUT: /api/usuarios/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Unificado con SecurityConfig
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody Usuario detalleUsuario) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, detalleUsuario);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK); // Devuelve 200 OK
    }

    // DELETE: /api/usuarios/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Unificado con SecurityConfig
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Devuelve 204 No Content
    }

}
