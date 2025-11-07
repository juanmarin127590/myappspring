package com.myapp.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServices usuarioService;

    // POST: /api/usuarios
    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarNuevoCliente(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Manejo de error de negocio (ej. Email ya existe)
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

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
        return usuarioService.obtenerUsuarioPorId(id)
                .map(usuario -> new ResponseEntity<>(usuario, HttpStatus.OK)) // Si existe, devuelve 200 OK
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Si no existe, devuelve 404 Not Found
    }

    // PUT: /api/usuarios/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario detalleUsuario) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, detalleUsuario);
            return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK); // Devuelve 200 OK
        } catch (RuntimeException e) {
            // Manejo básico de excepción (404 Not Found)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Devuelve 404 Not Found si no existe
        }
    }

    // DELETE: /api/usuarios/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Devuelve 204 No Content
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Devuelve 404 Not Found si no existe
        }
    }

}
