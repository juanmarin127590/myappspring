package com.myapp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // Método para obtener todos los pedidos de un usuario específico
    List<Pedido> findByUsuario_IdUsuarioOrderByFechaPedidoDesc(Long idUsuario);

    // Método para obtener un pedido por ID y verificar que pertenezca al usuario
    Optional<Pedido> findByIdPedidoAndUsuario_IdUsuario(Long idPedido, Long idUsuario);
    
}
