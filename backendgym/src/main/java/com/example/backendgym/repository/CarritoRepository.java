package com.example.backendgym.repository;

import com.example.backendgym.domain.Carrito;
import com.example.backendgym.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuarioAndProductoId(Usuario usuario, Long productoId);
    List<Carrito> findByUsuarioOrderByFechaAgregadoDesc(Usuario usuario);
    List<Carrito> findByUsuario_IdOrderByFechaAgregadoDesc(Long usuarioId);
    void deleteByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);
    long countByUsuario(Usuario usuario);
}
