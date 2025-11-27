package com.example.backendgym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioRegisterDTO {
    @NotBlank @Size(min = 2, max = 60)
    private String nombre;

    @NotBlank @Size(min = 2, max = 60)
    private String apellido;

    @NotBlank @Email @Size(max = 120)
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    // getters/setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
