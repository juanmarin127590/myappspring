package com.myapp.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.myapp.models.CarritoCompra;
import com.myapp.models.ItemCarrito;
import com.myapp.models.Producto;
import com.myapp.models.Usuario;
import com.myapp.repositories.CarritoCompraRepository;
import com.myapp.repositories.ItemCarritoRepository;
import com.myapp.repositories.ProductoRepository;
import com.myapp.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class CarritoService {

    private final CarritoCompraRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoCompraRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene el carrito de compra del usuario o lo crea si no existe.
     */
    @Transactional
    public CarritoCompra obtenerOCrearCarrito(Long idUsuario) {
        return carritoRepository.findByUsuario_IdUsuario(idUsuario)
            .orElseGet(() -> {
                Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

                CarritoCompra nuevoCarrito = new CarritoCompra();
                nuevoCarrito.setUsuario(usuario);
                return carritoRepository.save(nuevoCarrito);
            });
    }

    /**
     * Calcula los subtotales de los ítems y el monto total del carrito.
     */
   private CarritoCompra calcularTotales(CarritoCompra carrito) {
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            carrito.setMontoTotal(BigDecimal.ZERO);
            return carrito;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            BigDecimal precioUnitario = item.getProducto().getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(item.getCantidad()));
            
            item.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
            total = total.add(subtotal);
        }
        carrito.setMontoTotal(total.setScale(2, RoundingMode.HALF_UP));
        return carrito;
    }

    /**
     * Agrega o actualiza un producto en el carrito.
     */
    @Transactional
    public CarritoCompra agregarOActualizarItem(Long idUsuario, Long idProducto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        
        CarritoCompra carrito = obtenerOCrearCarrito(idUsuario);

        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
            
        if (!producto.getActivo()) {
            throw new IllegalStateException("El producto no está activo y no se puede agregar al carrito.");
        }

        // Buscar si el ítem ya existe en el carrito
        Optional<ItemCarrito> itemExistenteOpt = itemCarritoRepository.findByCarrito_IdCarritoAndProducto_IdProducto(
            carrito.getIdCarrito(), idProducto);

        ItemCarrito item;
        if (itemExistenteOpt.isPresent()) {
            item = itemExistenteOpt.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            item = new ItemCarrito();
            item.setCarrito(carrito);
            item.setProducto(producto);
            item.setCantidad(cantidad);
        }
        
        // Validación de Stock (Importante)
        if (producto.getCantidadStock() < item.getCantidad()) {
            throw new IllegalStateException("Stock insuficiente. Solo hay " + producto.getCantidadStock() + " unidades disponibles.");
        }

        itemCarritoRepository.save(item);

        // Recargar el carrito para tener la lista de ítems actualizada
        return obtenerCarritoPorUsuario(idUsuario).get();
    }

    /**
     * Elimina un ítem del carrito.
     */
    @Transactional
    public void eliminarItem(Long idUsuario, Long idItemCarrito) {
        CarritoCompra carrito = obtenerOCrearCarrito(idUsuario);
        
        // Verificar que el ítem pertenece al carrito del usuario
        Optional<ItemCarrito> itemOpt = carrito.getItems().stream()
            .filter(i -> i.getIdItemCarrito().equals(idItemCarrito))
            .findFirst();

        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Ítem de carrito no encontrado o no pertenece a este carrito.");
        }
        
        itemCarritoRepository.delete(itemOpt.get());
    }
    
    /**
     * Limpia completamente el carrito del usuario.
     */
    @Transactional
    public void limpiarCarrito(Long idUsuario) {
        CarritoCompra carrito = obtenerOCrearCarrito(idUsuario);
        carrito.getItems().clear(); // El orphanRemoval=true y CascadeType.ALL en CarritoCompra se encargará de eliminar los ítems
        carritoRepository.save(carrito);
    }
    
    /**
     * Obtiene el carrito del usuario con los totales calculados.
     */
    public Optional<CarritoCompra> obtenerCarritoPorUsuario(Long idUsuario) {
        Optional<CarritoCompra> carritoOpt = carritoRepository.findByUsuario_IdUsuario(idUsuario);
        return carritoOpt.map(this::calcularTotales);
    }


}
