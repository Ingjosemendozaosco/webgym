package com.example.backendgym.controller;

import com.example.backendgym.domain.ConsultaSoporte;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.ConsultaSoporteRepository;
import com.example.backendgym.service.ConsultaService;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas/me")
public class ConsultasMeController {

    private final ConsultaSoporteRepository repo;
    private final UsuarioRepository usuarios;
    private final ConsultaService consultaService;

    public ConsultasMeController(ConsultaSoporteRepository repo, UsuarioRepository usuarios, ConsultaService consultaService) {
        this.repo = repo;
        this.usuarios = usuarios;
        this.consultaService = consultaService;
    }

    @PostMapping
    public ConsultaSoporte crearMe(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        Usuario u = usuarios.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String asunto = str(body.get("asunto"));
        String mensaje = str(body.get("mensaje"));
        return consultaService.crear(u.getId(), asunto, mensaje);
    }

    @GetMapping
    public List<ConsultaSoporte> misConsultas(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        Usuario u = usuarios.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return consultaService.misConsultas(u.getId());
    }

    private static String str(Object o) { return o == null ? null : o.toString(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
