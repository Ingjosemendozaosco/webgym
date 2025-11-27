package com.example.backendgym.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plan_suscripcion")
public class PlanSuscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beneficio;

    @Column(name = "descuento_porcentaje")
    private Integer descuentoPorcentaje;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    public enum Tipo { DESCUENTO, HORAS }

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private Tipo tipo;

    public enum Estado { ACTIVO, INACTIVO, SOLICITADO }

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private Estado estado = Estado.INACTIVO;

    @Column(name = "horas_max_reserva")
    private Integer horasMaxReserva;

    @Column(name = "horas_dia_max")
    private Integer horasDiaMax; // horas máximas por día para reservas

    @Column(name = "horas_semana_max")
    private Integer horasSemanaMax; // horas máximas por semana para reservas

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getBeneficio() { return beneficio; }
    public void setBeneficio(String beneficio) { this.beneficio = beneficio; }

    public Integer getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(Integer descuentoPorcentaje) { this.descuentoPorcentaje = descuentoPorcentaje; }

    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Integer getHorasMaxReserva() { return horasMaxReserva; }
    public void setHorasMaxReserva(Integer horasMaxReserva) { this.horasMaxReserva = horasMaxReserva; }

    public Integer getHorasDiaMax() { return horasDiaMax; }
    public void setHorasDiaMax(Integer horasDiaMax) { this.horasDiaMax = horasDiaMax; }

    public Integer getHorasSemanaMax() { return horasSemanaMax; }
    public void setHorasSemanaMax(Integer horasSemanaMax) { this.horasSemanaMax = horasSemanaMax; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}

