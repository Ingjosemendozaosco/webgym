package com.example.backendgym.controller;

import com.example.backendgym.domain.Promocion;
import com.example.backendgym.service.PromocionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
public class PromocionesController {

    private final PromocionService promocionService;

    public PromocionesController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping("/activas")
    public List<Promocion> activas() {
        return promocionService.activasAhora();
    }
}
