package com.myapp.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "EstadosPedido")
@Data
public class EstadoPedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pedido")
    private Integer idEstado;

    @Column(name = "nombre_estado", nullable = false, length = 50, unique = true)
    private String nombreEstado;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

}
