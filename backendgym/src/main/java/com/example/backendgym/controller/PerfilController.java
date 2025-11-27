package com.example.backendgym.controller;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.domain.Asistencia;
import com.example.backendgym.repository.PedidoRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Transactional
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final PerfilService perfilService;

    public PerfilController(UsuarioRepository usuarioRepository,
                            PedidoRepository pedidoRepository,
                            SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                            PerfilService perfilService) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.perfilService = perfilService;
    }

    @GetMapping
    public Usuario ver(@RequestParam(required = false) Long usuarioId, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return perfilService.ver(uid);
    }

    @PatchMapping
    public Usuario editar(@RequestParam(required = false) Long usuarioId, @RequestBody Map<String, Object> body, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return perfilService.editar(uid, body);
    }

    @PatchMapping("/password")
    public Map<String, String> cambiarPassword(@RequestParam(required = false) Long usuarioId,
                                               @RequestBody Map<String, String> body,
                                               Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return perfilService.cambiarPassword(uid, body);
    }

    @GetMapping("/pedidos")
    public List<Pedido> pedidos(@RequestParam(required = false) Long usuarioId, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return perfilService.pedidos(uid);
    }

    @GetMapping("/planes")
    public List<Map<String, Object>> planes(@RequestParam(required = false) Long usuarioId, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return perfilService.planes(uid);
    }

    @GetMapping("/asistencias")
    public List<Asistencia> asistencias(@RequestParam(required = false) Long usuarioId, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return perfilService.asistencias(uid);
    }

    @GetMapping("/concurrencia")
    public Map<String, Long> concurrenciaGym() {
        long dentro = perfilService.contarUsuariosDentro();
        Map<String, Long> res = new HashMap<>();
        res.put("dentro", dentro);
        return res;
    }

    @GetMapping("/asistencia-resumen-usuarios")
    public List<Map<String, Object>> resumenUsuarios() {
        return perfilService.resumenAsistenciaUsuarios();
    }

    private Long resolveUsuarioId(Long usuarioIdParam, Authentication auth) {
        if (usuarioIdParam != null) return usuarioIdParam;
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
