package com.example.backendgym.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "usuario")
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	private String nombre;

	@NotBlank
	@Size(max = 100)
	private String apellido = "";

	@NotBlank
	@Email
	@Size(max = 150)
	@Column(unique = true)
	private String email;

	@NotBlank
	@Size(max = 255)
	@Column(name = "password_hash")
	private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Rol rol = Rol.USUARIO;

    @Size(max = 30)
    private String telefono;

    @Size(max = 255)
    private String direccion;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean activo = Boolean.TRUE; // por defecto activo

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean verificado = Boolean.FALSE; // por defecto no verificado

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "recordatorios_activos")
    private Boolean recordatoriosActivos = Boolean.FALSE;

    @Column(name = "qr_token", length = 64, unique = true)
    private String qrToken;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }

	public String getApellido() { return apellido; }
	public void setApellido(String apellido) { this.apellido = apellido; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public Rol getRol() { return rol; }
	public void setRol(Rol rol) { this.rol = rol; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Boolean getActivo() { return activo != null ? activo : Boolean.TRUE; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Boolean getVerificado() { return verificado != null ? verificado : Boolean.FALSE; }
    public void setVerificado(Boolean verificado) { this.verificado = verificado; }

    public Boolean getRecordatoriosActivos() { return recordatoriosActivos != null ? recordatoriosActivos : Boolean.FALSE; }
    public void setRecordatoriosActivos(Boolean recordatoriosActivos) { this.recordatoriosActivos = recordatoriosActivos; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

	@Column(name="email_verificado")
	private boolean emailVerificado = false;

	public boolean isEmailVerificado() { return emailVerificado; }
	public void setEmailVerificado(boolean emailVerificado) { this.emailVerificado = emailVerificado; }


}
