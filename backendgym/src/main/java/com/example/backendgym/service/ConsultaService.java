package com.example.backendgym.service;

import com.example.backendgym.domain.ConsultaSoporte;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.ConsultaSoporteRepository;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {
    private final ConsultaSoporteRepository repo;
    private final UsuarioRepository usuarios;

    public ConsultaService(ConsultaSoporteRepository repo, UsuarioRepository usuarios) {
        this.repo = repo;
        this.usuarios = usuarios;
    }

    // Admin: listar con filtro de estado
    public Page<ConsultaSoporte> adminList(int page, int size, String estado) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200));
        if (estado != null && !estado.isBlank()) {
            ConsultaSoporte.Estado st;
            try { st = ConsultaSoporte.Estado.valueOf(estado.toUpperCase()); }
            catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido"); }
            return repo.findByEstadoOrderByFechaCreacionDesc(st, pageable);
        }
        return repo.findAllByOrderByFechaCreacionDesc(pageable);
    }

    // Usuario actual: crear consulta
    public ConsultaSoporte crear(Long uid, String asunto, String mensaje) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (asunto == null || asunto.isBlank() || mensaje == null || mensaje.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos requeridos: asunto, mensaje");
        }
        Usuario u = usuarios.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        ConsultaSoporte c = new ConsultaSoporte();
        c.setUsuario(u);
        c.setAsunto(asunto);
        c.setMensaje(mensaje);
        c.setEstado(ConsultaSoporte.Estado.ABIERTA);
        c.setFechaCreacion(LocalDateTime.now());
        return repo.save(c);
    }

    // Usuario actual: listar sus consultas
    public List<ConsultaSoporte> misConsultas(Long uid) {
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return repo.findByUsuario_IdOrderByFechaCreacionDesc(uid);
    }

    // Crear consulta desde usuarioId explícito (lógica de ConsultaSoporteController.crear)
    public ConsultaSoporte crearDesdeAdmin(Long usuarioId, String asunto, String mensaje) {
        if (usuarioId == null || asunto == null || mensaje == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Usuario u = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ConsultaSoporte c = new ConsultaSoporte();
        c.setUsuario(u);
        c.setAsunto(asunto);
        c.setMensaje(mensaje);
        c.setEstado(ConsultaSoporte.Estado.ABIERTA);
        c.setFechaCreacion(LocalDateTime.now());
        return repo.save(c);
    }

    // Responder consulta (lógica de ConsultaSoporteController.responder)
    public ConsultaSoporte responder(Long id, String respuesta) {
        ConsultaSoporte c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (respuesta == null || respuesta.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        c.setRespuesta(respuesta);
        c.setEstado(ConsultaSoporte.Estado.RESPONDIDA);
        c.setFechaRespuesta(LocalDateTime.now());
        return repo.save(c);
    }

    // Cerrar consulta (lógica de ConsultaSoporteController.cerrar)
    public ConsultaSoporte cerrar(Long id) {
        ConsultaSoporte c = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setEstado(ConsultaSoporte.Estado.CERRADA);
        return repo.save(c);
    }
}
