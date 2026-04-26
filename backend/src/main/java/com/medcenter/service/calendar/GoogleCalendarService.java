package com.medcenter.service.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.medcenter.config.MedcenterProperties;
import com.medcenter.domain.Appointment;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Интеграция с Google Calendar.
 * Активируется только если включён флаг {@code medcenter.google-calendar.enabled} и
 * задан {@code medcenter.google-calendar.service-account-json}. В противном случае
 * методы возвращают {@code null} / no-op, и система работает без синхронизации.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private final MedcenterProperties properties;
    private volatile Calendar calendarClient;

    @PostConstruct
    void init() {
        try {
            this.calendarClient = buildClient();
            if (calendarClient == null) {
                log.info("GoogleCalendarService: интеграция выключена (calendar-id={}, enabled={}).",
                    properties.getGoogleCalendar().getCalendarId(),
                    properties.getGoogleCalendar().isEnabled());
            } else {
                log.info("GoogleCalendarService: подключено к календарю {}",
                    properties.getGoogleCalendar().getCalendarId());
            }
        } catch (Exception e) {
            log.error("GoogleCalendarService: не удалось инициализировать клиента: {}",
                e.getMessage(), e);
            this.calendarClient = null;
        }
    }

    public boolean isEnabled() {
        return calendarClient != null;
    }

    private Calendar buildClient() throws Exception {
        MedcenterProperties.GoogleCalendar cfg = properties.getGoogleCalendar();
        if (!cfg.isEnabled()) return null;
        if (cfg.getServiceAccountJson() == null || cfg.getServiceAccountJson().isBlank()) {
            return null;
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(
            new ByteArrayInputStream(cfg.getServiceAccountJson().getBytes(StandardCharsets.UTF_8))
        ).createScoped(List.of(CalendarScopes.CALENDAR));
        if (cfg.getImpersonateEmail() != null && !cfg.getImpersonateEmail().isBlank()) {
            credentials = credentials.createDelegated(cfg.getImpersonateEmail());
        }
        HttpRequestInitializer initializer = new HttpCredentialsAdapter(credentials);
        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            initializer
        ).setApplicationName(cfg.getApplicationName()).build();
    }

    /** Создаёт событие. Возвращает event id или {@code null}, если интеграция не активна. */
    public String createEvent(Appointment a) {
        if (!isEnabled()) return null;
        try {
            Event event = toEvent(a);
            Event created = calendarClient.events()
                .insert(properties.getGoogleCalendar().getCalendarId(), event)
                .execute();
            log.info("Calendar event создан: id={} appt={}", created.getId(), a.getId());
            return created.getId();
        } catch (Exception e) {
            log.error("Calendar create error appt={}: {}", a.getId(), e.getMessage(), e);
            return null;
        }
    }

    /** Обновляет событие; используется при переносе. */
    public void updateEvent(Appointment a) {
        if (!isEnabled() || a.getCalendarEventId() == null) return;
        try {
            Event event = toEvent(a);
            calendarClient.events()
                .update(properties.getGoogleCalendar().getCalendarId(), a.getCalendarEventId(), event)
                .execute();
            log.info("Calendar event обновлён: id={} appt={}", a.getCalendarEventId(), a.getId());
        } catch (Exception e) {
            log.error("Calendar update error appt={}: {}", a.getId(), e.getMessage(), e);
        }
    }

    /** Удаляет событие; используется при отмене записи. */
    public void deleteEvent(String eventId) {
        if (!isEnabled() || eventId == null) return;
        try {
            calendarClient.events()
                .delete(properties.getGoogleCalendar().getCalendarId(), eventId)
                .execute();
            log.info("Calendar event удалён: id={}", eventId);
        } catch (Exception e) {
            log.error("Calendar delete error event={}: {}", eventId, e.getMessage(), e);
        }
    }

    private Event toEvent(Appointment a) {
        Event event = new Event()
            .setSummary("Приём у врача — " + a.getDoctor().getUser().getFullName())
            .setDescription(buildDescription(a))
            .setLocation("Кабинет " + (a.getDoctor().getRoomNumber() == null ? "—" : a.getDoctor().getRoomNumber()));
        // appointmentDate хранится в UTC (см. spring.jpa.properties.hibernate.jdbc.time_zone=UTC),
        // поэтому интерпретируем LocalDateTime как UTC, а не как системную TZ.
        ZonedDateTime start = a.getAppointmentDate().atZone(ZoneOffset.UTC);
        ZonedDateTime end = start.plusMinutes(30);
        event.setStart(new EventDateTime()
            .setDateTime(new com.google.api.client.util.DateTime(start.toInstant().toEpochMilli()))
            .setTimeZone("UTC"));
        event.setEnd(new EventDateTime()
            .setDateTime(new com.google.api.client.util.DateTime(end.toInstant().toEpochMilli()))
            .setTimeZone("UTC"));
        if (a.getPatient().getUser().getEmail() != null) {
            event.setAttendees(Collections.singletonList(
                new EventAttendee().setEmail(a.getPatient().getUser().getEmail())));
        }
        return event;
    }

    private String buildDescription(Appointment a) {
        return "Специализация: " + a.getDoctor().getSpecialization()
            + (a.getNotes() != null ? "\nЗаметка: " + a.getNotes() : "")
            + "\nСтатус: " + a.getStatus();
    }
}
