package com.example.backendgym.controller;

import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.Reservacion;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.domain.PlanSuscripcion;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.repository.ReservacionRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.service.ReservacionService;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/reservas")
public class ReservacionController {

    private final ProductoRepository productoRepository;
    private final ReservacionRepository reservacionRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final ReservacionService reservacionService;
    private final UsuarioRepository usuarioRepository;

    public ReservacionController(ProductoRepository productoRepository,
                                 ReservacionRepository reservacionRepository,
                                 SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                                 UsuarioRepository usuarioRepository,
                                 ReservacionService reservacionService) {
        this.productoRepository = productoRepository;
        this.reservacionRepository = reservacionRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservacionService = reservacionService;
    }

    @GetMapping("/filtros/uso")
    public List<String> filtrosUso() { return reservacionService.filtrosUso(); }

    @GetMapping("/productos")
    public Page<Producto> productosReservables(@RequestParam(required = false, defaultValue = "") String q,
                                               @RequestParam(required = false) String categoria,
                                               @RequestParam(required = false, defaultValue = "true") boolean soloConStock,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "12") int size) {
        return reservacionService.productosReservables(q, categoria, soloConStock, page, size);
    }

    @PostMapping
    public Reservacion crear(@RequestBody Map<String, Object> body, Authentication auth) {
        Long usuarioId = body.get("usuarioId") == null ? null : Long.valueOf(body.get("usuarioId").toString());
        Long productoId = body.get("productoId") == null ? null : Long.valueOf(body.get("productoId").toString());
        String fechaStr = body.get("fecha") == null ? null : body.get("fecha").toString();
        Integer duracionMinutos = body.get("duracionMinutos") == null ? null : Integer.valueOf(body.get("duracionMinutos").toString());
        Long uid = resolveUsuarioId(usuarioId, auth);
        return reservacionService.crear(uid, productoId, fechaStr, duracionMinutos);
    }

    @GetMapping("/mias")
    public List<Reservacion> mias(@RequestParam(required = false, defaultValue = "30") int days, Authentication auth) {
        Long uid = resolveUsuarioId(null, auth);
        return reservacionService.mias(uid, days);
    }

    @GetMapping("/admin")
    public Page<Reservacion> adminList(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) String estado) {
        return reservacionService.adminList(page, size, estado);
    }

    @PatchMapping("/{id}/estado")
    public Map<String, Object> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String estado = body == null ? null : (body.get("estado") == null ? null : body.get("estado").toString());
        return reservacionService.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> eliminar(@PathVariable Long id, Authentication auth) {
        Long uid = resolveUsuarioId(null, auth);
        return reservacionService.eliminar(id, uid);
    }

    @PostMapping("/{id}/estado")
    public Map<String, Object> cambiarEstadoPost(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return cambiarEstado(id, body);
    }

    @GetMapping("/semana")
    public List<Reservacion> semana(@RequestParam String desde,
                                    @RequestParam(required = false) Long productoId,
                                    Authentication auth) {
        // Listar reservas en la semana [desde, desde+7d)
        Long uid = resolveUsuarioId(null, auth);
        return reservacionService.semana(uid, desde, productoId);
    }

    @PostMapping("/{id}/cancelar")
    public Map<String, Object> cancelar(@PathVariable Long id, Authentication auth) {
        Long uid = resolveUsuarioId(null, auth);
        return reservacionService.cancelar(id, uid);
    }

    private Long resolveUsuarioId(Long usuarioIdParam, Authentication auth) {
        if (usuarioIdParam != null) return usuarioIdParam;
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
