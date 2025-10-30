package com.myapp.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

// Anotación para indicar que esta clase es una entidad persistente
@Entity
// Anotación para mapear a la tabla 'Usuario' en la BD
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // NOTA: En un proyecto real, ¡DEBE ser hasheada!

    @Column(length = 20)
    private String telefono;

    // El 'updatable = false' asegura que Hibernate no intentará cambiar este valor en un UPDATE
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean activo = true;

    // Constructor sin argumentos (requerido por JPA)
    public Usuario() {
    }

    // Constructor con argumentos útil para la logica de negocio
    public Usuario(String nombre, String apellidos, String email, String password, String telefono) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
    }

    // Relación Many-to-Many con Roles
    @ManyToMany(fetch = FetchType.EAGER) // Carga EAGER para obtener roles al cargar el usuario (útil para seguridad)
    @JoinTable(
        name = "UsuarioRoles", // Nombre de la tabla intermedia en la BD
        joinColumns = @JoinColumn(name = "id_usuario"), // Columna de esta entidad (Usuario)
        inverseJoinColumns = @JoinColumn(name = "id_rol") // Columna de la entidad relacionada (Rol)
    )
    private Set<Rol> roles = new HashSet<>();

    // Getters y Setters
    public Long getIdUsuario() {return idUsuario; }
    public void setIdUsuario(Long idUsuario) {this.idUsuario = idUsuario;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getApellidos() {return apellidos;}
    public void setApellidos(String apellidos) {this.apellidos = apellidos;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}
    public LocalDateTime getFechaRegistro() {return fechaRegistro;}
    public Boolean getActivo() {return activo;}
    public void setActivo(Boolean activo) {this.activo = activo;}
    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }

}
