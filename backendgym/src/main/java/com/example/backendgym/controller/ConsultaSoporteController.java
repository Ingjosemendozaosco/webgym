package com.example.backendgym.controller;

import com.example.backendgym.domain.ConsultaSoporte;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.ConsultaSoporteRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.ConsultaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaSoporteController {

    private final ConsultaSoporteRepository consultaRepo;
    private final UsuarioRepository usuarioRepo;
    private final ConsultaService consultaService;

    public ConsultaSoporteController(ConsultaSoporteRepository consultaRepo,
                                     UsuarioRepository usuarioRepo,
                                     ConsultaService consultaService) {
        this.consultaRepo = consultaRepo;
        this.usuarioRepo = usuarioRepo;
        this.consultaService = consultaService;
    }

    @PostMapping
    public ConsultaSoporte crear(@RequestBody Map<String, Object> body) {
        Long usuarioId = body.get("usuarioId") == null ? null : Long.valueOf(body.get("usuarioId").toString());
        String asunto = body.get("asunto") == null ? null : body.get("asunto").toString();
        String mensaje = body.get("mensaje") == null ? null : body.get("mensaje").toString();
        return consultaService.crearDesdeAdmin(usuarioId, asunto, mensaje);
    }

    @GetMapping
    public List<ConsultaSoporte> listar(@RequestParam Long usuarioId) {
        return consultaRepo.findByUsuario_IdOrderByFechaCreacionDesc(usuarioId);
    }

    @GetMapping("/{id}")
    public ConsultaSoporte obtener(@PathVariable Long id) {
        return consultaRepo.findById(id).orElse(null);
    }

    @PostMapping("/{id}/responder")
    public ConsultaSoporte responder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String respuesta = body.get("respuesta") == null ? null : body.get("respuesta").toString();
        return consultaService.responder(id, respuesta);
    }

    @PostMapping("/{id}/cerrar")
    public ConsultaSoporte cerrar(@PathVariable Long id) {
        return consultaService.cerrar(id);
    }
}
