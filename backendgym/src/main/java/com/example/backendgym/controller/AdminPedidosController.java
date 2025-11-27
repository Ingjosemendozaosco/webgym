package com.example.backendgym.controller;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.repository.PedidoRepository;
import com.example.backendgym.service.AdminPedidosService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidosController {
    private final PedidoRepository repo;
    private final AdminPedidosService adminPedidosService;
    public AdminPedidosController(PedidoRepository repo, AdminPedidosService adminPedidosService) { this.repo = repo; this.adminPedidosService = adminPedidosService; }

    @GetMapping
    public Page<Pedido> listar(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) List<String> estados) {
        return adminPedidosService.listar(page, size, estados);
    }

    @PatchMapping("/{id}/anular")
    public Pedido anular(@PathVariable Long id) {
        return adminPedidosService.anular(id);
    }
}
