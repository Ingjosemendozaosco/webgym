package com.example.backendgym.controller;

import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.SuscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {

    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                                 UsuarioRepository usuarioRepository,
                                 SuscripcionService suscripcionService) {
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.suscripcionService = suscripcionService;
    }

    @GetMapping("/mias")
    public List<SuscripcionUsuario> mias(Authentication auth) {
        Long uid = resolveUsuarioId(null, auth);
        return suscripcionUsuarioRepository.findByUsuario_Id(uid);
    }

    @PostMapping
    public SuscripcionUsuario crear(@RequestBody Map<String, Object> body, Authentication auth) {
        Long uid = resolveUsuarioId(null, auth);
        Long membresiaId = getLong(body.get("membresiaId"));
        Long planId = getLong(body.get("planSuscripcionId"));
        BigDecimal monto = toBigDecimal(body.get("monto"));
        String metodoPago = (String) body.get("metodoPago");
        String comprobanteUrl = (String) body.get("comprobanteUrl");
        return suscripcionService.crear(uid, membresiaId, planId, monto, metodoPago, comprobanteUrl);
    }

    @PostMapping("/iniciar")
    public Map<String, Object> iniciar(@RequestBody Map<String, Object> body, Authentication auth) {
        Long uid = resolveUsuarioId(null, auth);
        Long membresiaId = getLong(body.get("membresiaId"));
        Long planId = getLong(body.get("planSuscripcionId"));
        BigDecimal monto = toBigDecimal(body.get("monto"));
        return suscripcionService.iniciar(uid, membresiaId, planId, monto);
    }

    private Long resolveUsuarioId(Long usuarioIdParam, Authentication auth) {
        if (usuarioIdParam != null) return usuarioIdParam;
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Long getLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return null; }
    }
}
