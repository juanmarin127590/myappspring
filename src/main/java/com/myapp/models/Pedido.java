package com.myapp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"direcciones", "roles", "hibernateLazyInitializer"})
    private Usuario usuario;

    // Dirección de Envío - Se toma una "foto" de la dirección al momento del pedido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_direccion_envio", nullable = false)
    @JsonIgnoreProperties({"usuario", "hibernateLazyInitializer"})
    private Direccion direccionEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_direccion_facturacion", nullable = false)
    @JsonIgnoreProperties({"usuario", "hibernateLazyInitializer"})
    private Direccion direccionFacturacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_pedido", referencedColumnName = "id_estado_pedido", nullable = false)
    private EstadoPedido estadoPedido;

    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaPedido;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoEnvio = BigDecimal.ZERO; // Usamos BigDecimal.ZERO como default

    @Column(name = "impuestos", nullable = false, precision = 10, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "observacion_cliente", columnDefinition = "TEXT")
    private String observacionCliente;

    @Column(name = "estado_pago", nullable = false, length = 50)
    private String estadoPago; // Ej: 'Pendiente', 'Aprobado', 'Rechazado'

    @Column(name = "referencia_pago", length = 255)
    private String referenciaPago;

    // Relación Uno-a-Muchos con DetallesPedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();
    
    // Campo para agregar detalles a Pedido (ayuda al Service)
    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

}
