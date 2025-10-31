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
                // 1. Validación de SKU único si el SKU ha cambiado
                if (!productoExistente.getSku().equals(productoDetalles.getSku())) {
                    Optional<Producto> existingSku = productoRepository.findBySku(productoDetalles.getSku());
                    if (existingSku.isPresent() && !existingSku.get().getIdProducto().equals(id)) {
                        throw new IllegalArgumentException("Ya existe otro producto con el SKU: " + productoDetalles.getSku());
                    }
                }
                
                // 2. Validación de Categoría
                Categoria categoria = categoriaRepository.findById(productoDetalles.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + productoDetalles.getCategoria().getIdCategoria()));

                // 3. Aplicar cambios
                productoExistente.setSku(productoDetalles.getSku());
                productoExistente.setNombreProducto(productoDetalles.getNombreProducto());
                productoExistente.setDescripcionLarga(productoDetalles.getDescripcionLarga());
                productoExistente.setPrecio(productoDetalles.getPrecio());
                productoExistente.setCantidadStock(productoDetalles.getCantidadStock());
                productoExistente.setActivo(productoDetalles.getActivo());
                productoExistente.setCategoria(categoria);
                
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
