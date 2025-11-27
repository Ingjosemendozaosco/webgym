package com.example.backendgym.controller;

import com.example.backendgym.domain.Calificacion;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.CalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final UsuarioRepository usuarioRepository;

    public CalificacionController(CalificacionService calificacionService,
                                  UsuarioRepository usuarioRepository) {
        this.calificacionService = calificacionService;
        this.usuarioRepository = usuarioRepository;
    }

    private Long resolveUsuarioId(Long usuarioIdParam, Authentication auth) {
        if (usuarioIdParam != null) return usuarioIdParam;
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/producto/{productoId}")
    public Calificacion calificarProducto(@PathVariable Long productoId,
                                           @RequestParam(required = false) Long usuarioId,
                                           @RequestBody Map<String, Object> body,
                                           Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        int puntuacion = Integer.parseInt(String.valueOf(body.getOrDefault("puntuacion", 0)));
        String comentario = body.get("comentario") != null ? String.valueOf(body.get("comentario")) : null;
        return calificacionService.calificarProducto(uid, productoId, puntuacion, comentario);
    }

    @GetMapping("/producto/{productoId}")
    public List<Calificacion> listarProducto(@PathVariable Long productoId) {
        return calificacionService.listarPorProducto(productoId);
    }

    @GetMapping("/producto/{productoId}/resumen")
    public Map<String, Object> resumenProducto(@PathVariable Long productoId) {
        return calificacionService.resumenProducto(productoId);
    }

    @PostMapping("/membresia/{membresiaId}")
    public Calificacion calificarMembresia(@PathVariable Long membresiaId,
                                            @RequestParam(required = false) Long usuarioId,
                                            @RequestBody Map<String, Object> body,
                                            Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        int puntuacion = Integer.parseInt(String.valueOf(body.getOrDefault("puntuacion", 0)));
        String comentario = body.get("comentario") != null ? String.valueOf(body.get("comentario")) : null;
        return calificacionService.calificarMembresia(uid, membresiaId, puntuacion, comentario);
    }

    @GetMapping("/membresia/{membresiaId}")
    public List<Calificacion> listarMembresia(@PathVariable Long membresiaId) {
        return calificacionService.listarPorMembresia(membresiaId);
    }

    @GetMapping("/membresia/{membresiaId}/resumen")
    public Map<String, Object> resumenMembresia(@PathVariable Long membresiaId) {
        return calificacionService.resumenMembresia(membresiaId);
    }

    @PostMapping("/plan/{planId}")
    public Calificacion calificarPlan(@PathVariable Long planId,
                                       @RequestParam(required = false) Long usuarioId,
                                       @RequestBody Map<String, Object> body,
                                       Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        int puntuacion = Integer.parseInt(String.valueOf(body.getOrDefault("puntuacion", 0)));
        String comentario = body.get("comentario") != null ? String.valueOf(body.get("comentario")) : null;
        return calificacionService.calificarPlan(uid, planId, puntuacion, comentario);
    }

    @GetMapping("/plan/{planId}")
    public List<Calificacion> listarPlan(@PathVariable Long planId) {
        return calificacionService.listarPorPlan(planId);
    }

    @GetMapping("/plan/{planId}/resumen")
    public Map<String, Object> resumenPlan(@PathVariable Long planId) {
        return calificacionService.resumenPlan(planId);
    }
}
