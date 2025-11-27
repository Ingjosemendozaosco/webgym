package com.example.backendgym.service;

import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public Usuario actualizarParcial(Long id, Usuario payload) {
        Usuario u = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            if (payload.getTelefono() != null) {
                String t = payload.getTelefono() != null ? payload.getTelefono().trim() : null;
                if (t != null && !t.isEmpty()) {
                    if (!t.matches("\\d{9}")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TELEFONO_INVALIDO");
                    }
                    u.setTelefono(t);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (payload.getDireccion() != null) u.setDireccion(payload.getDireccion());
        } catch (Exception ignored) {}
        return repo.save(u);
    }
}
