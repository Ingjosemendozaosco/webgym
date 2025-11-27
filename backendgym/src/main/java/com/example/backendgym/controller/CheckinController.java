package com.example.backendgym.controller;

import com.example.backendgym.service.CheckinService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/{qrToken}")
    public Map<String, Object> registrar(@PathVariable String qrToken) {
        return checkinService.registrarPorToken(qrToken);
    }
}
