package com.example.backendgym.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class MailService {
  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:}")
  private String from;
  
  public MailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Async
  public void sendHtml(@Nullable String to, String subject, String html) {
    if (to == null || to.isBlank()) {
      System.err.println("[MAIL] destinatario vacío, no se envía.");
      return;
    }
    try {
      MimeMessage mime = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
      if (from != null && !from.isBlank()) helper.setFrom(from); // 👈 From explícito (Gmail lo exige)
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(html, true);
      mailSender.send(mime);
      System.out.println("[MAIL] Enviado a " + to + " asunto='" + subject + "'");
    } catch (MessagingException | MailException e) {
      System.err.println("[MAIL] ERROR enviando a " + to + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
      e.printStackTrace(); // 👈 así ves la causa exacta (credenciales, 535, TLS, etc.)
    }
  }

  /** Enviar HTML con un adjunto genérico (por ejemplo, PDF). */
  @Async
  public void sendHtmlWithAttachment(@Nullable String to, String subject, String html,
                                     byte[] attachmentBytes, String attachmentFilename, String contentType) {
    if (to == null || to.isBlank()) {
      System.err.println("[MAIL] destinatario vacío, no se envía (adjunto).");
      return;
    }
    try {
      MimeMessage mime = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
      if (from != null && !from.isBlank()) helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(html, true);
      if (attachmentBytes != null && attachmentBytes.length > 0) {
        ByteArrayResource res = new ByteArrayResource(attachmentBytes);
        helper.addAttachment(attachmentFilename != null ? attachmentFilename : "comprobante.pdf", res, contentType);
      }
      mailSender.send(mime);
      System.out.println("[MAIL] Enviado con adjunto a " + to + " asunto='" + subject + "'");
    } catch (MessagingException | MailException e) {
      System.err.println("[MAIL] ERROR enviando (adjunto) a " + to + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
      e.printStackTrace();
    }
  }
}
