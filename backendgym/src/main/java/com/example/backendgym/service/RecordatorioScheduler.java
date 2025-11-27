package com.example.backendgym.service;

import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecordatorioScheduler {

    private final UsuarioRepository usuarioRepository;
    private final RecordatorioService recordatorioService;

    public RecordatorioScheduler(UsuarioRepository usuarioRepository,
                                 RecordatorioService recordatorioService) {
        this.usuarioRepository = usuarioRepository;
        this.recordatorioService = recordatorioService;
    }

    // Ejecuta cada 5 minutos y revisa reservas en la próxima hora para usuarios con recordatorios activos
    @Scheduled(fixedDelay = 300_000L)
    public void procesarRecordatoriosProximaHora() {
        List<Usuario> usuarios = usuarioRepository.findByRecordatoriosActivosTrue();
        for (Usuario u : usuarios) {
            try {
                if (u.getId() != null) {
                    recordatorioService.enviarRecordatoriosUsuario(u.getId());
                }
            } catch (Exception ex) {
                System.err.println("[RecordatorioScheduler] Error enviando recordatorio a usuario " + u.getId() + ": " + ex.getMessage());
            }
        }
    }
}
