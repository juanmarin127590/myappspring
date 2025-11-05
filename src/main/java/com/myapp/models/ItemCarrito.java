package com.myapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "ItemsCarrito", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_carrito", "id_producto"}) // Restricción de unicidad del script
})
@Data
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_carrito")
    private Long idItemCarrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito", nullable = false)
    @JsonIgnore // Romper el bucle de serialización Carrito <-> ItemsCarrito
    private CarritoCompra idCarrito;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER para facilitar la serialización del producto
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonIgnoreProperties({"categoria", "stock", "activo", "descripcion", "hibernateLazyInitializer"}) // Solo datos públicos
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private LocalDateTime fechaAgregado = LocalDateTime.now();

    // Campo transitorio para el cálculo del subtotal del ítem
    @Transient 
    private BigDecimal subtotal;
}
