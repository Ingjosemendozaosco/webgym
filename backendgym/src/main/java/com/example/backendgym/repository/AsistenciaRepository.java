package com.example.backendgym.repository;

import com.example.backendgym.domain.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByUsuario_IdOrderByFechaHoraDesc(Long usuarioId);

    Optional<Asistencia> findTopByUsuario_IdOrderByFechaHoraDesc(Long usuarioId);
}
