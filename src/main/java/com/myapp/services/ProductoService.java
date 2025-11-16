package com.myapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.myapp.models.Categoria;
import com.myapp.models.Producto;
import com.myapp.repositories.CategoriaRepository;
import com.myapp.repositories.ProductoRepository;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // CREATE (C) - Crear un nuevo producto
    public Producto crearProducto(Producto producto) {
        // 1. Validar unicidad del SKU
        if (productoRepository.findBySku(producto.getSku()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el SKU: " + producto.getSku());
        }

        // 2. Validar existencia y asignar Categoría
        Categoria categoria = categoriaRepository.findById(producto.getCategoria().getIdCategoria())
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + producto.getCategoria().getIdCategoria()));
        
        producto.setCategoria(categoria);
        
        return productoRepository.save(producto);
    }

    // READ ALL (R) - Obtener todos los productos (Catálogo Público)
    public List<Producto> obtenerCatalogoActivo() {
        // Retornar solo productos activos, ideal para el catálogo
        return productoRepository.findAllByActivoTrue();
    }
    
    // READ ALL (R) - Obtener todos los productos (Admin)
    public List<Producto> obtenerTodosLosProductos() {
        // Retornar todos, incluyendo inactivos, para gestión administrativa
        return productoRepository.findAll();
    }

    // READ BY ID (R) - Obtener un producto por ID
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    // UPDATE (U) - Actualizar un producto existente
    public Producto actualizarProducto(Long id, Producto productoDetalles) {
        return productoRepository.findById(id)
            .map(productoExistente -> {
                // 1. Actualizar y validar SKU si se proporciona y es diferente
                if (productoDetalles.getSku() != null && !productoExistente.getSku().equals(productoDetalles.getSku())) {
                    Optional<Producto> existingSku = productoRepository.findBySku(productoDetalles.getSku());
                    if (existingSku.isPresent() && !existingSku.get().getIdProducto().equals(id)) {
                        throw new IllegalArgumentException("Ya existe otro producto con el SKU: " + productoDetalles.getSku());
                    }
                    productoExistente.setSku(productoDetalles.getSku());
                }
                
                // 2. Actualizar y validar Categoría si se proporciona
                if (productoDetalles.getCategoria() != null && productoDetalles.getCategoria().getIdCategoria() != null) {
                    Categoria categoria = categoriaRepository.findById(productoDetalles.getCategoria().getIdCategoria())
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + productoDetalles.getCategoria().getIdCategoria()));
                    productoExistente.setCategoria(categoria);
                }

                // 3. Actualizar otros campos solo si se proporcionan (no son null)
                if (productoDetalles.getNombreProducto() != null) {
                    productoExistente.setNombreProducto(productoDetalles.getNombreProducto());
                }
                if (productoDetalles.getDescripcionLarga() != null) {
                    productoExistente.setDescripcionLarga(productoDetalles.getDescripcionLarga());
                }
                if (productoDetalles.getPrecio() != null) {
                    productoExistente.setPrecio(productoDetalles.getPrecio());
                }
                if (productoDetalles.getCantidadStock() != null) {
                    productoExistente.setCantidadStock(productoDetalles.getCantidadStock());
                }
                if (productoDetalles.getActivo() != null) {
                    productoExistente.setActivo(productoDetalles.getActivo());
                }
                
                return productoRepository.save(productoExistente);
            }).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
    }

    // DELETE (D) - Eliminar un producto (se recomienda desactivar en lugar de borrar)
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        // Estrategia de Soft Delete (desactivar) en lugar de Hard Delete
        producto.setActivo(false);
        productoRepository.save(producto);
    }
}
