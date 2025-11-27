package com.example.backendgym.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "suscripcion_usuario")
@Check(constraints = "(membresia_id IS NOT NULL AND plan_suscripcion_id IS NULL) OR (membresia_id IS NULL AND plan_suscripcion_id IS NOT NULL)")
public class SuscripcionUsuario {
    public enum Estado {
        PENDIENTE_PAGO,
        ACTIVA,
        RECHAZADA,
        CANCELADA,
        EXPIRADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membresia_id")
    private Membresia membresia;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_suscripcion_id")
    private PlanSuscripcion planSuscripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private Estado estado = Estado.PENDIENTE_PAGO;

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "comprobante_url", length = 255)
    private String comprobanteUrl;

    @Column(name = "comentario_trabajador", length = 255)
    private String comentarioTrabajador;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Membresia getMembresia() { return membresia; }
    public void setMembresia(Membresia membresia) { this.membresia = membresia; }

    public PlanSuscripcion getPlanSuscripcion() { return planSuscripcion; }
    public void setPlanSuscripcion(PlanSuscripcion planSuscripcion) { this.planSuscripcion = planSuscripcion; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getComprobanteUrl() { return comprobanteUrl; }
    public void setComprobanteUrl(String comprobanteUrl) { this.comprobanteUrl = comprobanteUrl; }

    public String getComentarioTrabajador() { return comentarioTrabajador; }
    public void setComentarioTrabajador(String comentarioTrabajador) { this.comentarioTrabajador = comentarioTrabajador; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public Usuario getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Usuario aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    // Conveniencias
    @Transient
    public boolean esMembresia() { return this.membresia != null; }

    @Transient
    public boolean esPlan() { return this.planSuscripcion != null; }
}

