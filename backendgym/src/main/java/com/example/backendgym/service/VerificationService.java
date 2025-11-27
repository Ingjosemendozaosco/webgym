package com.example.backendgym.service;

import com.example.backendgym.domain.VerificationCode;
import com.example.backendgym.repository.VerificationCodeRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int OTP_LENGTH = 6;          
    private static final int EXPIRE_MINUTES = 15;     

    private final VerificationCodeRepository repo;
    private final MailService mail;

    public VerificationService(VerificationCodeRepository repo, MailService mail) {
        this.repo = repo;
        this.mail = mail;
    }

    private String generateNumericCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(RNG.nextInt(10)); 
        return sb.toString();
    }

    /** Código alfanumérico (letras mayúsculas) para recuperación de contraseña. */
    private String generateAlphaCode(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int idx = RNG.nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }

    public String createAndSend(String email, String purpose) {
        repo.deleteByEmailAndPurpose(email, purpose);
        // REGISTER / LOGIN siguen usando código numérico; RECOVER usa letras mayúsculas
        String code = "RECOVER".equalsIgnoreCase(purpose)
                ? generateAlphaCode(OTP_LENGTH)
                : generateNumericCode(OTP_LENGTH);
        VerificationCode vc = new VerificationCode();
        vc.setEmail(email);
        vc.setPurpose(purpose);
        vc.setCode(code);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        repo.save(vc);

        String html = """
            <div style="font-family:Arial,sans-serif">
                <h2>Código de verificación</h2>
                <p>Usa este código para continuar:</p>
                <div style="font-size:26px;font-weight:700;letter-spacing:3px;margin:12px 0">%s</div>
                <p>Vence en %d minutos.</p>
                <p style="color:#6b7280;font-size:12px">Si no solicitaste este código, ignora este mensaje.</p>
            </div>
            """.formatted(code, EXPIRE_MINUTES);

        System.out.println("[OTP] " + purpose + " para " + email + " -> " + code);
        mail.sendHtml(email, "Tu código de verificación", html);
        return code;
    }

    public boolean validate(String email, String purpose, String code) {
        Optional<VerificationCode> opt = repo.findTopByEmailAndPurposeOrderByIdDesc(email, purpose);
        return opt
            .filter(v -> v.getCode().equals(code) && v.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(v -> { repo.deleteByEmailAndPurpose(email, purpose); return true; })
            .orElse(false);
    }

    public boolean existsValid(String email, String purpose, String code) {
        Optional<VerificationCode> opt = repo.findTopByEmailAndPurposeOrderByIdDesc(email, purpose);
        return opt
            .filter(v -> v.getCode().equals(code) && v.getExpiresAt().isAfter(LocalDateTime.now()))
            .isPresent();
    }
}
