package com.example.backendgym.controller;

import com.example.backendgym.domain.*;
import com.example.backendgym.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Transactional
@RequestMapping("/api/pagos")
public class PagoController {

    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final com.example.backendgym.service.PagoService pagoService;

    @Value("${mp.access.token:}")
    private String mpAccessToken;
    @Value("${mp.back.success:http://localhost:4200/gracias}")
    private String mpBackSuccess;
    @Value("${mp.back.failure:http://localhost:4200/pagos}")
    private String mpBackFailure;
    @Value("${mp.back.pending:http://localhost:4200/pagos}")
    private String mpBackPending;
    @Value("${mp.notification.url:http://localhost:8080/api/pagos/mercadopago/webhook}")
    private String mpNotificationUrl;

    public PagoController(CarritoRepository carritoRepository,
                          PedidoRepository pedidoRepository,
                          PagoRepository pagoRepository,
                          SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository,
                          com.example.backendgym.service.PagoService pagoService) {
        this.carritoRepository = carritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pagoService = pagoService;
    }

    /**
     * Endpoint de retorno para back_urls de Mercado Pago (HTTPS vía ngrok).
     * Recibe los parámetros que envía MP (status/payment_id/etc) y redirige al frontend en localhost.
     * Esto evita el requisito de HTTPS en el frontend para auto_return.
     */
    @GetMapping("/mercadopago/return")
    public ResponseEntity<Void> retornoMp(@RequestParam Map<String, String> params) {
        String destino = pagoService.buildReturnUrl(params);
        return ResponseEntity.status(HttpStatus.FOUND) // 302
                .header("Location", destino)
                .build();
    }

    @DeleteMapping({"/trabajador/pedidos/{id}", "/admin/pedidos/{id}"})
    public Map<String, Object> eliminarPedido(@PathVariable Long id) {
        return pagoService.eliminarPedido(id);
    }

    @GetMapping({"/trabajador/pagos","/admin/pagos"})
    public List<Map<String, Object>> listarPagos(@RequestParam(required = false) String estado) {
        return pagoService.listarPagos(estado);
    }

    @GetMapping({"/trabajador/suscripciones","/admin/suscripciones"})
    public List<Map<String, Object>> listarSuscripciones(@RequestParam(required = false) String estado,
                                                         @RequestParam(required = false) String tipo) {
        return pagoService.listarSuscripciones(estado, tipo);
    }

    @PostMapping({"/trabajador/suscripciones/{id}/confirmar","/admin/suscripciones/{id}/confirmar"})
    public Map<String, Object> confirmarSuscripcion(@PathVariable Long id) {
        SuscripcionUsuario su = suscripcionUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        su.setEstado(SuscripcionUsuario.Estado.ACTIVA);
        su.setFechaAprobacion(LocalDateTime.now());
        suscripcionUsuarioRepository.save(su);
        Map<String, Object> res = new HashMap<>();
        res.put("id", su.getId());
        res.put("estado", su.getEstado());
        return res;
    }

