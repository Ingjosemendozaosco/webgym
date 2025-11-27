package com.example.backendgym.controller;

import com.example.backendgym.repository.PedidoRepository;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.DetallePedidoRepository;
import com.example.backendgym.service.AdminEstadisticasService;
import com.example.backendgym.domain.SuscripcionUsuario;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/estadisticas")
public class AdminEstadisticasController {
    private final PedidoRepository pedidoRepo;
    private final ReservacionRepository reservaRepo;
    private final SuscripcionUsuarioRepository susRepo;
    private final DetallePedidoRepository detalleRepo;
    private final AdminEstadisticasService adminEstadisticasService;

    public AdminEstadisticasController(PedidoRepository pedidoRepo,
                                       ReservacionRepository reservaRepo,
                                       SuscripcionUsuarioRepository susRepo,
                                       DetallePedidoRepository detalleRepo,
                                       AdminEstadisticasService adminEstadisticasService) {
        this.pedidoRepo = pedidoRepo;
        this.reservaRepo = reservaRepo;
        this.susRepo = susRepo;
        this.detalleRepo = detalleRepo;
        this.adminEstadisticasService = adminEstadisticasService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@RequestParam String from, @RequestParam String to) {
        return adminEstadisticasService.overview(from, to);
    }

    @GetMapping("/ingresos-series")
    public List<Map<String, Object>> ingresosSeries(@RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "day") String groupBy) {
        return adminEstadisticasService.ingresosSeries(from, to, groupBy);
    }

    @GetMapping("/pedidos-series")
    public List<Map<String, Object>> pedidosSeries(@RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "day") String groupBy) {
        return adminEstadisticasService.pedidosSeries(from, to, groupBy);
    }

    @GetMapping("/reservas-series")
    public List<Map<String, Object>> reservasSeries(@RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "day") String groupBy) {
        return adminEstadisticasService.reservasSeries(from, to, groupBy);
    }

    @GetMapping("/ultimos-pedidos")
    public List<Map<String, Object>> ultimosPedidos(@RequestParam(defaultValue = "10") int limit) {
        return adminEstadisticasService.ultimosPedidos(limit);
    }

    @GetMapping("/tops")
    public Map<String, Object> tops(@RequestParam String from, @RequestParam String to, @RequestParam(defaultValue = "5") int limit) {
        return adminEstadisticasService.tops(from, to, limit);
    }
}
