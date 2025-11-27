package com.example.backendgym.service;

import com.example.backendgym.domain.Membresia;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.MembresiaRepository;
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
public class MembresiaService {
    private final MembresiaRepository membresiaRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;

    public MembresiaService(MembresiaRepository membresiaRepository,
                            SuscripcionUsuarioRepository suscripcionUsuarioRepository) {
        this.membresiaRepository = membresiaRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
    }

    public List<Membresia> listar(){ return membresiaRepository.findAll(); }

    public Membresia obtener(Long id){ return membresiaRepository.findById(id).orElse(null); }

    public Membresia crear(Membresia m){ return membresiaRepository.save(m); }

    public Membresia actualizar(Long id, Membresia m){ m.setId(id); return membresiaRepository.save(m); }

    public void eliminar(Long id){ membresiaRepository.deleteById(id); }

    public List<Map<String,Object>> suscriptores(Long id){
        if (!membresiaRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<SuscripcionUsuario> list = suscripcionUsuarioRepository.findByMembresia_Id(id);
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

    public SuscripcionUsuario adquirir(Long id, Long usuarioId, BigDecimal monto){
        Membresia m = membresiaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario u = new Usuario();
        u.setId(usuarioId);
        SuscripcionUsuario su = new SuscripcionUsuario();
        su.setUsuario(u);
        su.setMembresia(m);
        su.setFechaInicio(LocalDateTime.now());
        su.setFechaFin(LocalDateTime.now().plusDays(m.getDuracionDias() != null ? m.getDuracionDias() : 30));
        su.setEstado(SuscripcionUsuario.Estado.PENDIENTE_PAGO);
        su.setMetodoPago(null);
        su.setMonto(monto != null ? monto : m.getPrecio());
        return suscripcionUsuarioRepository.save(su);
    }
}
