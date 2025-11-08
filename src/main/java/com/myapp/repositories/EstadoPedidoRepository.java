package com.myapp.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myapp.models.EstadoPedido;


@Repository
public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Long>{
}
