package com.example.backendgym.controller;

import com.example.backendgym.domain.Reservacion;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.service.AdminReservasService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reservas")
public class AdminReservasController {
    private final ReservacionRepository repo;
    private final AdminReservasService adminReservasService;
    public AdminReservasController(ReservacionRepository repo, AdminReservasService adminReservasService) { this.repo = repo; this.adminReservasService = adminReservasService; }

    @GetMapping("/semana")
    public List<Map<String, Object>> semana(@RequestParam String desde,
                                            @RequestParam(required = false) Long productoId,
                                            @RequestParam(required = false) Long membresiaId) {
        return adminReservasService.semana(desde, productoId, membresiaId);
    }
}
