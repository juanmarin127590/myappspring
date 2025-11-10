package com.myapp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.models.Pedido;
import com.myapp.util.security.CustomUserDetails;
import com.myapp.services.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;


    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Placeholder para obtener el ID de usuario (debes usar tu lógica de Spring Security)
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Asegúrate de que el principal es una instancia de tu CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getIdUsuario();
    }

    // ----------------------------------------------------
    // ENDPOINTS DE USUARIO AUTENTICADO
    // ----------------------------------------------------
    
    // POST: /api/pedidos - CREAR un nuevo pedido
   @PostMapping
    public ResponseEntity<Object> crearPedido(@RequestBody Pedido pedidoRequest) {
        Long idUsuario = getAuthenticatedUserId();
        try {
            Pedido nuevoPedido = pedidoService.crearPedido(idUsuario, pedidoRequest);
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // GET: /api/pedidos - Obtener historial de pedidos del usuario
    @GetMapping
    public ResponseEntity<List<Pedido>> obtenerMisPedidos() {
        Long idUsuario = getAuthenticatedUserId();
        List<Pedido> pedidos = pedidoService.obtenerMisPedidos(idUsuario);
        return ResponseEntity.ok(pedidos);
    }
    
    // GET: /api/pedidos/{idPedido} - Obtener detalle de un pedido (verificando propiedad)
    @GetMapping("/{idPedido}")
    public ResponseEntity<Pedido> obtenerDetallePedido(@PathVariable Long idPedido) {
        Long idUsuario = getAuthenticatedUserId();
        return pedidoService.obtenerPedidoPorIdYUsuario(idPedido, idUsuario)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT: /api/pedidos/{idPedido}/cancelar - Cancelar un pedido
    @PutMapping("/{idPedido}/cancelar")
    public ResponseEntity<Object> cancelarPedido(@PathVariable Long idPedido) {
        Long idUsuario = getAuthenticatedUserId();
        try {
            Pedido pedidoCancelado = pedidoService.cancelarPedido(idPedido, idUsuario);
            return ResponseEntity.ok(pedidoCancelado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    // ----------------------------------------------------
    // ENDPOINTS DE ADMINISTRADOR
    // ----------------------------------------------------

    // GET: /api/pedidos/admin/all - Obtener todos los pedidos (ADMIN)
    @GetMapping("/admin/all")
    public ResponseEntity<List<Pedido>> obtenerTodosLosPedidosAdmin() {
        return ResponseEntity.ok(pedidoService.obtenerTodosLosPedidos());
    }
    
    // GET: /api/pedidos/admin/{idPedido} - Obtener cualquier pedido por ID (ADMIN)
    @GetMapping("/admin/{idPedido}")
    public ResponseEntity<Pedido> obtenerPedidoPorIdAdmin(@PathVariable Long idPedido) {
        return pedidoService.obtenerPedidoPorId(idPedido)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // PUT: /api/pedidos/admin/{idPedido}/estado - Actualizar el estado del pedido (ADMIN)
    @PutMapping("/admin/{idPedido}/estado")
    public ResponseEntity<Object> actualizarEstadoPedidoAdmin(@PathVariable Long idPedido, @RequestParam Integer idEstado) {
        try {
            Pedido pedidoActualizado = pedidoService.actualizarEstado(idPedido, idEstado);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
}
