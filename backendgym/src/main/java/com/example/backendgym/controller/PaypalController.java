package com.example.backendgym.controller;

import com.example.backendgym.service.PaypalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PaypalController {

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestParam Long usuarioId) {
        return paypalService.crearOrdenDesdeCarrito(usuarioId);
    }

    @PostMapping("/capture")
    public Map<String, Object> capture(@RequestParam String orderId,
                                       @RequestParam Long pedidoId) {
        return paypalService.capturarOrden(orderId, pedidoId);
    }
}
