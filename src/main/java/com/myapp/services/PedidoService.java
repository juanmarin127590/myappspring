package com.myapp.services;


import org.springframework.transaction.annotation.Transactional;
import com.myapp.models.DetallePedido;
import com.myapp.models.Direccion;
import com.myapp.models.EstadoPedido;
import com.myapp.models.MetodoPago;
import com.myapp.models.Pedido;
import com.myapp.models.Producto;
import com.myapp.models.Usuario;
import com.myapp.repositories.DireccionRepository;
import com.myapp.repositories.EstadoPedidoRepository;
import com.myapp.repositories.MetodoPagoRepository;
import com.myapp.repositories.PedidoRepository;
import com.myapp.repositories.ProductoRepository;
import com.myapp.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final DireccionRepository direccionRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository, DireccionRepository direccionRepository, MetodoPagoRepository metodoPagoRepository, EstadoPedidoRepository estadoPedidoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.direccionRepository = direccionRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.estadoPedidoRepository = estadoPedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Proceso de creación de un nuevo pedido. 
     * Incluye validaciones críticas y lógica transaccional.
     */
    @Transactional
    public Pedido crearPedido(Long idUsuario, Pedido pedidoRequest) {
        // --- 1. Validar Entidades y Obtener Datos de Referencia ---
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Se usa la dirección proporcionada por el usuario, asegurando que le pertenezca
        Direccion direccionEnvio = direccionRepository.findByIdDireccionAndUsuario_IdUsuario(
            pedidoRequest.getDireccionEnvio().getIdDireccion(), idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Dirección de envío no válida o no pertenece al usuario."));
        
        MetodoPago metodoPago = metodoPagoRepository.findByIdMetodoPago(pedidoRequest.getIdMetodoPago())
            .orElseThrow(() -> new IllegalArgumentException("Método de pago no válido."));

        // Estado inicial del pedido (ej. ID 0 = "Pendiente")
        EstadoPedido estadoInicial = estadoPedidoRepository.findById((long) 0) 
            .orElseThrow(() -> new IllegalStateException("Estado inicial del pedido (ID 1) no encontrado."));

        // --- 2. Inicializar Pedido ---
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuario(usuario);
        nuevoPedido.setDireccionEnvio(direccionEnvio);
        nuevoPedido.setIdMetodoPago(metodoPago);
        nuevoPedido.setIdEstado(estadoInicial);
        nuevoPedido.setEstadoPago("Pendiente");
        nuevoPedido.setMontoEnvio(new BigDecimal("6000.00")); // Ejemplo de costo de envío fijo

        BigDecimal subtotalGeneral = BigDecimal.ZERO;
        
        // --- 3. Procesar Detalles del Pedido, Validar Stock y Calcular Totales ---
        if (pedidoRequest.getDetalles() == null || pedidoRequest.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto.");
        }

        for (DetallePedido detalleRequest : pedidoRequest.getDetalles()) {
            Producto producto = productoRepository.findById(detalleRequest.getProducto().getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + detalleRequest.getProducto().getIdProducto()));
            
            Integer cantidad = detalleRequest.getCantidad();
            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser positiva.");
            }
            
            // Validación de Stock
            if (producto.getCantidadStock() < cantidad) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombreProducto());
            }

            // Crear DetallePedido
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            // El precio unitario debe ser el precio actual del producto
            detalle.setPrecioUnitario(producto.getPrecio()); 
            
            // Calcular subtotal del detalle
            BigDecimal subtotalDetalle = producto.getPrecio().multiply(new BigDecimal(cantidad));
            detalle.setSubtotal(subtotalDetalle.setScale(2, RoundingMode.HALF_UP));
            
            nuevoPedido.addDetalle(detalle); // Añade el detalle al pedido y asigna la referencia bidireccional
            subtotalGeneral = subtotalGeneral.add(subtotalDetalle);

            // Actualizar Stock del Producto (Operación Crítica)
            producto.setCantidadStock(producto.getCantidadStock() - cantidad);
            productoRepository.save(producto);
        }

        // --- 4. Calcular Monto Total del Pedido ---
        BigDecimal montoTotal = subtotalGeneral.add(nuevoPedido.getMontoEnvio());
        nuevoPedido.setMontoTotal(montoTotal.setScale(2, RoundingMode.HALF_UP));

        // --- 5. Guardar el Pedido Completo (cascada guarda los detalles) ---
        return pedidoRepository.save(nuevoPedido);
    }
    
    // READ ALL (Usuario autenticado)
    public List<Pedido> obtenerMisPedidos(Long idUsuario) {
        return pedidoRepository.findByUsuario_IdUsuarioOrderByFechaPedidoDesc(idUsuario);
    }
    
    // READ BY ID (Usuario autenticado - verificación de propiedad)
    public Optional<Pedido> obtenerPedidoPorIdYUsuario(Long idPedido, Long idUsuario) {
        return pedidoRepository.findByIdPedidoAndUsuario_IdUsuario(idPedido, idUsuario);
    }

    // READ ALL (Administrador)
    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll();
    }
    
    // READ BY ID (Administrador)
    public Optional<Pedido> obtenerPedidoPorId(Long idPedido) {
        return pedidoRepository.findById(idPedido);
    }
    
    // Método de Negocio: Cambiar Estado (Solo para Admin)
    @Transactional
    public Pedido actualizarEstado(
        Long idPedido, 
        Integer idNuevoEstado
        
        ) {
        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + idPedido));

        EstadoPedido nuevoEstado = estadoPedidoRepository.findByIdNuevoEstado(idNuevoEstado)
            .orElseThrow(() -> new IllegalArgumentException("Estado de pedido no válido."));
            
        pedido.setIdEstado(nuevoEstado);
        
        // Se podría agregar lógica adicional aquí (ej. enviar email al cliente, manejar devoluciones, etc.)

        return pedidoRepository.save(pedido);
    }
    
    // Método de Negocio: Cancelar Pedido (Usuario)
    @Transactional
    public Pedido cancelarPedido(Long idPedido, Long idUsuario) {
        Pedido pedido = pedidoRepository.findByIdPedidoAndUsuario_IdUsuario(idPedido, idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado o no pertenece al usuario."));

        String estadoActual = pedido.getIdEstado().getNombreEstado();
        
        // Regla de Negocio: Solo se puede cancelar si está 'Pendiente Pago' o 'Pagado'
        if ("Pendiente".equals(estadoActual) || "Aprobado".equals(estadoActual)) {
            // Revertir Stock (Operación Crítica)
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setCantidadStock(producto.getCantidadStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
            
            // Asignar el estado de Cancelado (asumo ID 3 para 'Anulado')
            EstadoPedido estadoCancelado = estadoPedidoRepository.findByIdNuevoEstado(3)
                .orElseThrow(() -> new IllegalStateException("Estado 'Cancelado' no encontrado."));
                
            pedido.setIdEstado(estadoCancelado);
            return pedidoRepository.save(pedido);
        } else {
            throw new IllegalStateException("El pedido con estado " + estadoActual + " no puede ser cancelado.");
        }
    }
}
   