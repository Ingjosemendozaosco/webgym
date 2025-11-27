package com.example.backendgym.service;

import com.example.backendgym.domain.Carrito;
import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.repository.CarritoRepository;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final ProductoPrecioService productoPrecioService;

    public CarritoService(CarritoRepository carritoRepository,
                          ProductoRepository productoRepository,
                          SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                          ProductoPrecioService productoPrecioService) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.productoPrecioService = productoPrecioService;
    }

    public Map<String, Object> obtener(Long uid) {
        var usuario = new com.example.backendgym.domain.Usuario();
        usuario.setId(uid);
        List<Carrito> items = carritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
        BigDecimal subtotal = items.stream().map(Carrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int descuentoPlan = obtenerDescuentoPlan(uid);
        BigDecimal descuentoMonto = subtotal.multiply(new BigDecimal(descuentoPlan)).divide(new BigDecimal(100));
        BigDecimal total = subtotal.subtract(descuentoMonto);
        Map<String, Object> res = new HashMap<>();
        res.put("items", items);
        res.put("subtotal", subtotal);
        res.put("descuentoPlanPorcentaje", descuentoPlan);
        res.put("descuentoPlanMonto", descuentoMonto);
        res.put("total", total);
        res.put("moneda", "PEN");
        return res;
    }

    @Transactional
    public Carrito agregar(Long uid, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null || !producto.isDisponible()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (producto.getStock() == null || producto.getStock() < cantidad) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var usuario = new com.example.backendgym.domain.Usuario();
        usuario.setId(uid);
        var existenteOpt = carritoRepository.findByUsuarioAndProductoId(usuario, productoId);
        Carrito item;
        if (existenteOpt.isPresent()) {
            item = existenteOpt.get();
            int nueva = item.getCantidad() + cantidad;
            if (producto.getStock() == null || nueva > producto.getStock()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            item.setCantidad(nueva);
        } else {
            item = new Carrito();
            item.setUsuario(usuario);
            item.setProducto(producto);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(productoPrecioService.getPrecioConPromocion(producto));
        }
        return carritoRepository.save(item);
    }

    @Transactional
    public Carrito actualizarCantidad(Long itemId, int cantidad) {
        Carrito item = carritoRepository.findById(itemId).orElse(null);
        if (item == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (cantidad < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Producto producto = productoRepository.findById(item.getProducto().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (producto.getStock() == null || cantidad > producto.getStock()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        item.setCantidad(cantidad);
        item.setPrecioUnitario(productoPrecioService.getPrecioConPromocion(producto));
        return carritoRepository.save(item);
    }

    @Transactional
    public void eliminar(Long itemId) {
        Carrito item = carritoRepository.findById(itemId).orElse(null);
        if (item == null) return;
        carritoRepository.delete(item);
    }

    @Transactional
    public void vaciar(Long uid) {
        var usuario = new com.example.backendgym.domain.Usuario();
        usuario.setId(uid);
        List<Carrito> items = carritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
        carritoRepository.deleteByUsuario(usuario);
    }

    private int obtenerDescuentoPlan(Long usuarioId) {
        List<SuscripcionUsuario> list = suscripcionUsuarioRepository.findByUsuario_IdAndEstado(usuarioId, SuscripcionUsuario.Estado.ACTIVA);
        LocalDateTime ahora = LocalDateTime.now();
        int max = 0;
        for (SuscripcionUsuario su : list) {
            if (su.getFechaInicio() != null && su.getFechaFin() != null && (su.getFechaInicio().isAfter(ahora) || su.getFechaFin().isBefore(ahora))) continue;
            if (su.getPlanSuscripcion() != null && su.getPlanSuscripcion().getTipo() == com.example.backendgym.domain.PlanSuscripcion.Tipo.DESCUENTO) {
                Integer p = su.getPlanSuscripcion().getDescuentoPorcentaje();
                if (p != null) max = Math.max(max, p);
            }
            if (su.getMembresia() != null && su.getMembresia().getDescuentoPorcentaje() != null) {
                max = Math.max(max, su.getMembresia().getDescuentoPorcentaje());
            }
        }
        return max;
    }
}
