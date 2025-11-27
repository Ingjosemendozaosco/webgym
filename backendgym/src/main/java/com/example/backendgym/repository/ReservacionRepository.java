package com.example.backendgym.repository;

import com.example.backendgym.domain.Reservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.backendgym.domain.Reservacion.EstadoReservacion;

@Repository
public interface ReservacionRepository extends JpaRepository<Reservacion, Long> {
    List<Reservacion> findByUsuario_IdAndFechaAfter(Long usuarioId, LocalDateTime fecha);

    List<Reservacion> findByProducto_IdAndFechaBetween(Long productoId, LocalDateTime inicio, LocalDateTime fin);
    List<Reservacion> findByMembresia_IdAndFechaBetween(Long membresiaId, LocalDateTime inicio, LocalDateTime fin);
    List<Reservacion> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Eager fetch helpers for admin semana view
    @Query("select r from Reservacion r left join fetch r.producto p left join fetch r.membresia m left join fetch r.usuario u where r.fecha between :inicio and :fin")
    List<Reservacion> findAllWithRefsByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("select r from Reservacion r left join fetch r.producto p left join fetch r.membresia m left join fetch r.usuario u where p.id = :productoId and r.fecha between :inicio and :fin")
    List<Reservacion> findAllWithRefsByProductoAndFechaBetween(@Param("productoId") Long productoId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("select r from Reservacion r left join fetch r.producto p left join fetch r.membresia m left join fetch r.usuario u where m.id = :membresiaId and r.fecha between :inicio and :fin")
    List<Reservacion> findAllWithRefsByMembresiaAndFechaBetween(@Param("membresiaId") Long membresiaId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    List<Reservacion> findByProducto_IdAndFechaAfter(Long productoId, LocalDateTime fecha);

    List<Reservacion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<Reservacion> findByUsuario_IdAndFechaBetweenOrderByFechaAsc(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    Page<Reservacion> findByEstadoOrderByFechaDesc(EstadoReservacion estado, Pageable pageable);

    Page<Reservacion> findAllByOrderByFechaDesc(Pageable pageable);
}
