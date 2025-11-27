package com.example.backendgym.service;

import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.repository.DetallePedidoRepository;
import com.example.backendgym.repository.PedidoRepository;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminEstadisticasService {
    private final PedidoRepository pedidoRepo;
    private final ReservacionRepository reservaRepo;
    private final SuscripcionUsuarioRepository susRepo;
    private final DetallePedidoRepository detalleRepo;

    public AdminEstadisticasService(PedidoRepository pedidoRepo,
                                    ReservacionRepository reservaRepo,
                                    SuscripcionUsuarioRepository susRepo,
                                    DetallePedidoRepository detalleRepo) {
        this.pedidoRepo = pedidoRepo;
        this.reservaRepo = reservaRepo;
        this.susRepo = susRepo;
        this.detalleRepo = detalleRepo;
    }

    public Map<String, Object> overview(String from, String to) {
        LocalDateTime inicio = LocalDate.parse(from).atStartOfDay();
        LocalDateTime fin = LocalDate.parse(to).plusDays(1).atStartOfDay();
        BigDecimal ingresos = pedidoRepo.sumIngresosConfirmadosBetween(inicio, fin);
        long pedidos = pedidoRepo.countByFechaPedidoBetween(inicio, fin);
        long reservas = reservaRepo.countByFechaBetween(inicio, fin);
        long susActivas = susRepo.countByEstado(SuscripcionUsuario.Estado.ACTIVA);
        Map<String, Object> res = new HashMap<>();
        res.put("ingresos", ingresos);
        res.put("pedidos", pedidos);
        res.put("reservas", reservas);
        res.put("suscripcionesActivas", susActivas);
        res.put("moneda", "PEN");
        return res;
    }

    public List<Map<String, Object>> ingresosSeries(String from, String to, String groupBy) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        List<Map<String, Object>> out = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDateTime a = cursor.atStartOfDay();
            LocalDateTime b;
            if ("month".equalsIgnoreCase(groupBy)) {
                b = cursor.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            } else if ("week".equalsIgnoreCase(groupBy)) {
                b = cursor.plusDays(7).atStartOfDay();
            } else {
                b = cursor.plusDays(1).atStartOfDay();
            }
            BigDecimal val = pedidoRepo.sumIngresosConfirmadosBetween(a, b);
            Map<String, Object> point = new HashMap<>();
            point.put("t", cursor.toString());
            point.put("value", val);
            out.add(point);
            cursor = "month".equalsIgnoreCase(groupBy) ? cursor.plusMonths(1) : ("week".equalsIgnoreCase(groupBy) ? cursor.plusWeeks(1) : cursor.plusDays(1));
        }
        return out;
    }

    public List<Map<String, Object>> pedidosSeries(String from, String to, String groupBy) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        List<Map<String, Object>> out = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDateTime a = cursor.atStartOfDay();
            LocalDateTime b = "month".equalsIgnoreCase(groupBy)
                    ? cursor.plusMonths(1).withDayOfMonth(1).atStartOfDay()
                    : ("week".equalsIgnoreCase(groupBy) ? cursor.plusDays(7).atStartOfDay() : cursor.plusDays(1).atStartOfDay());
            long val = pedidoRepo.countByFechaPedidoBetween(a, b);
            Map<String, Object> point = new HashMap<>();
            point.put("t", cursor.toString());
            point.put("value", val);
            out.add(point);
            cursor = "month".equalsIgnoreCase(groupBy) ? cursor.plusMonths(1) : ("week".equalsIgnoreCase(groupBy) ? cursor.plusWeeks(1) : cursor.plusDays(1));
        }
        return out;
    }

    public List<Map<String, Object>> reservasSeries(String from, String to, String groupBy) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        List<Map<String, Object>> out = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDateTime a = cursor.atStartOfDay();
            LocalDateTime b = "month".equalsIgnoreCase(groupBy)
                    ? cursor.plusMonths(1).withDayOfMonth(1).atStartOfDay()
                    : ("week".equalsIgnoreCase(groupBy) ? cursor.plusDays(7).atStartOfDay() : cursor.plusDays(1).atStartOfDay());
            long val = reservaRepo.countByFechaBetween(a, b);
            Map<String, Object> point = new HashMap<>();
            point.put("t", cursor.toString());
            point.put("value", val);
            out.add(point);
            cursor = "month".equalsIgnoreCase(groupBy) ? cursor.plusMonths(1) : ("week".equalsIgnoreCase(groupBy) ? cursor.plusWeeks(1) : cursor.plusDays(1));
        }
        return out;
    }

    public List<Map<String, Object>> ultimosPedidos(int limit) {
        int lim = Math.max(1, Math.min(100, limit));
        var page = pedidoRepo.findAll(org.springframework.data.domain.PageRequest.of(0, lim, org.springframework.data.domain.Sort.by("fechaPedido").descending()));
        List<Map<String, Object>> out = new ArrayList<>();
        page.getContent().forEach(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("usuarioId", p.getUsuario() != null ? p.getUsuario().getId() : null);
            m.put("estado", p.getEstado());
            m.put("fecha", p.getFechaPedido());
            m.put("total", p.getTotal());
            out.add(m);
        });
        return out;
    }

    public Map<String, Object> tops(String from, String to, int limit) {
        int lim = Math.max(1, Math.min(20, limit));
        LocalDateTime inicio = LocalDate.parse(from).atStartOfDay();
        LocalDateTime fin = LocalDate.parse(to).plusDays(1).atStartOfDay();

        List<Object[]> prodIng = detalleRepo.topProductosPorIngresos(inicio, fin);
        List<Object[]> prodUni = detalleRepo.topProductosPorUnidades(inicio, fin);
        List<Object[]> planes = susRepo.topPlanesBetween(inicio, fin);
        List<Object[]> membresias = susRepo.topMembresiasBetween(inicio, fin);

        Map<String, Object> res = new HashMap<>();
        res.put("productosIngresos", prodIng.stream().limit(lim).map(r -> Map.of(
                "id", r[0], "nombre", r[1], "ingresos", r[2], "unidades", r[3]
        )).toList());
        res.put("productosUnidades", prodUni.stream().limit(lim).map(r -> Map.of(
                "id", r[0], "nombre", r[1], "ingresos", r[2], "unidades", r[3]
        )).toList());
        res.put("planes", planes.stream().limit(lim).map(r -> Map.of(
                "id", r[0], "nombre", r[1], "cantidad", r[2]
        )).toList());
        res.put("membresias", membresias.stream().limit(lim).map(r -> Map.of(
                "id", r[0], "nombre", r[1], "cantidad", r[2]
        )).toList());
        return res;
    }
}
