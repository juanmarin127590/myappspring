package com.DTOs;

import lombok.Data;
import java.math.BigDecimal;

import jakarta.annotation.Nonnull;


@Data
public class PaymentRequest {
    
    @Nonnull()
    private Long idPedido;
    
    @Nonnull()
    private Long idMetodoPago;
    
    @Nonnull()
    private BigDecimal monto;
    
    // Este campo simula la referencia o token que devuelve una pasarela real
    private String tokenPagoExterno; 
}