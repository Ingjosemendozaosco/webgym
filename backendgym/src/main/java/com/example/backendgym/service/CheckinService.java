package com.example.backendgym.service;

import com.example.backendgym.domain.Asistencia;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.AsistenciaRepository;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CheckinService {

    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;

    public CheckinService(UsuarioRepository usuarioRepository,
                          AsistenciaRepository asistenciaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    public Map<String, Object> registrarPorToken(String qrToken) {
        if (qrToken == null || qrToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR_TOKEN_REQUERIDO");
        }

        Usuario usuario = usuarioRepository.findByQrToken(qrToken.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR_NO_VALIDO"));

        LocalDateTime ahoraPe = ZonedDateTime.now(ZoneId.of("America/Lima")).toLocalDateTime();

        Asistencia ultima = asistenciaRepository
                .findTopByUsuario_IdOrderByFechaHoraDesc(usuario.getId())
                .orElse(null);

        Asistencia saved;
        Asistencia.Tipo nuevoTipo;
        if (ultima == null || ultima.getTipo() == Asistencia.Tipo.SALIDA) {
            // No hay registros previos o el último fue SALIDA: crear nueva ENTRADA
            nuevoTipo = Asistencia.Tipo.ENTRADA;
        } else {
            // El último registro es ENTRADA: registrar una nueva SALIDA
            nuevoTipo = Asistencia.Tipo.SALIDA;
        }

        Asistencia nueva = new Asistencia();
        nueva.setUsuario(usuario);
        nueva.setFechaHora(ahoraPe);
        nueva.setTipo(nuevoTipo);
        saved = asistenciaRepository.save(nueva);

        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("usuarioId", usuario.getId());
        res.put("nombreCompleto", usuario.getNombre() + " " + usuario.getApellido());
        res.put("tipo", saved.getTipo().name());
        res.put("fechaHora", saved.getFechaHora());
        return res;
    }
}
