package com.example.backendgym.repository;

import com.example.backendgym.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("select distinct p.categoria from Producto p where p.categoria is not null and p.categoria <> ''")
    java.util.List<String> findDistinctCategorias();

    java.util.List<Producto> findTop5ByDisponibleTrueOrderByPopularidadDesc();

    @Query("select p from Producto p where p.disponible = true and (lower(p.nombre) like lower(concat('%', ?1, '%')) or lower(p.descripcion) like lower(concat('%', ?1, '%')))")
    Page<Producto> searchDisponibleByNombreOrDescripcion(String q, Pageable pageable);

    @Query("select p from Producto p where p.disponible = true and p.categoria = ?1 and (lower(p.nombre) like lower(concat('%', ?2, '%')) or lower(p.descripcion) like lower(concat('%', ?2, '%')))")
    Page<Producto> searchDisponibleByCategoriaAndNombreOrDescripcion(String categoria, String q, Pageable pageable);

    @Query("select p from Producto p where p.disponible = true and p.stock > 0 and (lower(p.nombre) like lower(concat('%', ?1, '%')) or lower(p.descripcion) like lower(concat('%', ?1, '%')))")
    Page<Producto> searchDisponibleConStockByNombreOrDescripcion(String q, Pageable pageable);

    @Query("select p from Producto p where p.disponible = true and p.stock > 0 and p.categoria = ?1 and (lower(p.nombre) like lower(concat('%', ?2, '%')) or lower(p.descripcion) like lower(concat('%', ?2, '%')))")
    Page<Producto> searchDisponibleConStockByCategoriaAndNombreOrDescripcion(String categoria, String q, Pageable pageable);

    // Sin filtrar por disponible (para vistas de administración)
    @Query("select p from Producto p where (lower(p.nombre) like lower(concat('%', ?1, '%')) or lower(p.descripcion) like lower(concat('%', ?1, '%')))")
    Page<Producto> searchAllByNombreOrDescripcion(String q, Pageable pageable);

    @Query("select p from Producto p where p.categoria = ?1 and (lower(p.nombre) like lower(concat('%', ?2, '%')) or lower(p.descripcion) like lower(concat('%', ?2, '%')))")
    Page<Producto> searchAllByCategoriaAndNombreOrDescripcion(String categoria, String q, Pageable pageable);

    @Query("select p from Producto p where p.stock > 0 and (lower(p.nombre) like lower(concat('%', ?1, '%')) or lower(p.descripcion) like lower(concat('%', ?1, '%')))")
    Page<Producto> searchAllConStockByNombreOrDescripcion(String q, Pageable pageable);

    @Query("select p from Producto p where p.stock > 0 and p.categoria = ?1 and (lower(p.nombre) like lower(concat('%', ?2, '%')) or lower(p.descripcion) like lower(concat('%', ?2, '%')))")
    Page<Producto> searchAllConStockByCategoriaAndNombreOrDescripcion(String categoria, String q, Pageable pageable);

    // Filtrado explícito por disponible (true/false)
    @Query("select p from Producto p where p.disponible = ?1 and (lower(p.nombre) like lower(concat('%', ?2, '%')) or lower(p.descripcion) like lower(concat('%', ?2, '%')))")
    Page<Producto> searchByDisponibleAndNombreOrDescripcion(boolean disponible, String q, Pageable pageable);

    @Query("select p from Producto p where p.disponible = ?1 and p.categoria = ?2 and (lower(p.nombre) like lower(concat('%', ?3, '%')) or lower(p.descripcion) like lower(concat('%', ?3, '%')))")
    Page<Producto> searchByDisponibleAndCategoriaAndNombreOrDescripcion(boolean disponible, String categoria, String q, Pageable pageable);

    @Query("select p from Producto p where p.disponible = ?1 and p.stock > 0 and (lower(p.nombre) like lower(concat('%', ?2, '%')) or lower(p.descripcion) like lower(concat('%', ?2, '%')))")
    Page<Producto> searchByDisponibleConStockAndNombreOrDescripcion(boolean disponible, String q, Pageable pageable);

    @Query("select p from Producto p where p.disponible = ?1 and p.stock > 0 and p.categoria = ?2 and (lower(p.nombre) like lower(concat('%', ?3, '%')) or lower(p.descripcion) like lower(concat('%', ?3, '%')))")
    Page<Producto> searchByDisponibleConStockAndCategoriaAndNombreOrDescripcion(boolean disponible, String categoria, String q, Pageable pageable);
}
