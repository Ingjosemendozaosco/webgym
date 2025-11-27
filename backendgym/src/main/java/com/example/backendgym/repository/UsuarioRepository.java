package com.example.backendgym.repository;

import com.example.backendgym.domain.Usuario;
import com.example.backendgym.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<Usuario> findByQrToken(String qrToken);

    List<Usuario> findByRecordatoriosActivosTrue();

    @Query("SELECT u FROM Usuario u " +
           "WHERE (:rol IS NULL OR u.rol = :rol) " +
           "AND (:activo IS NULL OR u.activo = :activo) " +
           "ORDER BY u.fechaCreacion DESC")
    Page<Usuario> findByFilters(@Param("rol") Rol rol,
                                @Param("activo") Boolean activo,
                                Pageable pageable);

    @Query("SELECT u FROM Usuario u " +
           "WHERE (LOWER(u.email) LIKE :q OR LOWER(u.nombre) LIKE :q OR LOWER(u.apellido) LIKE :q) " +
           "AND (:rol IS NULL OR u.rol = :rol) " +
           "AND (:activo IS NULL OR u.activo = :activo) " +
           "ORDER BY u.fechaCreacion DESC")
    Page<Usuario> findByQueryAndFilters(@Param("q") String q,
                                        @Param("rol") Rol rol,
                                        @Param("activo") Boolean activo,
                                        Pageable pageable);
}
