package com.myapp.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Categorias")
@JsonIgnoreProperties({"hibernateLazyInitializer", "productos"})
public class Categoria {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(name = "nombre_categoria", nullable = false, unique = true, length = 100)
    private String nombreCategoria;

    @Column(name = "descripcion", nullable = true, length = 500)
    private String descripcion;

    // Relación Uno-a-Muchos con Productos (El 'mappedBy' indica el dueño de la relación)
    @JsonIgnore
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Producto> producto = new ArrayList<>();
    
    // Constructor sin argumentos (requerido por JPA)
    public Categoria() {}

    // Constructor con argumentos útil para la logica de negocio
    public Categoria(String nombreCategoria, String descripcion) {
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;     
    }

    // Getters y Setters
    public Long getIdCategoria() {
        return idCategoria; 
    }
    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria; 
    }
    public String getNombreCategoria() {
        return nombreCategoria; 
    }
    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
    public String getDescripcion() {
        return descripcion; 
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Producto> getProducto() {
        return producto;
    }

    public void setProducto(List<Producto> productos) {
        this.producto = productos;
    }

}
