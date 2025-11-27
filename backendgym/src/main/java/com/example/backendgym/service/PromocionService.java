package com.example.backendgym.service;

import com.example.backendgym.domain.Producto;
import com.example.backendgym.domain.Promocion;
import com.example.backendgym.repository.PromocionRepository;
import com.example.backendgym.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PromocionService {
    private final PromocionRepository repo;
    private final ProductoRepository productoRepository;

    public PromocionService(PromocionRepository repo, ProductoRepository productoRepository) {
        this.repo = repo;
        this.productoRepository = productoRepository;
    }

    public List<Promocion> activasAhora() {
        return repo.findActivas(LocalDateTime.now());
    }

    public List<Promocion> activasParaProducto(Producto producto) {
        if (producto == null) return Collections.emptyList();
        String categoria = producto.getCategoria();
        return activasAhora().stream()
                .filter(p -> {
                    if (p.getTipo() == Promocion.Tipo.GENERAL) {
                        // Promos generales aplican a todos los productos
                        return true;
                    }
                    if (p.getTipo() == Promocion.Tipo.PRODUCTO) {
                        boolean mismaCategoria = p.getCategoria() != null && Objects.equals(p.getCategoria(), categoria);
                        boolean explicit = p.getProductos() != null &&
                                p.getProductos().stream().anyMatch(pr -> Objects.equals(pr.getId(), producto.getId()));
                        // Para PRODUCTO, solo aplica si coincide categoría o está en la lista explícita
                        return mismaCategoria || explicit;
                    }
                    // Otros tipos (por ejemplo RESERVA) no aplican a productos
                    return false;
                })
                .collect(Collectors.toList());
    }

    public int mejorDescuentoParaProducto(Producto producto) {
        return activasParaProducto(producto).stream()
                .map(p -> p.getDescuentoPorcentaje() == null ? 0 : p.getDescuentoPorcentaje())
                .max(Integer::compareTo)
                .orElse(0);
    }

    // Búsquedas y utilitarios
    public Page<Promocion> buscar(String q, Promocion.Tipo tipo, String evento, boolean soloActivas, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.search(q, tipo, evento, soloActivas, LocalDateTime.now(), pageable);
    }

    public Page<Promocion> buscarPorCategoria(String q, Promocion.Tipo tipo, String categoria, String evento, boolean soloActivas, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.searchByCategoria(q, tipo, categoria, evento, soloActivas, LocalDateTime.now(), pageable);
    }

    public Page<Promocion> ofertasFlash(long minutos, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findFlash(LocalDateTime.now(), minutos, pageable);
    }

    public java.util.List<String> categoriasProducto() {
        return productoRepository.findDistinctCategorias();
    }

    // CRUD
    public Promocion obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Promocion crear(PromocionRequest req) {
        Promocion p = mapear(new Promocion(), req);
        return repo.save(p);
    }

    public Promocion actualizar(Long id, PromocionRequest req) {
        Promocion p = obtener(id);
        p = mapear(p, req);
        p.setId(id);
        return repo.save(p);
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repo.deleteById(id);
    }

    public Promocion mapear(Promocion p, PromocionRequest req) {
        p.setTitulo(req.titulo);
        p.setDescripcion(req.descripcion);
        p.setTipo(req.tipo != null ? req.tipo : Promocion.Tipo.GENERAL);
        Integer d = req.descuentoPorcentaje == null ? 0 : Math.max(0, Math.min(100, req.descuentoPorcentaje));
        p.setDescuentoPorcentaje(d);
        p.setActivo(req.activo != null ? req.activo : Boolean.TRUE);
        p.setFechaInicio(req.fechaInicio != null ? req.fechaInicio : LocalDateTime.now());
        p.setFechaFin(req.fechaFin);
        p.setEvento(req.evento);
        p.setCategoria(req.categoria);
        if (req.productoIds != null) {
            java.util.Set<com.example.backendgym.domain.Producto> set = new java.util.HashSet<>();
            for (Long pid : req.productoIds) {
                productoRepository.findById(pid).ifPresent(set::add);
            }
            p.getProductos().clear();
            p.getProductos().addAll(set);
        }
        return p;
    }

    // DTO interno reutilizable (idéntico al del controller previo)
    public static class PromocionRequest {
        public String titulo;
        public String descripcion;
        public Promocion.Tipo tipo;
        public Integer descuentoPorcentaje;
        public Boolean activo;
        public LocalDateTime fechaInicio;
        public LocalDateTime fechaFin;
        public String evento;
        public String categoria;
        public java.util.List<Long> productoIds;
    }
}
