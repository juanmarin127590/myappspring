package com.myapp.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.models.MetodoPago;


@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> { 
}
