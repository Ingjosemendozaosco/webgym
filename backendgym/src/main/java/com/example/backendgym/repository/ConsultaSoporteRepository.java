package com.example.backendgym.repository;

import com.example.backendgym.domain.ConsultaSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ConsultaSoporteRepository extends JpaRepository<ConsultaSoporte, Long> {
    List<ConsultaSoporte> findByUsuario_IdOrderByFechaCreacionDesc(Long usuarioId);

    Page<ConsultaSoporte> findByEstadoOrderByFechaCreacionDesc(ConsultaSoporte.Estado estado, Pageable pageable);

    Page<ConsultaSoporte> findAllByOrderByFechaCreacionDesc(Pageable pageable);
}
