package com.myapp.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "DetallesPedido")
@Data
public class DetallePedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_pedido")
    private Long idDetallePedido;

    // Relación Many-to-One con Pedido (para la FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    @JsonIgnore // FIX: Romper el bucle de Pedido <-> DetallesPedido
    private Pedido pedido;

    // Relación Many-to-One con Producto (para la FK)
    // Mantenemos la referencia al producto, pero sólo para obtener info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonIgnoreProperties({"categoria", "hibernateLazyInitializer"}) // Solo cargamos lo esencial del producto
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    // PRECIO DE VENTA: Precio del producto al momento de la compra (evita problemas si el precio del producto cambia)
    @Column(name = "precio_unitario_congelado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal_linea", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // cantidad * precioUnitario

}
