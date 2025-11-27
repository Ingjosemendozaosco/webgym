package com.example.backendgym.controller;

import com.example.backendgym.domain.Carrito;
import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.Pedido;
import com.example.backendgym.domain.DetallePedido;
import com.example.backendgym.domain.SuscripcionUsuario;
import com.example.backendgym.repository.CarritoRepository;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.repository.SuscripcionUsuarioRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.ProductoPrecioService;
import com.example.backendgym.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/carrito")
@Transactional
public class CarritoController {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final ProductoPrecioService productoPrecioService;
    private final UsuarioRepository usuarioRepository;
    private final CarritoService carritoService;

    public CarritoController(CarritoRepository carritoRepository,
                             ProductoRepository productoRepository,
                             SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                             UsuarioRepository usuarioRepository,
                             ProductoPrecioService productoPrecioService,
                             CarritoService carritoService) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoPrecioService = productoPrecioService;
        this.carritoService = carritoService;
    }

    @GetMapping
    public Map<String, Object> obtener(@RequestParam(required = false) Long usuarioId, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return carritoService.obtener(uid);
    }

    @PostMapping
    public Carrito agregar(@RequestParam(required = false) Long usuarioId,
                           @RequestParam Long productoId,
                           @RequestParam(defaultValue = "1") Integer cantidad,
                           Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return carritoService.agregar(uid, productoId, cantidad);
    }

    @PatchMapping("/{itemId}")
    public Carrito actualizarCantidad(@PathVariable Long itemId, @RequestParam int cantidad) {
        return carritoService.actualizarCantidad(itemId, cantidad);
    }

    @DeleteMapping("/{itemId}")
    public void eliminar(@PathVariable Long itemId) {
        carritoService.eliminar(itemId);
    }

    @DeleteMapping
    public void vaciar(@RequestParam(required = false) Long usuarioId, Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        carritoService.vaciar(uid);
    }

    // descuentos calculados en CarritoService

    private Long resolveUsuarioId(Long usuarioIdParam, Authentication auth) {
        if (usuarioIdParam != null) return usuarioIdParam;
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
