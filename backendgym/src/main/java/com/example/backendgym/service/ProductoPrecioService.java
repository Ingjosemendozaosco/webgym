package com.example.backendgym.service;

import com.example.backendgym.domain.Producto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductoPrecioService {

    private final PromocionService promocionService;

    public ProductoPrecioService(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    public BigDecimal getPrecioConPromocion(Producto producto) {
        if (producto == null || producto.getPrecio() == null) return BigDecimal.ZERO;
        BigDecimal base = producto.getPrecio();
        int promoPct = promocionService.mejorDescuentoParaProducto(producto);
        // considerar el descuento propio del producto si existe (en %) y tomar el mayor
        int prodPct = 0;
        if (producto.getDescuento() != null) {
            try {
                prodPct = producto.getDescuento().setScale(0, RoundingMode.DOWN).intValue();
            } catch (Exception ignored) {}
        }
        int best = Math.max(prodPct, Math.max(0, Math.min(100, promoPct)));
        if (best <= 0) return base;
        return base.subtract(base.multiply(BigDecimal.valueOf(best)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }
}
