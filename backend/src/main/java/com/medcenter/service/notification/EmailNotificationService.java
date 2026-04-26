package com.medcenter.service.notification;

import com.medcenter.config.MedcenterProperties;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Тонкая обёртка над Spring Mail, которая отправляет HTML-письма.
 * Если SMTP-настройки пустые или отправитель отключён — вызовы становятся no-op
 * (логируются как WARN), чтобы можно было запускать систему локально без SMTP.
 */
@Service
@Slf4j
public class EmailNotificationService {

    private final MedcenterProperties properties;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Autowired
    public EmailNotificationService(MedcenterProperties properties,
                                    JavaMailSender mailSender) {
        this.properties = properties;
        this.mailSender = mailSender;
    }

    @PostConstruct
    void announce() {
        if (!isConfigured()) {
            log.warn("EmailNotificationService: SMTP не настроен (MEDCENTER_SMTP_USERNAME пуст). " +
                "Уведомления будут логироваться, но не отправляться по почте.");
        } else {
            log.info("EmailNotificationService: SMTP сконфигурирован, отправляем письма от имени {}",
                properties.getNotifications().getFromAddress());
        }
    }

    public boolean isConfigured() {
        return properties.getNotifications().isEnabled()
            && smtpUsername != null && !smtpUsername.isBlank();
    }

    public void send(String to, String subject, String htmlBody) {
        send(to, subject, htmlBody, null, null);
    }

    /** Отправляет письмо. Если опционально передан icsContent — добавляется как вложение. */
    public void send(String to, String subject, String htmlBody, String icsContent, String icsFileName) {
        if (to == null || to.isBlank()) {
            log.debug("EmailNotificationService: пропускаем отправку, адресат пуст");
            return;
        }
        if (!isConfigured()) {
            log.info("EmailNotificationService [DRY-RUN] -> {} :: {}", to, subject);
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            try {
                helper.setFrom(new InternetAddress(
                    properties.getNotifications().getFromAddress(),
                    properties.getNotifications().getFromName(),
                    StandardCharsets.UTF_8.name()
                ));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(properties.getNotifications().getFromAddress());
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (icsContent != null && !icsContent.isBlank()) {
                String name = icsFileName == null || icsFileName.isBlank() ? "appointment.ics" : icsFileName;
                helper.addAttachment(name, () -> new java.io.ByteArrayInputStream(
                    icsContent.getBytes(StandardCharsets.UTF_8)));
            }
            mailSender.send(mime);
            log.info("Email sent to {} :: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Не удалось отправить email на {} ({}): {}", to, subject, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Сбой EmailNotificationService при отправке на {}: {}", to, e.getMessage(), e);
        }
    }
}
