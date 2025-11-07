package com.myapp.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.DTOs.PaymentRequest;
import com.myapp.models.Pago;
import com.myapp.services.PagoService;
import com.myapp.util.security.CustomUserDetails;


@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }
    
    // Método real para obtener el ID de usuario del contexto de seguridad JWT
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getIdUsuario();
        }
        throw new IllegalStateException("Usuario no autenticado o contexto de seguridad no válido.");
    }

    // POST: /api/pagos/process - Inicia la simulación del pago
    @PostMapping("/process")
    public ResponseEntity<Object> procesarPago(@RequestBody PaymentRequest request) {
        Long idUsuario = getAuthenticatedUserId();
        
        try {
            Pago pago = pagoService.procesarPago(idUsuario, request);
            
            if ("Aprobado".equals(pago.getEstado())) {
                return new ResponseEntity<>(pago, HttpStatus.OK); // 200 OK
            } else {
                // Si el pago es rechazado, se retorna 402 Payment Required con el detalle
                return new ResponseEntity<>(pago, HttpStatus.PAYMENT_REQUIRED); // 402
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Manejo de errores como pedido no encontrado, monto incorrecto o estado inválido
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
