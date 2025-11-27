package com.example.backendgym.repository;

import com.example.backendgym.domain.SuscripcionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SuscripcionUsuarioRepository extends JpaRepository<SuscripcionUsuario, Long> {
    List<SuscripcionUsuario> findByUsuario_IdAndEstado(Long usuarioId, SuscripcionUsuario.Estado estado);
    List<SuscripcionUsuario> findByUsuario_Id(Long usuarioId);
    List<SuscripcionUsuario> findByMembresia_Id(Long membresiaId);
    List<SuscripcionUsuario> findByPlanSuscripcion_Id(Long planId);
    long countByEstado(SuscripcionUsuario.Estado estado);

    @Query("select s.planSuscripcion.id, s.planSuscripcion.nombre, count(s) from SuscripcionUsuario s where s.planSuscripcion is not null and s.fechaInicio between :inicio and :fin group by s.planSuscripcion.id, s.planSuscripcion.nombre order by count(s) desc")
    List<Object[]> topPlanesBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("select s.membresia.id, s.membresia.nombre, count(s) from SuscripcionUsuario s where s.membresia is not null and s.fechaInicio between :inicio and :fin group by s.membresia.id, s.membresia.nombre order by count(s) desc")
    List<Object[]> topMembresiasBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
