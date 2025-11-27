package com.example.backendgym.controller;

import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/usuarios")
@Transactional
public class UsuarioController {

    private final UsuarioRepository repo;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioRepository repo, UsuarioService usuarioService) {
        this.repo = repo;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return repo.findAll();
    }

    @GetMapping("/me")
    public Usuario me(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String email = auth.getName();
        return repo.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario u) {
        return repo.save(u);
    }

    @PatchMapping("/{id}")
    public Usuario actualizarParcial(@PathVariable Long id, @RequestBody Usuario payload) {
        return usuarioService.actualizarParcial(id, payload);
    }
}
