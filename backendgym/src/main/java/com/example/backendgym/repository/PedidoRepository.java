package com.example.backendgym.repository;

import com.example.backendgym.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuario_IdOrderByFechaPedidoDesc(Long usuarioId);
    Page<Pedido> findByEstadoInOrderByFechaPedidoDesc(List<Pedido.EstadoPedido> estados, Pageable pageable);

    @Query("select count(p) from Pedido p where p.fechaPedido between :inicio and :fin")
    long countByFechaPedidoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("select coalesce(sum(p.total),0) from Pedido p where p.estado = com.example.backendgym.domain.Pedido$EstadoPedido.CONFIRMADO and p.fechaPedido between :inicio and :fin")
    BigDecimal sumIngresosConfirmadosBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
