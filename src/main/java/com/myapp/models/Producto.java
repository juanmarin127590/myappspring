package com.myapp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "Productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku; // Stock Keeping Unit, código único para cada producto

    @Column(name = "nombre_producto", nullable = false, length = 255)
    private String nombreProducto;

    @Column(name = "descripcion_larga", columnDefinition = "TEXT")
    private String descripcionLarga;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "cantidad_stock", nullable = false)
    private Integer cantidadStock;

    @Column(name = "imagen_principal_url", columnDefinition = "TEXT")
    private String imagenUrl;

    @Column(name = "peso_kg", precision = 6, scale = 3)
    private BigDecimal pesoKg;

    @Column(name = "dimensiones_cm", length = 100)
    private String dimensionesCm; // Formato: "Largo x Ancho x Alto"

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", nullable = true)
    private LocalDateTime fechaActualizacion;

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Relación ManyToOne con Categoria (Clave foránea)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    // FIX de serialización: Ignoramos el lado de la lista de productos de
    // Categoria.
    @JsonIgnoreProperties({ "productos", "hibernateLazyInitializer" })
    private Categoria categoria;

    // Constructor sin argumentos (requerido por JPA)
    public Producto() {
    }

    // Constructor con argumentos útil para la logica de negocio
    public Producto(String sku, String nombreProducto, String descripcionLarga, BigDecimal precio,
            Integer cantidadStock,
            String imagenUrl, BigDecimal pesoKg, String dimensionesCm, Boolean activo, Categoria categoria) {
        this.sku = sku;
        this.nombreProducto = nombreProducto;
        this.descripcionLarga = descripcionLarga;
        this.precio = precio;
        this.cantidadStock = cantidadStock;
        this.imagenUrl = imagenUrl;
        this.pesoKg = pesoKg;
        this.dimensionesCm = dimensionesCm;
        this.activo = activo;
        this.categoria = categoria;
    }

    // Getters y Setters
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcionLarga() {
        return descripcionLarga;
    }

    public void setDescripcionLarga(String descripcionLarga) {
        this.descripcionLarga = descripcionLarga;
    }

    public BigDecimal getPrecio() {
        return precio;
    }       
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    public Integer getCantidadStock() {
        return cantidadStock;
    }
    public void setCantidadStock(Integer cantidadStock) {
        this.cantidadStock = cantidadStock;
    }
    public String getImagenUrl() {
        return imagenUrl;
    }   
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    public BigDecimal getPesoKg() {
        return pesoKg;  
    }
    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }
    public String getDimensionesCm() {
        return dimensionesCm;
    }       
    public void setDimensionesCm(String dimensionesCm) {
        this.dimensionesCm = dimensionesCm;
    }
    public Boolean getActivo() {
        return activo;
    }   
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;       
    }   
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    public Categoria getCategoria() {
        return categoria;   
    }       
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }   
    
}