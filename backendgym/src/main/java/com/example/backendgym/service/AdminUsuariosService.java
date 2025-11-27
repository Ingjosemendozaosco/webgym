package com.example.backendgym.service;

import com.example.backendgym.domain.Rol;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AdminUsuariosService {
    private final UsuarioRepository repo;
    public AdminUsuariosService(UsuarioRepository repo){ this.repo = repo; }

    public Page<Usuario> listar(int page, int size, String q, String rol, Boolean activo){
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200));
        Rol rolEnum = null;
        if (rol != null && !rol.isBlank()) {
            try { rolEnum = Rol.valueOf(rol.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            return repo.findByQueryAndFilters(like, rolEnum, activo, pageable);
        }
        return repo.findByFilters(rolEnum, activo, pageable);
    }

    public Usuario cambiarRol(Long id, Map<String, Object> body){
        String val = body.get("rol") == null ? null : body.get("rol").toString();
        if (val == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rol requerido");
        Usuario u = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            Rol r = Rol.valueOf(val.toUpperCase());
            u.setRol(r);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }
        return repo.save(u);
    }

    public Usuario setActivo(Long id, Map<String, Object> body){
        Object v = body.get("activo");
        if (v == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activo requerido");
        Usuario u = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        u.setActivo(Boolean.valueOf(v.toString()));
        return repo.save(u);
    }
}
