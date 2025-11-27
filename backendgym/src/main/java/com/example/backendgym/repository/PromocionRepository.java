package com.example.backendgym.repository;

import com.example.backendgym.domain.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    @Query("select p from Promocion p where (:q is null or lower(p.titulo) like lower(concat('%', :q, '%')) or lower(p.descripcion) like lower(concat('%', :q, '%')))" +
           " and (:tipo is null or p.tipo = :tipo)" +
           " and (:evento is null or lower(p.evento) = lower(:evento))" +
           " and (:soloActivas = false or (p.activo = true and p.fechaInicio <= :ahora and (p.fechaFin is null or p.fechaFin >= :ahora)))")
    Page<Promocion> search(@Param("q") String q,
                           @Param("tipo") Promocion.Tipo tipo,
                           @Param("evento") String evento,
                           @Param("soloActivas") boolean soloActivas,
                           @Param("ahora") LocalDateTime ahora,
                           Pageable pageable);

    @Query("select distinct p from Promocion p join p.productos prod where (:q is null or lower(p.titulo) like lower(concat('%', :q, '%')) or lower(p.descripcion) like lower(concat('%', :q, '%')))" +
           " and (:tipo is null or p.tipo = :tipo)" +
           " and (:evento is null or lower(p.evento) = lower(:evento))" +
           " and (:categoria is null or prod.categoria = :categoria)" +
           " and (:soloActivas = false or (p.activo = true and p.fechaInicio <= :ahora and (p.fechaFin is null or p.fechaFin >= :ahora)))")
    Page<Promocion> searchByCategoria(@Param("q") String q,
                                      @Param("tipo") Promocion.Tipo tipo,
                                      @Param("categoria") String categoria,
                                      @Param("evento") String evento,
                                      @Param("soloActivas") boolean soloActivas,
                                      @Param("ahora") LocalDateTime ahora,
                                      Pageable pageable);

    @Query("select p from Promocion p where p.activo = true and p.fechaInicio <= :ahora and p.fechaFin is not null and p.fechaFin >= :ahora and function('timestampdiff', MINUTE, p.fechaInicio, p.fechaFin) <= :minutos")
    Page<Promocion> findFlash(@Param("ahora") LocalDateTime ahora,
                              @Param("minutos") long minutos,
                              Pageable pageable);

    @Query("select p from Promocion p where p.activo = true and p.fechaInicio <= :ahora and (p.fechaFin is null or p.fechaFin >= :ahora)")
    List<Promocion> findActivas(@Param("ahora") LocalDateTime ahora);
}
