package com.example.backendgym.service;

import com.example.backendgym.domain.*;
import com.example.backendgym.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final MembresiaRepository membresiaRepository;
    private final PlanSuscripcionRepository planRepository;

    public CalificacionService(CalificacionRepository calificacionRepository,
                               UsuarioRepository usuarioRepository,
                               ProductoRepository productoRepository,
                               MembresiaRepository membresiaRepository,
                               PlanSuscripcionRepository planRepository) {
        this.calificacionRepository = calificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.membresiaRepository = membresiaRepository;
        this.planRepository = planRepository;
    }

    public Calificacion calificarProducto(Long usuarioId, Long productoId, int puntuacion, String comentario) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (puntuacion < 1 || puntuacion > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La puntuación debe estar entre 1 y 5");
        }
        Calificacion cal = calificacionRepository.findByUsuarioAndProducto(u, p).orElseGet(Calificacion::new);
        cal.setUsuario(u);
        cal.setProducto(p);
        cal.setMembresia(null);
        cal.setPlanSuscripcion(null);
        cal.setPuntuacion(puntuacion);
        cal.setComentario(comentario);
        return calificacionRepository.save(cal);
    }

    public List<Calificacion> listarPorProducto(Long productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return calificacionRepository.findByProductoOrderByFechaCreacionDesc(p);
    }

    public Map<String, Object> resumenProducto(Long productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        Double promedio = calificacionRepository.promedioPorProducto(p);
        Long cantidad = calificacionRepository.cantidadPorProducto(p);
        Map<String, Object> m = new HashMap<>();
        m.put("promedio", promedio != null ? promedio : 0.0);
        m.put("cantidad", cantidad != null ? cantidad : 0L);
        return m;
    }

    public Calificacion calificarMembresia(Long usuarioId, Long membresiaId, int puntuacion, String comentario) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Membresia m = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membresía no encontrada"));
        if (puntuacion < 1 || puntuacion > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La puntuación debe estar entre 1 y 5");
        }
        Calificacion cal = calificacionRepository.findByUsuarioAndMembresia(u, m).orElseGet(Calificacion::new);
        cal.setUsuario(u);
        cal.setMembresia(m);
        cal.setProducto(null);
        cal.setPlanSuscripcion(null);
        cal.setPuntuacion(puntuacion);
        cal.setComentario(comentario);
        return calificacionRepository.save(cal);
    }

    public List<Calificacion> listarPorMembresia(Long membresiaId) {
        Membresia m = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membresía no encontrada"));
        return calificacionRepository.findByMembresiaOrderByFechaCreacionDesc(m);
    }

    public Map<String, Object> resumenMembresia(Long membresiaId) {
        Membresia m = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membresía no encontrada"));
        Double promedio = calificacionRepository.promedioPorMembresia(m);
        Long cantidad = calificacionRepository.cantidadPorMembresia(m);
        Map<String, Object> res = new HashMap<>();
        res.put("promedio", promedio != null ? promedio : 0.0);
        res.put("cantidad", cantidad != null ? cantidad : 0L);
        return res;
    }

    public Calificacion calificarPlan(Long usuarioId, Long planId, int puntuacion, String comentario) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        PlanSuscripcion plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        if (puntuacion < 1 || puntuacion > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La puntuación debe estar entre 1 y 5");
        }
        Calificacion cal = calificacionRepository.findByUsuarioAndPlanSuscripcion(u, plan).orElseGet(Calificacion::new);
        cal.setUsuario(u);
        cal.setPlanSuscripcion(plan);
        cal.setProducto(null);
        cal.setMembresia(null);
        cal.setPuntuacion(puntuacion);
        cal.setComentario(comentario);
        return calificacionRepository.save(cal);
    }

    public List<Calificacion> listarPorPlan(Long planId) {
        PlanSuscripcion plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        return calificacionRepository.findByPlanSuscripcionOrderByFechaCreacionDesc(plan);
    }

    public Map<String, Object> resumenPlan(Long planId) {
        PlanSuscripcion plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        Double promedio = calificacionRepository.promedioPorPlan(plan);
        Long cantidad = calificacionRepository.cantidadPorPlan(plan);
        Map<String, Object> res = new HashMap<>();
        res.put("promedio", promedio != null ? promedio : 0.0);
        res.put("cantidad", cantidad != null ? cantidad : 0L);
        return res;
    }
}
