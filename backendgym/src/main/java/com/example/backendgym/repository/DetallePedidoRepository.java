package com.example.backendgym.repository;

import com.example.backendgym.domain.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("select d.producto.id, d.producto.nombre, coalesce(sum(d.subtotal),0), coalesce(sum(d.cantidad),0) " +
           "from DetallePedido d join d.pedido p " +
           "where p.estado = com.example.backendgym.domain.Pedido$EstadoPedido.CONFIRMADO and p.fechaPedido between :inicio and :fin " +
           "group by d.producto.id, d.producto.nombre order by sum(d.subtotal) desc")
    List<Object[]> topProductosPorIngresos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("select d.producto.id, d.producto.nombre, coalesce(sum(d.subtotal),0), coalesce(sum(d.cantidad),0) " +
           "from DetallePedido d join d.pedido p " +
           "where p.estado = com.example.backendgym.domain.Pedido$EstadoPedido.CONFIRMADO and p.fechaPedido between :inicio and :fin " +
           "group by d.producto.id, d.producto.nombre order by sum(d.cantidad) desc")
    List<Object[]> topProductosPorUnidades(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
