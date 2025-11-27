package com.example.backendgym.service;

import com.example.backendgym.domain.Producto;
import com.example.backendgym.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repo;
    public ProductoService(ProductoRepository repo) { this.repo = repo; }

    public List<Producto> listar() { return repo.findAll(); }

    public Producto obtener(Long id) { return repo.findById(id).orElse(null); }

    public Producto crear(Producto p) { return repo.save(p); }

    public Producto actualizar(Long id, Producto p) { p.setId(id); return repo.save(p); }

    public void eliminar(Long id) { repo.deleteById(id); }

    public List<String> categorias() { return repo.findDistinctCategorias(); }

    public List<Producto> destacados() { return repo.findTop5ByDisponibleTrueOrderByPopularidadDesc(); }

    public Page<Producto> buscar(String q, String categoria, boolean soloConStock, boolean mostrarTodos, Boolean disponible, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (disponible != null) {
            boolean disp = disponible.booleanValue();
            if (categoria != null && !categoria.isBlank()) {
                if (soloConStock) {
                    return repo.searchByDisponibleConStockAndCategoriaAndNombreOrDescripcion(disp, categoria, q, pageable);
                }
                return repo.searchByDisponibleAndCategoriaAndNombreOrDescripcion(disp, categoria, q, pageable);
            }
            if (soloConStock) {
                return repo.searchByDisponibleConStockAndNombreOrDescripcion(disp, q, pageable);
            }
            return repo.searchByDisponibleAndNombreOrDescripcion(disp, q, pageable);
        }
        if (categoria != null && !categoria.isBlank()) {
            if (soloConStock) {
                return mostrarTodos
                        ? repo.searchAllConStockByCategoriaAndNombreOrDescripcion(categoria, q, pageable)
                        : repo.searchDisponibleConStockByCategoriaAndNombreOrDescripcion(categoria, q, pageable);
            }
            return mostrarTodos
                    ? repo.searchAllByCategoriaAndNombreOrDescripcion(categoria, q, pageable)
                    : repo.searchDisponibleByCategoriaAndNombreOrDescripcion(categoria, q, pageable);
        }
        if (soloConStock) {
            return mostrarTodos
                    ? repo.searchAllConStockByNombreOrDescripcion(q, pageable)
                    : repo.searchDisponibleConStockByNombreOrDescripcion(q, pageable);
        }
        return mostrarTodos
                ? repo.searchAllByNombreOrDescripcion(q, pageable)
                : repo.searchDisponibleByNombreOrDescripcion(q, pageable);
    }

    public Producto incrementarPopularidad(Long id) {
        Producto p = repo.findById(id).orElse(null);
        if (p == null) return null;
        Integer pop = p.getPopularidad() == null ? 0 : p.getPopularidad();
        p.setPopularidad(pop + 1);
        return repo.save(p);
    }
}
