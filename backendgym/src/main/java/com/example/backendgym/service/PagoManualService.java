package com.example.backendgym.service;

import com.example.backendgym.domain.Pedido;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PagoManualService {

    private final PagoService pagoService;

    public PagoManualService(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    public Map<String, Object> iniciarPedidoDesdeCarrito(Long usuarioId, Pedido.MetodoPago metodoPago) {
        return pagoService.iniciarPedidoDesdeCarrito(usuarioId, metodoPago);
    }

    public Map<String, Object> confirmarPedidoUsuario(Long pedidoId, String referenciaPago) {
        return pagoService.confirmarPedidoUsuario(pedidoId, referenciaPago);
    }
}
