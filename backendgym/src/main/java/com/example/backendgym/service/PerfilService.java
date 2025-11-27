package com.example.backendgym.service;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.domain.Asistencia;
import com.example.backendgym.domain.Rol;
import com.example.backendgym.repository.PedidoRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.repository.AsistenciaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final AsistenciaRepository asistenciaRepository;

    public PerfilService(UsuarioRepository usuarioRepository,
                         PedidoRepository pedidoRepository,
                         SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                         AsistenciaRepository asistenciaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    public Usuario ver(Long uid) {
        return usuarioRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Usuario editar(Long uid, Map<String, Object> body) {
        Usuario u = usuarioRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (body == null) return u;
        if (body.containsKey("nombre")) u.setNombre((String) body.get("nombre"));
        if (body.containsKey("apellido")) u.setApellido((String) body.get("apellido"));
        if (body.containsKey("email")) u.setEmail((String) body.get("email"));
        if (body.containsKey("telefono")) u.setTelefono((String) body.get("telefono"));
        if (body.containsKey("direccion")) u.setDireccion((String) body.get("direccion"));
        if (body.containsKey("fotoUrl")) u.setFotoUrl((String) body.get("fotoUrl"));
        if (body.containsKey("recordatoriosActivos")) {
            Object v = body.get("recordatoriosActivos");
            if (v instanceof Boolean b) {
                u.setRecordatoriosActivos(b);
            } else if (v instanceof String s) {
                u.setRecordatoriosActivos(Boolean.parseBoolean(s));
            }
        }
        return usuarioRepository.save(u);
    }

    @Transactional
    public Map<String, String> cambiarPassword(Long uid, Map<String, String> body) {
        String nueva = body == null ? null : body.get("nueva");
        if (nueva == null || nueva.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Usuario u = usuarioRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        u.setPassword(nueva);
        usuarioRepository.save(u);
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    public List<Pedido> pedidos(Long uid) {
        return pedidoRepository.findByUsuario_IdOrderByFechaPedidoDesc(uid);
    }

    public List<Asistencia> asistencias(Long uid) {
        return asistenciaRepository.findByUsuario_IdOrderByFechaHoraDesc(uid);
    }

    public long contarUsuariosDentro() {
        List<Asistencia> todas = asistenciaRepository.findAll();
        Map<Long, Asistencia> ultimaPorUsuario = new HashMap<>();
        for (Asistencia a : todas) {
            if (a == null || a.getUsuario() == null || a.getFechaHora() == null) continue;
            Long uid = a.getUsuario().getId();
            if (uid == null) continue;
            Asistencia actual = ultimaPorUsuario.get(uid);
            if (actual == null || a.getFechaHora().isAfter(actual.getFechaHora())) {
                ultimaPorUsuario.put(uid, a);
            }
        }
        long count = 0;
        for (Asistencia a : ultimaPorUsuario.values()) {
            if (a != null && a.getTipo() == Asistencia.Tipo.ENTRADA) {
                count++;
            }
        }
        return count;
    }

    public List<Map<String, Object>> resumenAsistenciaUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Asistencia> todas = asistenciaRepository.findAll();

        Map<Long, Asistencia> ultimaPorUsuario = new HashMap<>();
        Map<Long, Map<String, Object>> resumenPorUsuario = new HashMap<>();

        // Precalcular última asistencia por usuario
        for (Asistencia a : todas) {
            if (a == null || a.getUsuario() == null || a.getFechaHora() == null) continue;
            Long uid = a.getUsuario().getId();
            if (uid == null) continue;
            Asistencia actual = ultimaPorUsuario.get(uid);
            if (actual == null || a.getFechaHora().isAfter(actual.getFechaHora())) {
                ultimaPorUsuario.put(uid, a);
            }
        }

        // Ventanas de tiempo para conteos
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.DayOfWeek dow = hoy.getDayOfWeek();
        java.time.LocalDate inicioSemana = dow == java.time.DayOfWeek.MONDAY
                ? hoy
                : hoy.minusDays((dow.getValue() + 6) % 7); // lunes de esta semana
        java.time.LocalDate finSemana = hoy;

        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        // Indexar asistencias por usuario para conteos
        Map<Long, List<Asistencia>> asistenciasPorUsuario = new HashMap<>();
        for (Asistencia a : todas) {
            if (a == null || a.getUsuario() == null || a.getFechaHora() == null) continue;
            Long uid = a.getUsuario().getId();
            if (uid == null) continue;
            asistenciasPorUsuario.computeIfAbsent(uid, k -> new java.util.ArrayList<>()).add(a);
        }

        for (Usuario u : usuarios) {
            if (u == null) continue;
            if (u.getRol() != Rol.USUARIO) continue;

            Long uid = u.getId();
            if (uid == null) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("usuarioId", uid);
            m.put("nombreCompleto", (u.getNombre() == null ? "" : u.getNombre()) + " " + (u.getApellido() == null ? "" : u.getApellido()));

            Asistencia ultima = ultimaPorUsuario.get(uid);
            if (ultima != null) {
                m.put("ultimaFechaHora", ultima.getFechaHora());
                m.put("estadoActual", ultima.getTipo() == Asistencia.Tipo.ENTRADA ? "ENTRADA" : "SALIDA");
            } else {
                m.put("ultimaFechaHora", null);
                m.put("estadoActual", "SIN_REGISTROS");
            }

            int diasSemana = 0;
            int diasMes = 0;
            java.util.Set<String> diasSemanaSet = new java.util.HashSet<>();
            java.util.Set<String> diasMesSet = new java.util.HashSet<>();

            List<Asistencia> lista = asistenciasPorUsuario.get(uid);
            if (lista != null) {
                for (Asistencia a : lista) {
                    java.time.LocalDate d = a.getFechaHora().toLocalDate();
                    if (!d.isAfter(finSemana) && !d.isBefore(inicioSemana)) {
                        String key = d.toString();
                        diasSemanaSet.add(key);
                    }
                    if (d.getYear() == anioActual && d.getMonthValue() == mesActual) {
                        String key2 = d.toString();
                        diasMesSet.add(key2);
                    }
                }
            }

            diasSemana = diasSemanaSet.size();
            diasMes = diasMesSet.size();

            m.put("diasSemana", diasSemana);
            m.put("diasMes", diasMes);

            resumenPorUsuario.put(uid, m);
        }

        return new java.util.ArrayList<>(resumenPorUsuario.values());
    }

    public List<Map<String, Object>> planes(Long uid) {
        List<SuscripcionUsuario> list = suscripcionUsuarioRepository.findByUsuario_Id(uid);
        return list.stream().map(su -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", su.getId());
            m.put("estado", su.getEstado());
            m.put("monto", su.getMonto());
            m.put("fechaInicio", su.getFechaInicio());
            m.put("fechaFin", su.getFechaFin());
            if (su.getMembresia() != null) {
                m.put("tipo", "MEMBRESIA");
                try { m.put("membresiaId", su.getMembresia().getId()); } catch (Exception ignored) {}
                try { m.put("nombre", su.getMembresia().getNombre()); } catch (Exception ignored) {}
                try { m.put("descripcion", su.getMembresia().getDescripcion()); } catch (Exception ignored) {}
                try { m.put("beneficios", su.getMembresia().getBeneficios()); } catch (Exception ignored) {}
            } else if (su.getPlanSuscripcion() != null) {
                m.put("tipo", "PLAN");
                try { m.put("planSuscripcionId", su.getPlanSuscripcion().getId()); } catch (Exception ignored) {}
                try { m.put("nombre", su.getPlanSuscripcion().getNombre()); } catch (Exception ignored) {}
                try { m.put("beneficio", su.getPlanSuscripcion().getBeneficio()); } catch (Exception ignored) {}
                try { m.put("descripcion", su.getPlanSuscripcion().getDescripcion()); } catch (Exception ignored) {}
                try { m.put("horasMaxReserva", su.getPlanSuscripcion().getHorasMaxReserva()); } catch (Exception ignored) {}
            } else {
                m.put("tipo", "OTRO");
            }
            return m;
        }).toList();
    }
}
