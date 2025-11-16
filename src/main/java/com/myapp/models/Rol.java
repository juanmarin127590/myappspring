package com.myapp.models;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    // El nombre del rol se usa en Spring Security (ej. ROLE_ADMIN, ROLE_CLIENTE)
    private String nombreRol;

    // Relación ManyToMany con Usuario:
    // MappedBy indica que la tabla Usuario es dueña de la relación
   @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    // Usamos Set para evitar duplicados y mejorar el rendimiento de búsqueda
    private Set<Usuario> usuarios = new HashSet<>();

    public Rol() {
    }

    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    // --- Getters y Setters ---
    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

}
