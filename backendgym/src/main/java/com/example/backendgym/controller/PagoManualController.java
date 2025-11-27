package com.example.backendgym.controller;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.service.PagoManualService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoManualController {

    private final PagoManualService pagoManualService;

    public PagoManualController(PagoManualService pagoManualService) {
        this.pagoManualService = pagoManualService;
    }

    @PostMapping("/iniciar")
    public Map<String, Object> iniciar(@RequestParam Long usuarioId,
                                       @RequestParam Pedido.MetodoPago metodoPago) {
        return pagoManualService.iniciarPedidoDesdeCarrito(usuarioId, metodoPago);
    }

    @PostMapping("/{pedidoId}/confirmar")
    public Map<String, Object> confirmar(@PathVariable Long pedidoId,
                                         @RequestParam(required = false) String referenciaPago) {
        return pagoManualService.confirmarPedidoUsuario(pedidoId, referenciaPago);
    }
}
