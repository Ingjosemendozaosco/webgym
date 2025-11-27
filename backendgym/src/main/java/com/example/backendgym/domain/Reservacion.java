package com.example.backendgym.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservacion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JsonIgnoreProperties({"reservaciones", "password", "roles", "hibernateLazyInitializer", "handler"})
	private Usuario usuario;

	@ManyToOne
	private Membresia membresia; 

	@ManyToOne
	private Producto producto; 

	@NotNull
	private LocalDateTime fecha;

	@Column(name = "duracion_minutos")
	private Integer duracionMinutos = 60;

	@Enumerated(EnumType.STRING)
	private EstadoReservacion estado = EstadoReservacion.PENDIENTE;

	public enum EstadoReservacion { PENDIENTE, CONFIRMADA, CANCELADA, REVISION, ANULADA }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Usuario getUsuario() { return usuario; }
	public void setUsuario(Usuario usuario) { this.usuario = usuario; }

	public Membresia getMembresia() { return membresia; }
	public void setMembresia(Membresia membresia) { this.membresia = membresia; }

	public Producto getProducto() { return producto; }
	public void setProducto(Producto producto) { this.producto = producto; }

	public LocalDateTime getFecha() { return fecha; }
	public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

	public Integer getDuracionMinutos() { return duracionMinutos; }
	public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

	public EstadoReservacion getEstado() { return estado; }
	public void setEstado(EstadoReservacion estado) { this.estado = estado; }
}
