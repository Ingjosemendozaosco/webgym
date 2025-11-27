package com.example.backendgym.controller;

import com.example.backendgym.domain.Membresia;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.MembresiaRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.service.MembresiaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {

    private final MembresiaRepository membresiaRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final MembresiaService membresiaService;

    public MembresiaController(MembresiaRepository membresiaRepository,
                            SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                            MembresiaService membresiaService) {
        this.membresiaRepository = membresiaRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.membresiaService = membresiaService;
    }

    @GetMapping
    public List<Membresia> listar() { return membresiaService.listar(); }

    @GetMapping("/{id}")
    public Membresia obtener(@PathVariable Long id) { return membresiaService.obtener(id); }

    // Admin: crear/actualizar/eliminar
    @PostMapping
    public Membresia crear(@RequestBody Membresia m) { return membresiaService.crear(m); }

    @PutMapping("/{id}")
    public Membresia actualizar(@PathVariable Long id, @RequestBody Membresia m) { return membresiaService.actualizar(id, m); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { membresiaService.eliminar(id); }

    @GetMapping("/{id}/suscriptores")
    public List<java.util.Map<String, Object>> suscriptores(@PathVariable Long id) { return membresiaService.suscriptores(id); }

    @PostMapping("/{id}/adquirir")
    public SuscripcionUsuario adquirir(@PathVariable Long id, @RequestParam Long usuarioId, @RequestParam(required = false) BigDecimal monto) {
        return membresiaService.adquirir(id, usuarioId, monto);
    }
}
