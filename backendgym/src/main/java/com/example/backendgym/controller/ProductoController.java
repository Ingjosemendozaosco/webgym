package com.example.backendgym.controller;

import com.example.backendgym.domain.Producto;
import com.example.backendgym.repository.ProductoRepository;
import com.example.backendgym.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repo;
    private final ProductoService productoService;
    public ProductoController(ProductoRepository repo, ProductoService productoService) {
        this.repo = repo;
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar() { return productoService.listar(); }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) { return productoService.obtener(id); }

    @PostMapping
    public Producto crear(@RequestBody Producto p) { return productoService.crear(p); }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto p) { return productoService.actualizar(id, p); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { productoService.eliminar(id); }

    @GetMapping("/categorias")
    public List<String> categorias() { return productoService.categorias(); }

    @GetMapping("/destacados")
    public List<Producto> destacados() { return productoService.destacados(); }

    @GetMapping("/search")
    public Page<Producto> buscar(@RequestParam(required = false, defaultValue = "") String q,
                                 @RequestParam(required = false) String categoria,
                                 @RequestParam(required = false, defaultValue = "false") boolean soloConStock,
                                 @RequestParam(required = false, defaultValue = "false") boolean mostrarTodos,
                                 @RequestParam(required = false) Boolean disponible,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {
        return productoService.buscar(q, categoria, soloConStock, mostrarTodos, disponible, page, size);
    }

    @PostMapping("/{id}/popularidad")
    public Producto incrementarPopularidad(@PathVariable Long id) { return productoService.incrementarPopularidad(id); }
}

