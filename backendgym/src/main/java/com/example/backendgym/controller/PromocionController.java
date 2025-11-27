package com.example.backendgym.controller;

import com.example.backendgym.domain.Promocion;
import com.example.backendgym.repository.PromocionRepository;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.service.PromocionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final PromocionService promocionService;

    public PromocionController(PromocionRepository promocionRepository,
                               ProductoRepository productoRepository,
                               PromocionService promocionService) {
        this.promocionRepository = promocionRepository;
        this.productoRepository = productoRepository;
        this.promocionService = promocionService;
    }

    @GetMapping
    public Page<Promocion> buscar(@RequestParam(required = false) String q,
                                  @RequestParam(required = false) Promocion.Tipo tipo,
                                  @RequestParam(required = false) String evento,
                                  @RequestParam(defaultValue = "false") boolean soloActivas,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "12") int size) {
        return promocionService.buscar(q, tipo, evento, soloActivas, page, size);
    }

    @GetMapping("/por-categoria")
    public Page<Promocion> buscarPorCategoria(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) Promocion.Tipo tipo,
                                              @RequestParam(required = false) String categoria,
                                              @RequestParam(required = false) String evento,
                                              @RequestParam(defaultValue = "false") boolean soloActivas,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "12") int size) {
        return promocionService.buscarPorCategoria(q, tipo, categoria, evento, soloActivas, page, size);
    }

    @GetMapping("/flash")
    public Page<Promocion> ofertasFlash(@RequestParam(defaultValue = "1440") long minutos,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "12") int size) {
        return promocionService.ofertasFlash(minutos, page, size);
    }

    @GetMapping("/categorias-producto")
    public List<String> categoriasProducto() {
        return promocionService.categoriasProducto();
    }

    // --- CRUD ADMIN ---
    @GetMapping("/{id}")
    public Promocion obtener(@PathVariable Long id) { return promocionService.obtener(id); }

    @PostMapping
    public Promocion crear(@RequestBody PromocionService.PromocionRequest req) { return promocionService.crear(req); }

    @PutMapping("/{id}")
    public Promocion actualizar(@PathVariable Long id, @RequestBody PromocionService.PromocionRequest req) { return promocionService.actualizar(id, req); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { promocionService.eliminar(id); }

    // PromocionRequest se mueve al Service
}
