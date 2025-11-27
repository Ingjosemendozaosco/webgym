package com.example.backendgym.service;

import com.example.backendgym.domain.Membresia;
import com.example.backendgym.domain.PlanSuscripcion;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.MembresiaRepository;
import com.example.backendgym.repository.PlanSuscripcionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SuscripcionService {

    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final MembresiaRepository membresiaRepository;
    private final PlanSuscripcionRepository planSuscripcionRepository;

    public SuscripcionService(SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                              UsuarioRepository usuarioRepository,
                              MembresiaRepository membresiaRepository,
                              PlanSuscripcionRepository planSuscripcionRepository) {
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.membresiaRepository = membresiaRepository;
        this.planSuscripcionRepository = planSuscripcionRepository;
    }

    private void validarXorIds(Long membresiaId, Long planId) {
        if ((membresiaId == null && planId == null) || (membresiaId != null && planId != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar solo membresiaId O planSuscripcionId");
        }
    }

    @Transactional
    public SuscripcionUsuario crear(Long usuarioId,
                                    Long membresiaId,
                                    Long planId,
                                    BigDecimal monto,
                                    String metodoPago,
                                    String comprobanteUrl) {
        validarXorIds(membresiaId, planId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        SuscripcionUsuario s = new SuscripcionUsuario();
        s.setUsuario(usuario);
        s.setFechaInicio(LocalDateTime.now());

        if (membresiaId != null) {
            Membresia m = membresiaRepository.findById(membresiaId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membresia no encontrada"));
            s.setMembresia(m);
            s.setFechaFin(LocalDateTime.now().plusDays(m.getDuracionDias()));
            if (monto == null) {
                java.math.BigDecimal base = m.getPrecio();
                Integer d = m.getDescuentoPorcentaje();
                if (d != null && d > 0) {
                    java.math.BigDecimal desc = base.multiply(new java.math.BigDecimal(d)).divide(new java.math.BigDecimal(100));
                    monto = base.subtract(desc);
                } else {
                    monto = base;
                }
            }
        } else {
            PlanSuscripcion p = planSuscripcionRepository.findById(planId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
            s.setPlanSuscripcion(p);
            s.setFechaFin(LocalDateTime.now().plusDays(p.getDuracionDias()));
            if (monto == null) {
                java.math.BigDecimal base = p.getPrecio();
                monto = base;
            }
        }

        s.setMetodoPago(metodoPago != null ? metodoPago : "ONLINE");
        s.setMonto(monto);
        s.setComprobanteUrl(comprobanteUrl);
        s.setEstado(SuscripcionUsuario.Estado.PENDIENTE_PAGO);

        return suscripcionUsuarioRepository.save(s);
    }

    @Transactional
    public Map<String, Object> iniciar(Long usuarioId,
                                       Long membresiaId,
                                       Long planId,
                                       BigDecimal monto) {
        validarXorIds(membresiaId, planId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        SuscripcionUsuario s = new SuscripcionUsuario();
        s.setUsuario(usuario);
        s.setFechaInicio(LocalDateTime.now());

        if (membresiaId != null) {
            Membresia m = membresiaRepository.findById(membresiaId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membresia no encontrada"));
            s.setMembresia(m);
            s.setFechaFin(LocalDateTime.now().plusDays(m.getDuracionDias()));
            if (monto == null) {
                java.math.BigDecimal base = m.getPrecio();
                Integer d = m.getDescuentoPorcentaje();
                if (d != null && d > 0) {
                    java.math.BigDecimal desc = base.multiply(new java.math.BigDecimal(d)).divide(new java.math.BigDecimal(100));
                    monto = base.subtract(desc);
                } else {
                    monto = base;
                }
            }
        } else {
            PlanSuscripcion p = planSuscripcionRepository.findById(planId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
            s.setPlanSuscripcion(p);
            s.setFechaFin(LocalDateTime.now().plusDays(p.getDuracionDias()));
            if (monto == null) {
                java.math.BigDecimal base = p.getPrecio();
                Integer d = p.getDescuentoPorcentaje();
                if (d != null && d > 0) {
                    java.math.BigDecimal desc = base.multiply(new java.math.BigDecimal(d)).divide(new java.math.BigDecimal(100));
                    monto = base.subtract(desc);
                } else {
                    monto = base;
                }
            }
        }

        s.setMonto(monto);
        s.setEstado(SuscripcionUsuario.Estado.PENDIENTE_PAGO);

        SuscripcionUsuario saved = suscripcionUsuarioRepository.save(s);
        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("estado", saved.getEstado());
        res.put("monto", saved.getMonto());
        return res;
    }
}
