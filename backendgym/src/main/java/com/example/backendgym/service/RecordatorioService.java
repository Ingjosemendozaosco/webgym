package com.example.backendgym.service;

import com.example.backendgym.domain.Reservacion;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecordatorioService {

    private final MailService mailService;
    private final ReservacionRepository reservacionRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public RecordatorioService(MailService mailService,
                               ReservacionRepository reservacionRepository,
                               SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                               UsuarioRepository usuarioRepository) {
        this.mailService = mailService;
        this.reservacionRepository = reservacionRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public int enviarRecordatoriosUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String email = usuario.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario sin correo configurado");
        }

        if (!usuario.getRecordatoriosActivos()) {
            return 0;
        }

        ZonedDateTime nowPe = ZonedDateTime.now(ZoneId.of("America/Lima"));
        LocalDateTime now = nowPe.toLocalDateTime();
        LocalDate today = now.toLocalDate();
        LocalDateTime windowStart = now;
        LocalDateTime windowEnd = now.plusHours(1);

        List<Reservacion> reservasProximaHora = reservacionRepository
                .findByUsuario_IdAndFechaBetweenOrderByFechaAsc(usuarioId, windowStart, windowEnd);

        // No consideramos reservas canceladas o anuladas para los recordatorios de próxima hora
        reservasProximaHora.removeIf(r -> {
            Reservacion.EstadoReservacion st = r.getEstado();
            return st == Reservacion.EstadoReservacion.CANCELADA
                    || st == Reservacion.EstadoReservacion.ANULADA;
        });

        List<SuscripcionUsuario> activas = suscripcionUsuarioRepository
                .findByUsuario_IdAndEstado(usuarioId, SuscripcionUsuario.Estado.ACTIVA);

        List<SuscripcionUsuario> proximasAVencer = new ArrayList<>();
        for (SuscripcionUsuario s : activas) {
            LocalDateTime fin = s.getFechaFin();
            if (fin == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, fin.toLocalDate());
            if (days >= 0 && days <= 7) {
                proximasAVencer.add(s);
            }
        }

        if (reservasProximaHora.isEmpty() && proximasAVencer.isEmpty()) {
            return 0;
        }

        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String nombre = usuario.getNombre();
        String nombreSafe = (nombre != null && !nombre.isBlank()) ? nombre : "";

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family:Arial,sans-serif;color:#111827;line-height:1.5;'>");
        html.append("<h2 style='margin:0 0 4px;font-size:20px;'>Recordatorios de Angels Gym</h2>");
        html.append("<p style='margin:0 0 12px;font-size:14px;'>Hola ")
            .append(nombreSafe)
            .append(",</p>");
        html.append("<p style='margin:0 0 16px;font-size:14px;'>Te dejamos un resumen rápido de tu plan y tus reservas:</p>");

        if (!proximasAVencer.isEmpty()) {
            html.append("<div style='margin-bottom:16px;'>");
            html.append("<h3 style='margin:0 0 8px;font-size:16px;'>Planes y membresías por vencer</h3>");
            html.append("<ul style='margin:0;padding-left:18px;font-size:14px;'>");
            for (SuscripcionUsuario s : proximasAVencer) {
                LocalDateTime fin = s.getFechaFin();
                long days = ChronoUnit.DAYS.between(today, fin.toLocalDate());
                String nombrePlan;
                if (s.getMembresia() != null && s.getMembresia().getNombre() != null) {
                    nombrePlan = s.getMembresia().getNombre();
                } else if (s.getPlanSuscripcion() != null && s.getPlanSuscripcion().getNombre() != null) {
                    nombrePlan = s.getPlanSuscripcion().getNombre();
                } else {
                    nombrePlan = "tu plan";
                }
                String labelDias = (days == 1) ? "día" : "días";
                html.append("<li style='margin-bottom:4px;'>Tu plan <strong>")
                    .append(nombrePlan)
                    .append("</strong> vence en ")
                    .append(days)
                    .append(' ')
                    .append(labelDias)
                    .append(" (")
                    .append(fin.toLocalDate().format(fechaFormatter))
                    .append(")</li>");
            }
            html.append("</ul>");
            html.append("</div>");
        }

        if (!reservasProximaHora.isEmpty()) {
            html.append("<div style='margin-bottom:16px;'>");
            html.append("<h3 style='margin:0 0 8px;font-size:16px;'>Reservas para hoy</h3>");
            html.append("<ul style='margin:0;padding-left:18px;font-size:14px;'>");
            for (Reservacion r : reservasProximaHora) {
                LocalDateTime fecha = r.getFecha();
                String titulo;
                if (r.getProducto() != null && r.getProducto().getNombre() != null) {
                    titulo = r.getProducto().getNombre();
                } else if (r.getMembresia() != null && r.getMembresia().getNombre() != null) {
                    titulo = r.getMembresia().getNombre();
                } else {
                    titulo = "reserva en el gimnasio";
                }
                html.append("<li style='margin-bottom:4px;'>Tienes una reserva hoy a las <strong>")
                    .append(fecha.toLocalTime().format(horaFormatter))
                    .append("</strong> para <strong>")
                    .append(titulo)
                    .append("</strong></li>");
            }
            html.append("</ul>");
            html.append("</div>");
        }

        html.append("<div style='margin:12px 0 4px;text-align:center;'>"
                + "<a href='#' style='display:inline-block;background:#ef4444;color:#ffffff;"
                + "padding:10px 20px;border-radius:999px;font-size:14px;text-decoration:none;font-weight:600;'>"
                + "Ver mis reservas"
                + "</a>"
                + "</div>");

        html.append("<p style='margin-top:4px;font-size:12px;color:#6b7280;'>Si no reconoces esta información, puedes ignorar este correo.</p>");
        html.append("</div>");

        mailService.sendHtml(email, "Recordatorios de tu plan y reservas", html.toString());

        return proximasAVencer.size() + reservasProximaHora.size();
    }
}
