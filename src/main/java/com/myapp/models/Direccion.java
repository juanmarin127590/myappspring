package com.myapp.models;

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
@Table(name = "direcciones")
@Data
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long idDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    // Importante: ignorar el campo 'direcciones' del Usuario para evitar bucles de
    // serialización
    @JsonIgnoreProperties({ "direcciones", "hibernateLazyInitializer", "roles" })
    private Usuario usuario;

    @Column(name = "nombre_destinatario", nullable = false, length = 100)
    private String nombreDestinatario;

    @Column(name = "calle_principal", nullable = false, length = 255)
    private String callePrincipal;

    @Column(name = "numero_exterior", length = 50)
    private String numeroExterior;

    @Column(name = "informacion_adicional", length = 255)
    private String informacionAdicional;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "estado_provincia", nullable = false, length = 100)
    private String estado; // Provincia / Departamento

    @Column(name = "codigo_postal", nullable = false, length = 20)
    private String codigoPostal;

    @Column(nullable = false, length = 100)
    private String pais;

    @Column(name = "es_predeterminada_envio" , nullable = false)
    private Boolean principalEnvio = false; // Indica si es la dirección predeterminada de envio

    @Column(name = "es_predeterminada_facturacion" , nullable = false)
    private Boolean principalFactura = false; // Indica si es la dirección predeterminada de envio

}
