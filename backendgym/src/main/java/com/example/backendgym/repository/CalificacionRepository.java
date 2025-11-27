package com.example.backendgym.repository;

import com.example.backendgym.domain.Calificacion;
import com.example.backendgym.domain.Membresia;
import com.example.backendgym.domain.PlanSuscripcion;
import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    Optional<Calificacion> findByUsuarioAndProducto(Usuario usuario, Producto producto);

    Optional<Calificacion> findByUsuarioAndMembresia(Usuario usuario, Membresia membresia);

    Optional<Calificacion> findByUsuarioAndPlanSuscripcion(Usuario usuario, PlanSuscripcion planSuscripcion);

    List<Calificacion> findByProductoOrderByFechaCreacionDesc(Producto producto);

    List<Calificacion> findByMembresiaOrderByFechaCreacionDesc(Membresia membresia);

    List<Calificacion> findByPlanSuscripcionOrderByFechaCreacionDesc(PlanSuscripcion planSuscripcion);

    @Query("select avg(c.puntuacion) from Calificacion c where c.producto = :producto")
    Double promedioPorProducto(@Param("producto") Producto producto);

    @Query("select count(c) from Calificacion c where c.producto = :producto")
    Long cantidadPorProducto(@Param("producto") Producto producto);

    @Query("select avg(c.puntuacion) from Calificacion c where c.membresia = :membresia")
    Double promedioPorMembresia(@Param("membresia") Membresia membresia);

    @Query("select count(c) from Calificacion c where c.membresia = :membresia")
    Long cantidadPorMembresia(@Param("membresia") Membresia membresia);

    @Query("select avg(c.puntuacion) from Calificacion c where c.planSuscripcion = :plan")
    Double promedioPorPlan(@Param("plan") PlanSuscripcion plan);

    @Query("select count(c) from Calificacion c where c.planSuscripcion = :plan")
    Long cantidadPorPlan(@Param("plan") PlanSuscripcion plan);
}
