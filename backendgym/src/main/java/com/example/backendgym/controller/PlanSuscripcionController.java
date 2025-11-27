package com.example.backendgym.controller;

import com.example.backendgym.domain.PlanSuscripcion;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.PlanSuscripcionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.service.PlanSuscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/planes")
public class PlanSuscripcionController {

    private final PlanSuscripcionRepository planRepo;
    private final SuscripcionUsuarioRepository suRepo;
    private final PlanSuscripcionService planService;

    public PlanSuscripcionController(PlanSuscripcionRepository planRepo,
                                     SuscripcionUsuarioRepository suRepo,
                                     PlanSuscripcionService planService) {
        this.planRepo = planRepo;
        this.suRepo = suRepo;
        this.planService = planService;
    }

    @GetMapping
    public List<PlanSuscripcion> listar() { return planService.listar(); }

    @GetMapping("/{id}")
    public PlanSuscripcion obtener(@PathVariable Long id) { return planService.obtener(id); }

    // Admin: crear/actualizar/eliminar
    @PostMapping
    public PlanSuscripcion crear(@RequestBody PlanSuscripcion p) { return planService.crear(p); }

    @PutMapping("/{id}")
    public PlanSuscripcion actualizar(@PathVariable Long id, @RequestBody PlanSuscripcion p) { return planService.actualizar(id, p); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { planService.eliminar(id); }

    @GetMapping("/{id}/suscriptores")
    public List<java.util.Map<String, Object>> suscriptores(@PathVariable Long id) { return planService.suscriptores(id); }

    @PostMapping("/{id}/suscribirse")
    public SuscripcionUsuario suscribirse(@PathVariable Long id,
                                          @RequestParam Long usuarioId,
                                          @RequestParam(required = false) BigDecimal monto) {
        return planService.suscribirse(id, usuarioId, monto);
    }
}