    @GetMapping({"/trabajador/pedidos/{pedidoId}/comprobante-pdf","/admin/pedidos/{pedidoId}/comprobante-pdf"})
    public ResponseEntity<byte[]> descargarComprobantePdf(@PathVariable Long pedidoId) {
        byte[] pdf = pagoService.generarPdfComprobantePedido(pedidoId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "comprobante-pedido-" + pedidoId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping({"/trabajador/suscripciones/{id}/rechazar","/admin/suscripciones/{id}/rechazar"})
    public Map<String, Object> rechazarSuscripcion(@PathVariable Long id,
                                                   @RequestParam(required = false) String motivo) {
        SuscripcionUsuario su = suscripcionUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        su.setEstado(SuscripcionUsuario.Estado.RECHAZADA);
        if (motivo != null) su.setMotivoRechazo(motivo);
        suscripcionUsuarioRepository.save(su);
        Map<String, Object> res = new HashMap<>();
        res.put("id", su.getId());
        res.put("estado", su.getEstado());
        return res;
    }

    @PostMapping({"/trabajador/suscripciones/{id}/cancelar","/admin/suscripciones/{id}/cancelar"})
    public Map<String, Object> cancelarSuscripcion(@PathVariable Long id,
                                                   @RequestParam(required = false) String motivo) {
        SuscripcionUsuario su = suscripcionUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        su.setEstado(SuscripcionUsuario.Estado.CANCELADA);
        if (motivo != null) su.setMotivoRechazo(motivo);
        suscripcionUsuarioRepository.save(su);
        Map<String, Object> res = new HashMap<>();
        res.put("id", su.getId());
        res.put("estado", su.getEstado());
        return res;
    }

    @DeleteMapping({"/trabajador/suscripciones/{id}","/admin/suscripciones/{id}"})
    public Map<String, Object> eliminarSuscripcion(@PathVariable Long id) {
        SuscripcionUsuario su = suscripcionUsuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (su.getEstado() != SuscripcionUsuario.Estado.CANCELADA && su.getEstado() != SuscripcionUsuario.Estado.RECHAZADA && su.getEstado() != SuscripcionUsuario.Estado.EXPIRADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden eliminar suscripciones CANCELADAS, RECHAZADAS o EXPIRADAS");
        }
        suscripcionUsuarioRepository.delete(su);
        Map<String, Object> res = new HashMap<>();
        res.put("deleted", true);
        res.put("id", id);
        return res;
    }

    @PostMapping({"/admin/pedidos/{pedidoId}/confirmar","/trabajador/pedidos/{pedidoId}/confirmar"})
    public Map<String, Object> confirmarPedidoAdmin(@PathVariable Long pedidoId,
                                                    @RequestParam(required = false) String referenciaPago) {
        return pagoService.confirmarPedidoAdmin(pedidoId, referenciaPago);
    }

    private Long resolveUsuarioId(Long usuarioIdParam, Authentication auth) {
        if (usuarioIdParam != null) return usuarioIdParam;
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/mercadopago/preferencia")
    public ResponseEntity<Map<String, Object>> crearPreferencia(@RequestParam(required = false) Long usuarioId,
                                                                Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return pagoService.crearPreferencia(uid);
    }

    @PostMapping("/mercadopago/preferencia-suscripcion")
    public ResponseEntity<Map<String, Object>> crearPreferenciaSuscripcion(@RequestParam Long suscripcionId) {
        return pagoService.crearPreferenciaSuscripcion(suscripcionId);
    }

    @GetMapping({"/admin/pedidos","/trabajador/pedidos"})
    public List<Map<String, Object>> listarPedidosAdmin(@RequestParam(required = false) String estado,
                                                        @RequestParam(required = false) String estadoPago) {
        return pagoService.listarPedidosAdmin(estado, estadoPago);
    }

    @PostMapping({"/admin/pedidos/{pedidoId}/cancelar","/trabajador/pedidos/{pedidoId}/cancelar"})
    public Map<String, Object> cancelarPedido(@PathVariable Long pedidoId) {
        return pagoService.cancelarPedido(pedidoId);
    }

    @PostMapping("/mercadopago/preferencia-directa")
    public ResponseEntity<Map<String, Object>> crearPreferenciaDirecta(@RequestParam String titulo,
                                                                       @RequestParam BigDecimal monto,
                                                                       @RequestParam(required = false) Long usuarioId,
                                                                       Authentication auth) {
        Long uid = resolveUsuarioId(usuarioId, auth);
        return pagoService.crearPreferenciaDirecta(titulo, monto, uid);
    }

    @PostMapping("/mercadopago/webhook")
    public Map<String, Object> webhook(@RequestBody(required = false) Map<String, Object> payload,
                                       @RequestParam(required = false) Map<String, String> params) {
        return pagoService.webhook(payload, params);
    }

    // descuento calculado en PagoService
}
