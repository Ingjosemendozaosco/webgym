package com.example.backendgym.service;

import com.example.backendgym.domain.PlanSuscripcion;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.PlanSuscripcionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanSuscripcionService {
    private final PlanSuscripcionRepository planRepo;
    private final SuscripcionUsuarioRepository suRepo;

    public PlanSuscripcionService(PlanSuscripcionRepository planRepo, SuscripcionUsuarioRepository suRepo) {
        this.planRepo = planRepo;
        this.suRepo = suRepo;
    }

    public List<PlanSuscripcion> listar(){ return planRepo.findAll(); }

    public PlanSuscripcion obtener(Long id){ return planRepo.findById(id).orElse(null); }

    public PlanSuscripcion crear(PlanSuscripcion p){ return planRepo.save(p); }

    public PlanSuscripcion actualizar(Long id, PlanSuscripcion p){ p.setId(id); return planRepo.save(p); }

    public void eliminar(Long id){ planRepo.deleteById(id); }

    public List<Map<String,Object>> suscriptores(Long id){
        List<SuscripcionUsuario> list = suRepo.findByPlanSuscripcion_Id(id);
        return list.stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("usuarioId", s.getUsuario() != null ? s.getUsuario().getId() : null);
            m.put("usuarioEmail", s.getUsuario() != null ? s.getUsuario().getEmail() : null);
            m.put("estado", s.getEstado());
            m.put("fechaInicio", s.getFechaInicio());
            m.put("fechaFin", s.getFechaFin());
            m.put("metodoPago", s.getMetodoPago());
            m.put("monto", s.getMonto());
            return m;
        }).collect(Collectors.toList());
    }

    public SuscripcionUsuario suscribirse(Long id, Long usuarioId, BigDecimal monto){
        PlanSuscripcion plan = planRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario u = new Usuario();
        u.setId(usuarioId);
        SuscripcionUsuario su = new SuscripcionUsuario();
        su.setUsuario(u);
        su.setPlanSuscripcion(plan);
        su.setFechaInicio(LocalDateTime.now());
        Integer dias = plan.getDuracionDias() != null ? plan.getDuracionDias() : 30;
        su.setFechaFin(LocalDateTime.now().plusDays(dias));
        su.setEstado(SuscripcionUsuario.Estado.PENDIENTE_PAGO);
        su.setMetodoPago(null);
        su.setMonto(monto);
        return suRepo.save(su);
    }
}
