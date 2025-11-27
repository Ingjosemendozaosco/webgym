package com.example.backendgym.service;

import com.example.backendgym.domain.Carrito;
import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.repository.CarritoRepository;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final ReservacionRepository reservacionRepository;
    private final CarritoRepository carritoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;

    public DashboardService(ProductoRepository productoRepository,
                            ReservacionRepository reservacionRepository,
                            CarritoRepository carritoRepository,
                            SuscripcionUsuarioRepository suscripcionUsuarioRepository) {
        this.productoRepository = productoRepository;
        this.reservacionRepository = reservacionRepository;
        this.carritoRepository = carritoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
    }

    public Map<String, Object> resumen(Long usuarioId) {
        Map<String, Object> data = new HashMap<>();

        List<Producto> destacados = productoRepository.findTop5ByDisponibleTrueOrderByPopularidadDesc();
        data.put("destacados", destacados);

        var futuras = reservacionRepository.findByUsuario_IdAndFechaAfter(usuarioId, LocalDateTime.now());
        data.put("proximasReservas", futuras);

        var itemsCarrito = carritoRepository.findByUsuarioOrderByFechaAgregadoDesc(new com.example.backendgym.domain.Usuario() {{ setId(usuarioId); }});
        int cantidadItems = itemsCarrito.size();
        BigDecimal subtotal = itemsCarrito.stream()
                .map(Carrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> carrito = new HashMap<>();
        carrito.put("cantidadItems", cantidadItems);
        carrito.put("subtotal", subtotal);
        carrito.put("moneda", "PEN");
        data.put("carrito", carrito);

        var planesActivos = suscripcionUsuarioRepository.findByUsuario_IdAndEstado(usuarioId, SuscripcionUsuario.Estado.ACTIVA);
        data.put("planesActivos", planesActivos);

        return data;
    }
}
