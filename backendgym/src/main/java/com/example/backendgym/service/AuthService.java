package com.example.backendgym.service;

import com.example.backendgym.domain.PendingRegistration;
import com.example.backendgym.domain.Rol;
import com.example.backendgym.domain.Usuario;
import com.example.backendgym.dto.AuthRequest;
import com.example.backendgym.dto.AuthResponse;
import com.example.backendgym.dto.UsuarioRegisterDTO;
import com.example.backendgym.repository.PendingRegistrationRepository;
import com.example.backendgym.repository.UsuarioRepository;
import com.example.backendgym.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PendingRegistrationRepository pendingRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final com.example.backendgym.service.VerificationService verif;

    public AuthService(UsuarioRepository usuarioRepo,
                       PendingRegistrationRepository pendingRepo,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       com.example.backendgym.service.VerificationService verif) {
        this.usuarioRepo = usuarioRepo;
        this.pendingRepo = pendingRepo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.verif = verif;
    }

    public Map<String,String> registerInit(UsuarioRegisterDTO req) {
        System.out.println("[register-init] email=" + req.getEmail());
        if (usuarioRepo.findByEmail(req.getEmail()).isPresent())
            throw new RuntimeException("El correo ya existe en el sistema");

        pendingRepo.findByEmail(req.getEmail()).ifPresent(p -> pendingRepo.deleteByEmail(req.getEmail()));

        PendingRegistration p = new PendingRegistration();
        p.setNombre(req.getNombre());
        p.setApellido(req.getApellido());
        p.setEmail(req.getEmail());
        p.setPasswordHash(encoder.encode(req.getPassword()));
        p.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        pendingRepo.save(p);

        verif.createAndSend(req.getEmail(), "REGISTER");
        return Map.of("status","PENDING","message","Se envió un código a tu correo");
    }

    public Map<String,String> registerConfirm(String email, String code) {
        PendingRegistration p = pendingRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No hay registro pendiente para este correo"));
        if (p.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Registro pendiente expirado; vuelve a iniciar");

        boolean ok = verif.validate(email, "REGISTER", code);
        if (!ok) throw new RuntimeException("Código inválido o expirado");

        if (usuarioRepo.findByEmail(email).isPresent())
            throw new RuntimeException("El correo ya existe en el sistema");

        Usuario u = new Usuario();
        u.setNombre(p.getNombre());
        u.setApellido(p.getApellido());
        u.setEmail(p.getEmail());
        u.setPassword(p.getPasswordHash());
        u.setRol(Rol.USUARIO);
        u.setVerificado(true);
        u.setQrToken(java.util.UUID.randomUUID().toString());
        usuarioRepo.save(u);

        pendingRepo.deleteByEmail(email);
        return Map.of("status","CREATED","message","Usuario creado");
    }

    public Map<String,String> loginInit(AuthRequest req) {
        Usuario u = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        if (!encoder.matches(req.getPassword(), u.getPassword()))
            throw new RuntimeException("Credenciales inválidas");
        if (Boolean.TRUE.equals(u.getVerificado())) {
            return Map.of(
                    "status", "ALREADY_VERIFIED",
                    "message", "El usuario está verificado. Usa /api/auth/login para iniciar sesión.");
        }
        verif.createAndSend(u.getEmail(), "LOGIN");
        return Map.of("status","OTP_REQUIRED","message","Se envió un código a tu correo");
    }

    public AuthResponse loginConfirm(String email, String code) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        boolean ok = verif.validate(email, "LOGIN", code);
        if (!ok) throw new RuntimeException("Código inválido o expirado");
        String token = jwt.generateToken(u.getEmail(), u.getRol().name(), u.getId());
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest req) {
        Usuario u = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        if (!encoder.matches(req.getPassword(), u.getPassword()))
            throw new RuntimeException("Credenciales inválidas");
        if (!Boolean.TRUE.equals(u.getVerificado()))
            throw new RuntimeException("Cuenta no verificada. Completa la verificación del registro.");
        String token = jwt.generateToken(u.getEmail(), u.getRol().name(), u.getId());
        return new AuthResponse(token);
    }

    public Map<String,String> recoverInit(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));
        verif.createAndSend(u.getEmail(), "RECOVER");
        return Map.of("status","PENDING","message","Se envió un código de recuperación a tu correo");
    }

    public Map<String,String> recoverConfirm(String email, String code, String newPassword) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

        boolean ok = verif.validate(email, "RECOVER", code);
        if (!ok) throw new RuntimeException("Código inválido o expirado");

        u.setPassword(encoder.encode(newPassword));
        usuarioRepo.save(u);
        return Map.of("status","UPDATED","message","Contraseña actualizada correctamente");
    }

    public Map<String,String> recoverValidate(String email, String code) {
        usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

        boolean ok = verif.existsValid(email, "RECOVER", code);
        if (!ok) throw new RuntimeException("Código inválido o expirado");

        return Map.of("status","VALID","message","Código válido");
    }

    public Map<String,String> resend(String email, String purpose) {
        verif.createAndSend(email, purpose);
        return Map.of("status","RESENT","message","Código reenviado");
    }
}
