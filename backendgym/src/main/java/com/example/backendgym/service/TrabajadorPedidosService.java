package com.example.backendgym.service;

import com.example.backendgym.domain.Pedido;
import com.example.backendgym.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrabajadorPedidosService {

    private final PedidoRepository repo;

    public TrabajadorPedidosService(PedidoRepository repo) {
        this.repo = repo;
    }

    public Pedido rechazar(Long id, String motivo) {
        Pedido p = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setEstado(Pedido.EstadoPedido.RECHAZADO);
        // Si tienes campo de observaciones/notas, aquí podrías guardarlo usando "motivo"
        return repo.save(p);
    }

    public Pedido marcarRevision(Long id, String nota) {
        Pedido p = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setEstado(Pedido.EstadoPedido.REVISION);
        // Guardar nota si existe un campo, usando "nota"
        return repo.save(p);
    }
}
