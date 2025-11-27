package com.example.backendgym.controller;

import com.example.backendgym.domain.ConsultaSoporte;
import com.example.backendgym.repository.ConsultaSoporteRepository;
import com.example.backendgym.service.ConsultaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas-admin")
public class ConsultasController {

    private final ConsultaSoporteRepository repo;
    private final ConsultaService consultaService;

    public ConsultasController(ConsultaSoporteRepository repo, ConsultaService consultaService) {
        this.repo = repo;
        this.consultaService = consultaService;
    }

    // Admin: listar todas con paginación y filtro por estado
    @GetMapping
    public Page<ConsultaSoporte> adminList(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(required = false) String estado) {
        return consultaService.adminList(page, size, estado);
    }
}
