package com.myapp.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.myapp.models.Usuario;
import com.myapp.services.UsuarioServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("/api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioServices usuarioServices;

    // POST: /api/usuario
    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
            // El método save es transaccional por defecto en Spring Data JPA
            Usuario nuevoUsuario = usuarioServices.crearUsuario(usuario);

            return new ResponseEntity<> (nuevoUsuario, HttpStatus.CREATED);
        
    }

    // GET: /api/usuario
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
            List<Usuario> listaUsuarios = usuarioServices.obtenerTodosLosUsuarios();
            return new ResponseEntity<> (listaUsuarios, HttpStatus.OK); // Devuelve 200 OK
    }

    // GET: /api/usuario/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
            return usuarioServices.obtenerUsuarioPorId(id)
                    .map(usuario -> new ResponseEntity<>(usuario, HttpStatus.OK)) // Si existe, devuelve 200 OK
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));  // Si no existe, devuelve 404 Not Found  
    }

    // PUT: /api/usuario/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario detalleUsuario) {
            try {
                Usuario usuarioActualizado = usuarioServices.actualizarUsuario(id, detalleUsuario);
                return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK); // Devuelve 200 OK
            } catch (RuntimeException e) {
                // Manejo básico de excepción (404 Not Found)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Devuelve 404 Not Found si no existe
            }
    }

    // DELETE: /api/usuario/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
            try {
                usuarioServices.eliminarUsuario(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Devuelve 204 No Content
            } catch (RuntimeException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Devuelve 404 Not Found si no existe
            }
        }

}
