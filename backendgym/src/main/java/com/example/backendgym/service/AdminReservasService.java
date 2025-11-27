package com.example.backendgym.service;

import com.example.backendgym.domain.Reservacion;
import com.example.backendgym.repository.ReservacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminReservasService {
    private final ReservacionRepository repo;
    public AdminReservasService(ReservacionRepository repo){ this.repo = repo; }

    public List<Map<String, Object>> semana(String desde, Long productoId, Long membresiaId){
        LocalDateTime inicio = LocalDateTime.parse(desde)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusDays(7);
        List<Reservacion> list;
        if (productoId != null) {
            list = repo.findByProducto_IdAndFechaBetween(productoId, inicio, fin);
        } else if (membresiaId != null) {
            list = repo.findByMembresia_IdAndFechaBetween(membresiaId, inicio, fin);
        } else {
            list = repo.findByFechaBetween(inicio, fin);
        }
        list = list.stream().sorted((a,b) -> a.getFecha().compareTo(b.getFecha())).toList();
        return list.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("fecha", r.getFecha());
            m.put("duracionMinutos", r.getDuracionMinutos());
            m.put("estado", r.getEstado());
            if (r.getUsuario() != null) {
                m.put("usuarioId", r.getUsuario().getId());
                m.put("usuarioNombre", r.getUsuario().getNombre());
                m.put("usuarioEmail", r.getUsuario().getEmail());
            } else {
                m.put("usuarioId", null);
            }
            if (r.getProducto() != null) {
                m.put("productoId", r.getProducto().getId());
                m.put("productoNombre", r.getProducto().getNombre());
            }
            if (r.getMembresia() != null) {
                m.put("membresiaId", r.getMembresia().getId());
                m.put("membresiaNombre", r.getMembresia().getNombre());
            }
            return m;
        }).collect(Collectors.toList());
    }
}
