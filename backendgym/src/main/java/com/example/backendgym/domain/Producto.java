package com.example.backendgym.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    private String categoria;
    private String imagen;
    private BigDecimal descuento = BigDecimal.ZERO;
    private boolean disponible = true;

    private Integer popularidad; 

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { 
        this.descuento = (descuento != null) ? descuento : BigDecimal.ZERO; 
    }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public Integer getPopularidad() { return popularidad; }
    public void setPopularidad(Integer popularidad) { this.popularidad = popularidad; }

    // Método para obtener el precio con descuento
    public BigDecimal getPrecioConDescuento() {
        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            return precio.subtract(precio.multiply(descuento).divide(new BigDecimal(100)));
        }
        return precio;
    }


    @PrePersist
    @PreUpdate
    private void prePersistUpdate() {
        if (this.descuento == null) {
            this.descuento = BigDecimal.ZERO;
        }
        if (this.descuento.compareTo(BigDecimal.ZERO) < 0) {
            this.descuento = BigDecimal.ZERO;
        }
        if (this.descuento.compareTo(new BigDecimal("100")) > 0) {
            this.descuento = new BigDecimal("100");
        }
        this.descuento = this.descuento.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @PostLoad
    private void postLoad() {
        if (this.descuento == null) {
            this.descuento = BigDecimal.ZERO;
        }
    }
}






