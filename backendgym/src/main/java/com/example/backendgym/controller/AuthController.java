package com.example.backendgym.controller;

import com.example.backendgym.domain.PendingRegistration;
import com.example.backendgym.domain.Rol;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.dto.AuthRequest;
import com.example.backendgym.dto.AuthResponse;
import com.example.backendgym.dto.UsuarioRegisterDTO;
import com.example.backendgym.repository.PendingRegistrationRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.security.JwtService;
import com.example.backendgym.service.VerificationService;
import com.example.backendgym.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UsuarioRepository usuarioRepo;
  private final PendingRegistrationRepository pendingRepo;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final VerificationService verif;
  private final AuthService authService;

  public AuthController(
      UsuarioRepository usuarioRepo,
      PendingRegistrationRepository pendingRepo,
      PasswordEncoder encoder,
      JwtService jwt,
      VerificationService verif,
      AuthService authService
  ) {
    this.usuarioRepo = usuarioRepo;
    this.pendingRepo = pendingRepo;
    this.encoder = encoder;
    this.jwt = jwt;
    this.verif = verif;
    this.authService = authService;
  }

  // ============== REGISTRO EN 2 PASOS ==============

  // Paso 1: NO guarda usuario; guarda pendiente y envía código
  @PostMapping("/register-init")
  public Map<String,String> registerInit(@Valid @RequestBody UsuarioRegisterDTO req) {
    return authService.registerInit(req);
  }

  // Paso 2: valida código y recién crea el usuario en BD
  @PostMapping("/register-confirm")
  public Map<String,String> registerConfirm(@RequestParam String email, @RequestParam String code) {
    return authService.registerConfirm(email, code);
  }

  // ============== LOGIN EN 2 PASOS ==============

  // Paso 1: valida credenciales y envía código
  @PostMapping("/login-init")
  public Map<String,String> loginInit(@Valid @RequestBody AuthRequest req) {
    return authService.loginInit(req);
  }

  // Paso 2: confirma código y devuelve JWT
  @PostMapping("/login-confirm")
  public AuthResponse loginConfirm(@RequestParam String email, @RequestParam String code) {
    return authService.loginConfirm(email, code);
  }

  // ============== LOGIN NORMAL (sin OTP si ya verificado) ==============
  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody AuthRequest req) {
    return authService.login(req);
  }

  // ============== Recuperar contraseña ==============

  // Paso 1: envía código de recuperación (letras mayúsculas) al correo
  @PostMapping("/recover-init")
  public Map<String,String> recoverInit(@RequestParam String email) {
    return authService.recoverInit(email);
  }

  // Paso 2: valida código y permite cambiar la contraseña
  @PostMapping("/recover-confirm")
  public Map<String,String> recoverConfirm(
      @RequestParam String email,
      @RequestParam String code,
      @RequestParam String newPassword
  ) {
    return authService.recoverConfirm(email, code, newPassword);
  }

  // Validar código de recuperación sin cambiar la contraseña (uso previo en UI)
  @PostMapping("/recover-validate")
  public Map<String,String> recoverValidate(
      @RequestParam String email,
      @RequestParam String code
  ) {
    return authService.recoverValidate(email, code);
  }

  // ============== Reenviar código (opcional) ==============
  @PostMapping("/resend")
  public Map<String,String> resend(@RequestParam String email, @RequestParam String purpose) {
    return authService.resend(email, purpose);
  }
}
