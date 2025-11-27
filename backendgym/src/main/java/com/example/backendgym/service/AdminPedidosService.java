package com.example.backendgym.service;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.repository.PedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminPedidosService {
    private final PedidoRepository repo;
    public AdminPedidosService(PedidoRepository repo){ this.repo = repo; }

    public Page<Pedido> listar(int page, int size, List<String> estados){
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200));
        List<Pedido.EstadoPedido> filtros = (estados == null || estados.isEmpty())
                ? List.of(Pedido.EstadoPedido.RECHAZADO, Pedido.EstadoPedido.REVISION)
                : estados.stream().map(s -> {
                    try { return Pedido.EstadoPedido.valueOf(s.toUpperCase()); }
                    catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + s); }
                }).toList();
        return repo.findByEstadoInOrderByFechaPedidoDesc(filtros, pageable);
    }

    public Pedido anular(Long id){
        Pedido p = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setEstado(Pedido.EstadoPedido.ANULADO);
        return repo.save(p);
    }
}
