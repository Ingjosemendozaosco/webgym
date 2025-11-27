package com.example.backendgym.controller;

import com.example.backendgym.service.RecordatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recordatorios")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    public RecordatorioController(RecordatorioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    @PostMapping("/{usuarioId}")
    public ResponseEntity<Map<String, Object>> enviarRecordatorios(@PathVariable Long usuarioId) {
        int total = recordatorioService.enviarRecordatoriosUsuario(usuarioId);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "usuarioId", usuarioId,
                "totalRecordatorios", total
        ));
    }
}
