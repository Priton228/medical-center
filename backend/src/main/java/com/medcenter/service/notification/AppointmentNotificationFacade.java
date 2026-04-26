package com.medcenter.service.notification;

import com.medcenter.config.MedcenterProperties;
import com.medcenter.domain.Appointment;
import com.medcenter.service.calendar.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Высокоуровневый фасад над интеграциями (email + Google Calendar).
 * Отделяет {@link com.medcenter.service.AppointmentService} от деталей доставки уведомлений.
 *
 * Шаблон проектирования: Facade.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentNotificationFacade {

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", new Locale("ru"));

    private final MedcenterProperties properties;
    private final EmailNotificationService email;
    private final GoogleCalendarService calendar;

    /** Вызывается при создании записи — отправляет письмо пациенту и врачу, создаёт событие в календаре. */
    public void onCreated(Appointment a) {
        log.info("Notification#onCreated appt={} status={}", a.getId(), a.getStatus());

        // Google Calendar: создание события
        String eventId = calendar.createEvent(a);
        if (eventId != null) {
            a.setCalendarEventId(eventId);
        }

        // Email пациенту
        sendToPatient(a, "Запись на приём оформлена",
            "Здравствуйте, " + a.getPatient().getUser().getFullName() + "!",
            "Ваша запись успешно создана. Просим подтвердить участие или, если планы изменились, отменить запись.",
            true,
            "REQUEST"
        );
        // Email врачу
        sendToDoctor(a,
            "Новая запись на приём",
            "Здравствуйте, " + a.getDoctor().getUser().getFullName() + "!",
            "Пациент " + a.getPatient().getUser().getFullName() + " записался к вам.",
            "REQUEST"
        );
    }

    /** Вызывается при подтверждении записи. */
    public void onConfirmed(Appointment a) {
        log.info("Notification#onConfirmed appt={}", a.getId());
        calendar.updateEvent(a);
        sendToPatient(a, "Запись подтверждена",
            "Здравствуйте, " + a.getPatient().getUser().getFullName() + "!",
            "Ваша запись подтверждена. Ждём вас в указанное время.",
            false,
            "REQUEST"
        );
        sendToDoctor(a, "Запись подтверждена",
            "Здравствуйте, " + a.getDoctor().getUser().getFullName() + "!",
            "Пациент " + a.getPatient().getUser().getFullName() + " подтвердил запись.",
            "REQUEST"
        );
    }

    /** Вызывается при отмене записи. */
    public void onCancelled(Appointment a) {
        log.info("Notification#onCancelled appt={} eventId={}", a.getId(), a.getCalendarEventId());
        if (a.getCalendarEventId() != null) {
            calendar.deleteEvent(a.getCalendarEventId());
            a.setCalendarEventId(null);
        }
        sendToPatient(a, "Запись отменена",
            "Здравствуйте, " + a.getPatient().getUser().getFullName() + "!",
            "Ваша запись на приём была отменена. Если это произошло по ошибке — оформите новую запись через личный кабинет.",
            false,
            "CANCEL"
        );
        sendToDoctor(a, "Запись отменена",
            "Здравствуйте, " + a.getDoctor().getUser().getFullName() + "!",
            "Запись пациента " + a.getPatient().getUser().getFullName() + " была отменена.",
            "CANCEL"
        );
    }

    /** Вызывается при переносе. {@code oldDate} — прежнее время, для уведомления пациента. */
    public void onRescheduled(Appointment a) {
        log.info("Notification#onRescheduled appt={} newDate={}", a.getId(), a.getAppointmentDate());
        if (a.getCalendarEventId() != null) {
            calendar.updateEvent(a);
        } else {
            String newId = calendar.createEvent(a);
            if (newId != null) a.setCalendarEventId(newId);
        }
        sendToPatient(a, "Запись перенесена",
            "Здравствуйте, " + a.getPatient().getUser().getFullName() + "!",
            "Время вашего приёма было изменено. Новая дата: " + DATE_FMT.format(a.getAppointmentDate()) + ".",
            true,
            "REQUEST"
        );
        sendToDoctor(a, "Запись перенесена",
            "Здравствуйте, " + a.getDoctor().getUser().getFullName() + "!",
            "Запись пациента " + a.getPatient().getUser().getFullName() + " перенесена. Новая дата: " + DATE_FMT.format(a.getAppointmentDate()) + ".",
            "REQUEST"
        );
    }

    /** Вызывается планировщиком за 24 часа до приёма. */
    public void onReminder(Appointment a) {
        log.info("Notification#onReminder appt={}", a.getId());
        sendToPatient(a, "Напоминание о приёме",
            "Здравствуйте, " + a.getPatient().getUser().getFullName() + "!",
            "Напоминаем, что приём состоится через ~24 часа. Если планы изменились — пожалуйста, отмените запись через личный кабинет.",
            true,
            "REQUEST"
        );
    }

    // -- helpers ---------------------------------------------------------

    private void sendToPatient(Appointment a, String subject, String greeting, String body,
                               boolean withConfirmButtons, String icsMethod) {
        String to = a.getPatient().getUser().getEmail();
        if (to == null || to.isBlank()) return;
        String html = renderHtml(a, greeting, body, withConfirmButtons);
        String ics = calendar.isEnabled() ? null : IcsBuilder.build(a, icsMethod);
        email.send(to, subject, html, ics, "appointment-" + a.getId() + ".ics");
    }

    private void sendToDoctor(Appointment a, String subject, String greeting, String body, String icsMethod) {
        String to = a.getDoctor().getUser().getEmail();
        if (to == null || to.isBlank()) return;
        String html = renderHtml(a, greeting, body, false);
        String ics = calendar.isEnabled() ? null : IcsBuilder.build(a, icsMethod);
        email.send(to, subject, html, ics, "appointment-" + a.getId() + ".ics");
    }

    private String renderHtml(Appointment a, String greeting, String body, boolean withConfirmButtons) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Arial,sans-serif;color:#0f172a'>");
        sb.append("<div style='max-width:560px;margin:0 auto;padding:24px;background:#f1f5f9;border-radius:12px'>");
        sb.append("<h2 style='color:#0e7490;margin-top:0'>МедЦентр</h2>");
        sb.append("<p>").append(escape(greeting)).append("</p>");
        sb.append("<p>").append(escape(body)).append("</p>");
        sb.append("<table style='border-collapse:collapse;margin:12px 0'>");
        sb.append("<tr><td style='padding:4px 8px;color:#475569'>Дата приёма:</td>");
        sb.append("<td style='padding:4px 8px;font-weight:bold'>").append(DATE_FMT.format(a.getAppointmentDate())).append("</td></tr>");
        sb.append("<tr><td style='padding:4px 8px;color:#475569'>Врач:</td>");
        sb.append("<td style='padding:4px 8px'>").append(escape(a.getDoctor().getUser().getFullName())).append("</td></tr>");
        sb.append("<tr><td style='padding:4px 8px;color:#475569'>Специализация:</td>");
        sb.append("<td style='padding:4px 8px'>").append(escape(a.getDoctor().getSpecialization())).append("</td></tr>");
        sb.append("<tr><td style='padding:4px 8px;color:#475569'>Кабинет:</td>");
        sb.append("<td style='padding:4px 8px'>")
            .append(a.getDoctor().getRoomNumber() == null ? "—" : escape(a.getDoctor().getRoomNumber()))
            .append("</td></tr>");
        sb.append("</table>");
        if (withConfirmButtons && a.getConfirmToken() != null) {
            String base = properties.getNotifications().getPublicBackendUrl();
            String confirm = base + "/api/v1/appointments/confirm?token=" + a.getConfirmToken();
            String reject  = base + "/api/v1/appointments/reject?token="  + a.getConfirmToken();
            sb.append("<p style='margin-top:16px'>")
              .append("<a href='").append(confirm).append("' style='display:inline-block;padding:10px 18px;background:#0e7490;color:#fff;border-radius:8px;text-decoration:none;margin-right:8px'>Подтвердить</a>")
              .append("<a href='").append(reject).append("' style='display:inline-block;padding:10px 18px;background:#e11d48;color:#fff;border-radius:8px;text-decoration:none'>Отменить</a>")
              .append("</p>");
        }
        String front = properties.getNotifications().getPublicFrontendUrl();
        sb.append("<p style='font-size:12px;color:#64748b;margin-top:24px'>")
          .append("Открыть в личном кабинете: <a href='").append(front).append("'>").append(front).append("</a>")
          .append("</p></div></body></html>");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
