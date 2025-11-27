package com.example.backendgym.controller;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.repository.PedidoRepository;
import com.example.backendgym.service.TrabajadorPedidosService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pagos/trabajador/pedidos")
public class TrabajadorPedidosController {

    private final PedidoRepository repo;
    private final TrabajadorPedidosService trabajadorPedidosService;

    public TrabajadorPedidosController(PedidoRepository repo, TrabajadorPedidosService trabajadorPedidosService) {
        this.repo = repo;
        this.trabajadorPedidosService = trabajadorPedidosService;
    }

    @PostMapping("/{id}/rechazar")
    public Pedido rechazar(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        return trabajadorPedidosService.rechazar(id, motivo);
    }

    @PostMapping("/{id}/revision")
    public Pedido marcarRevision(@PathVariable Long id, @RequestParam(required = false) String nota) {
        return trabajadorPedidosService.marcarRevision(id, nota);
    }
}
