package com.example.backendgym.controller;

import com.example.backendgym.domain.Carrito;
import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.repository.CarritoRepository;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductoRepository productoRepository;
    private final ReservacionRepository reservacionRepository;
    private final CarritoRepository carritoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final DashboardService dashboardService;

    public DashboardController(ProductoRepository productoRepository,
                               ReservacionRepository reservacionRepository,
                               CarritoRepository carritoRepository,
                               SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                               DashboardService dashboardService) {
        this.productoRepository = productoRepository;
        this.reservacionRepository = reservacionRepository;
        this.carritoRepository = carritoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{usuarioId}")
    public Map<String, Object> resumen(@PathVariable Long usuarioId) {
        return dashboardService.resumen(usuarioId);
    }
}
