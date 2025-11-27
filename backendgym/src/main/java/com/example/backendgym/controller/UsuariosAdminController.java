package com.example.backendgym.controller;

import com.example.backendgym.domain.Usuario;
import com.example.backendgym.domain.Rol;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.AdminUsuariosService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuariosAdminController {

    private final UsuarioRepository repo;
    private final AdminUsuariosService adminUsuariosService;

    public UsuariosAdminController(UsuarioRepository repo, AdminUsuariosService adminUsuariosService) { this.repo = repo; this.adminUsuariosService = adminUsuariosService; }

    @GetMapping
    public Page<Usuario> listar(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String q,
                                @RequestParam(required = false) String rol,
                                @RequestParam(required = false) Boolean activo) {
        return adminUsuariosService.listar(page, size, q, rol, activo);
    }

    @PatchMapping("/{id}/rol")
    public Usuario cambiarRol(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return adminUsuariosService.cambiarRol(id, body);
    }

    @PatchMapping("/{id}/activo")
    public Usuario setActivo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return adminUsuariosService.setActivo(id, body);
    }
}
