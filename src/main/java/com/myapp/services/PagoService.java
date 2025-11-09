package com.myapp.services;

import java.math.RoundingMode;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DTOs.PaymentRequest;
import com.myapp.models.EstadoPedido;
import com.myapp.models.MetodoPago;
import com.myapp.models.Pago;
import com.myapp.models.Pedido;
import com.myapp.repositories.EstadoPedidoRepository;
import com.myapp.repositories.MetodoPagoRepository;
import com.myapp.repositories.PagoRepository;
import com.myapp.repositories.PedidoRepository;

@Service
public class PagoService {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;

    public PagoService(PedidoRepository pedidoRepository, PagoRepository pagoRepository, 
                       MetodoPagoRepository metodoPagoRepository, EstadoPedidoRepository estadoPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.estadoPedidoRepository = estadoPedidoRepository;
    }

    /**
     * Procesa el pago simulado para un pedido.
     */
    @Transactional
    public Pago procesarPago(Long idUsuario, PaymentRequest request) {
        
        // --- 1. Validar el Pedido ---
        Pedido pedido = pedidoRepository.findByIdPedidoAndUsuario_IdUsuario(
            request.getIdPedido(), idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado o no pertenece al usuario."));
            
        // Validar que el pedido esté en estado "Pendiente Pago" (Asumimos ID 1)
        if (!pedido.getIdEstado().getIdEstado().equals(1L)) { // ID 1 para 'Pendiente Pago'
            throw new IllegalStateException("El pedido no está en estado de 'Pendiente Pago'. Estado actual: " + pedido.getIdEstado().getNombreEstado());
        }
        
        // Validar que el monto del pago coincida con el monto total del pedido
        if (pedido.getMontoTotal().compareTo(request.getMonto().setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("El monto del pago (" + request.getMonto() + ") no coincide con el monto total del pedido (" + pedido.getMontoTotal() + ").");
        }

        // --- 2. Validar Método de Pago ---
        MetodoPago metodoPago = metodoPagoRepository.findById(request.getIdMetodoPago())
            .orElseThrow(() -> new IllegalArgumentException("Método de pago no válido."));
            
        // --- 3. Simulación de la Transacción Externa ---
        boolean pagoExitoso = simularTransaccion(); // Lógica de simulación

        // --- 4. Determinar Estado y Referencia ---
        String estadoPago;
        String referencia = "REF-" + System.currentTimeMillis();
        Long idNuevoEstadoPedido;
        
        if (pagoExitoso) {
            estadoPago = "Aprobado";
            idNuevoEstadoPedido = 2L; // ID 2 = Pagado
        } else {
            estadoPago = "Rechazado";
            idNuevoEstadoPedido = 1L; // ID 1 = Pendiente Pago (mantiene el estado)
            referencia = "FALLO-" + System.currentTimeMillis();
        }
        
        // --- 5. Crear el Registro de Pago ---
        Pago nuevoPago = new Pago();
        nuevoPago.setPedido(pedido);
        nuevoPago.setIdMetodoPago(metodoPago);
        nuevoPago.setMonto(request.getMonto().setScale(2, RoundingMode.HALF_UP));
        nuevoPago.setEstado(estadoPago);
        nuevoPago.setReferenciaTransaccion(referencia);
        
        Pago pagoGuardado = pagoRepository.save(nuevoPago);

        // --- 6. Actualizar el Pedido ---
        EstadoPedido nuevoEstado = estadoPedidoRepository.findById(idNuevoEstadoPedido)
            .orElseThrow(() -> new IllegalStateException("Estado de pedido ID " + idNuevoEstadoPedido + " no encontrado."));
            
        pedido.setIdEstado(nuevoEstado);
        pedido.setEstadoPago(estadoPago); // Actualiza el campo de estado de pago en el pedido
        pedido.setReferenciaPago(referencia); // Guarda la referencia de la transacción
        
        pedidoRepository.save(pedido);

        return pagoGuardado;
    }
    
    /**
     * Simula la llamada a una pasarela de pago externa. 
     * Retorna true (éxito) o false (fallo).
     */
    private boolean simularTransaccion() {
        // Simulación: 80% de probabilidad de éxito
        return new Random().nextInt(100) < 80;
    }
}
