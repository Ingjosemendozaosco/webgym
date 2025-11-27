package com.example.backendgym.service;

import com.example.backendgym.domain.PlanSuscripcion;
import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.Reservacion;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReservacionService {

    private final ProductoRepository productoRepository;
    private final ReservacionRepository reservacionRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final MailService mailService;

    public ReservacionService(ProductoRepository productoRepository,
                              ReservacionRepository reservacionRepository,
                              SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                              UsuarioRepository usuarioRepository,
                              MailService mailService) {
        this.productoRepository = productoRepository;
        this.reservacionRepository = reservacionRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.mailService = mailService;
    }

    public List<String> filtrosUso() {
        return productoRepository.findDistinctCategorias();
    }

    public Page<Producto> productosReservables(String q, String categoria, boolean soloConStock, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (categoria != null && !categoria.isBlank()) {
            if (soloConStock) {
                return productoRepository.searchDisponibleConStockByCategoriaAndNombreOrDescripcion(categoria, q, pageable);
            }
            return productoRepository.searchDisponibleByCategoriaAndNombreOrDescripcion(categoria, q, pageable);
        }
        if (soloConStock) {
            return productoRepository.searchDisponibleConStockByNombreOrDescripcion(q, pageable);
        }
        return productoRepository.searchDisponibleByNombreOrDescripcion(q, pageable);
    }

    @Transactional
    public Reservacion crear(Long uid, Long productoId, String fechaStr, Integer duracionMinutos) {
        if (uid == null || productoId == null || fechaStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        LocalDateTime fecha = LocalDateTime.parse(fechaStr);
        if (duracionMinutos == null) duracionMinutos = 60;
        int maxDur = 60;
        List<SuscripcionUsuario> activas = suscripcionUsuarioRepository.findByUsuario_IdAndEstado(uid, SuscripcionUsuario.Estado.ACTIVA);
        for (SuscripcionUsuario su : activas) {
            PlanSuscripcion ps = su.getPlanSuscripcion();
            if (ps != null && ps.getTipo() == PlanSuscripcion.Tipo.HORAS && ps.getHorasMaxReserva() != null) {
                maxDur = Math.max(maxDur, ps.getHorasMaxReserva() * 60);
            }
        }
        boolean durValida = (duracionMinutos >= 60) && (duracionMinutos <= maxDur) && (duracionMinutos % 60 == 0);
        if (!durValida) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        LocalDateTime now = LocalDateTime.now();
        if (fecha.isBefore(now)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reservar en el pasado");
        LocalDateTime fin = fecha.plusMinutes(duracionMinutos);
        if (fecha.getHour() < 6 || (fin.getHour() > 22 || (fin.getHour() == 22 && (fin.getMinute() > 0 || fin.getSecond() > 0)))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horario permitido 06:00–22:00");
        }
        List<Reservacion> futuras = reservacionRepository.findByProducto_IdAndFechaAfter(productoId, fecha.minusHours(6));
        for (Reservacion r : futuras) {
            LocalDateTime rInicio = r.getFecha();
            Integer d = r.getDuracionMinutos() == null ? 60 : r.getDuracionMinutos();
            LocalDateTime rFin = rInicio.plusMinutes(d);
            boolean overlap = !fin.isBefore(rInicio) && !fecha.isAfter(rFin);
            if (overlap) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        }
        Reservacion res = new Reservacion();
        Usuario u = new Usuario(); u.setId(uid);
        res.setUsuario(u);
        Producto p = new Producto(); p.setId(productoId);
        res.setProducto(p);
        res.setFecha(fecha);
        res.setDuracionMinutos(duracionMinutos);
        return reservacionRepository.save(res);
    }

    public List<Reservacion> mias(Long uid, int days) {
        int d = days <= 0 ? 30 : Math.min(days, 365);
        return reservacionRepository.findByUsuario_IdAndFechaAfter(uid, LocalDateTime.now().minusDays(d));
    }

    public Page<Reservacion> adminList(int page, int size, String estado) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200));
        if (estado != null && !estado.isBlank()) {
            try {
                Reservacion.EstadoReservacion st = Reservacion.EstadoReservacion.valueOf(estado.toUpperCase());
                return reservacionRepository.findByEstadoOrderByFechaDesc(st, pageable);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
            }
        }
        return reservacionRepository.findAllByOrderByFechaDesc(pageable);
    }

    @Transactional
    public Map<String, Object> cambiarEstado(Long id, String estado) {
        if (estado == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Reservacion r = reservacionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Reservacion.EstadoReservacion st;
        try { st = Reservacion.EstadoReservacion.valueOf(estado.toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido"); }
        r.setEstado(st);
        reservacionRepository.save(r);
        enviarCorreoCambioEstado(r);
        return Map.of("ok", true, "id", r.getId(), "estado", r.getEstado());
    }

    @Transactional
    public Map<String, Object> eliminar(Long id, Long uid) {
        Reservacion r = reservacionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (r.getUsuario() == null || r.getUsuario().getId() == null || !r.getUsuario().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (r.getEstado() != Reservacion.EstadoReservacion.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes eliminar reservaciones CANCELADAS");
        }
        reservacionRepository.deleteById(id);
        return Map.of("ok", true, "id", id);
    }

    public List<Reservacion> semana(Long uid, String desde, Long productoId) {
        LocalDateTime inicio = LocalDateTime.parse(desde).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusDays(7);
        if (productoId != null) {
            return reservacionRepository.findByProducto_IdAndFechaBetween(productoId, inicio, fin);
        }
        return reservacionRepository.findByUsuario_IdAndFechaBetweenOrderByFechaAsc(uid, inicio, fin);
    }

    @Transactional
    public Map<String, Object> cancelar(Long id, Long uid) {
        Reservacion r = reservacionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (r.getUsuario() == null || r.getUsuario().getId() == null || !r.getUsuario().getId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (r.getEstado() == Reservacion.EstadoReservacion.PENDIENTE) {
            if (ahora.isAfter(r.getFecha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reservación ya inició");
            }
        } else {
            if (ahora.isAfter(r.getFecha().minusHours(2))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede cancelar hasta 2 horas antes");
            }
        }
        r.setEstado(Reservacion.EstadoReservacion.CANCELADA);
        reservacionRepository.save(r);
        enviarCorreoCambioEstado(r);
        return Map.of("ok", true, "id", r.getId(), "estado", r.getEstado());
    }

    private void enviarCorreoCambioEstado(Reservacion r) {
        try {
            Usuario u = r.getUsuario();
            if (u == null || u.getId() == null) {
                return;
            }
            // Asegurarse de tener el email fresco desde BD
            u = usuarioRepository.findById(u.getId()).orElse(u);
            String email = u.getEmail();
            if (email == null || email.isBlank()) {
                return;
            }

            String titulo;
            if (r.getProducto() != null && r.getProducto().getNombre() != null) {
                titulo = r.getProducto().getNombre();
            } else if (r.getMembresia() != null && r.getMembresia().getNombre() != null) {
                titulo = r.getMembresia().getNombre();
            } else {
                titulo = "reserva en el gimnasio";
            }

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaHora = r.getFecha() != null ? r.getFecha().format(fmt) : "";

            String asunto;
            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family:Arial,sans-serif;color:#111827;line-height:1.5;'>");

            if (r.getEstado() == Reservacion.EstadoReservacion.CONFIRMADA) {
                asunto = "Reservación confirmada";
                html.append("<h2 style='margin:0 0 8px;font-size:20px;'>Tu reservación fue confirmada</h2>");
                html.append("<p style='margin:0 0 8px;font-size:14px;'>Hola,</p>");
                html.append("<p style='margin:0 0 8px;font-size:14px;'>Tu reserva para <strong>")
                    .append(titulo)
                    .append("</strong> ha sido <strong>confirmada</strong>.</p>");
                if (!fechaHora.isEmpty()) {
                    html.append("<p style='margin:0 0 8px;font-size:14px;'>Fecha y hora: <strong>")
                        .append(fechaHora)
                        .append("</strong></p>");
                }
            } else if (r.getEstado() == Reservacion.EstadoReservacion.CANCELADA) {
                asunto = "Reservación cancelada";
                html.append("<h2 style='margin:0 0 8px;font-size:20px;'>Tu reservación fue cancelada</h2>");
                html.append("<p style='margin:0 0 8px;font-size:14px;'>Hola,</p>");
                html.append("<p style='margin:0 0 8px;font-size:14px;'>Tu reserva para <strong>")
                    .append(titulo)
                    .append("</strong> ha sido <strong>cancelada</strong>.</p>");
                if (!fechaHora.isEmpty()) {
                    html.append("<p style='margin:0 0 8px;font-size:14px;'>Fecha y hora original: <strong>")
                        .append(fechaHora)
                        .append("</strong></p>");
                }
            } else {
                return; // solo enviamos para CONFIRMADA o CANCELADA
            }

            html.append("<p style='margin-top:8px;font-size:12px;color:#6b7280;'>Si no reconoces esta acción, contacta con Angels Gym.</p>");
            html.append("</div>");

            mailService.sendHtml(email, asunto, html.toString());
        } catch (Exception ex) {
            System.err.println("[ReservacionService] Error enviando correo por cambio de estado: " + ex.getMessage());
        }
    }
}
