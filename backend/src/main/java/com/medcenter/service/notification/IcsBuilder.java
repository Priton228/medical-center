package com.medcenter.service.notification;

import com.medcenter.domain.Appointment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Минимальный генератор iCalendar (.ics) файлов для случаев, когда Google Calendar
 * не настроен. Сгенерированный файл прикрепляется к письму, и пользователь может
 * добавить событие в свой календарь вручную.
 */
public final class IcsBuilder {

    private static final DateTimeFormatter UTC_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private IcsBuilder() {}

    public static String build(Appointment a, String method) {
        return build(
            "appt-" + a.getId() + "@medical-center.local",
            a.getAppointmentDate(),
            a.getAppointmentDate().plusMinutes(30),
            "Приём у врача — " + a.getDoctor().getUser().getFullName(),
            "Специализация: " + a.getDoctor().getSpecialization()
                + (a.getNotes() != null ? "\\nЗаметка: " + a.getNotes() : ""),
            "Кабинет " + (a.getDoctor().getRoomNumber() == null ? "—" : a.getDoctor().getRoomNumber()),
            method
        );
    }

    public static String build(String uid, LocalDateTime start, LocalDateTime end,
                               String summary, String description, String location,
                               String method) {
        String startUtc = start.atOffset(ZoneOffset.UTC).format(UTC_FORMAT);
        String endUtc   = end.atOffset(ZoneOffset.UTC).format(UTC_FORMAT);
        String now      = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(UTC_FORMAT);
        return "BEGIN:VCALENDAR\r\n" +
               "VERSION:2.0\r\n" +
               "PRODID:-//Medical Center//RU\r\n" +
               "CALSCALE:GREGORIAN\r\n" +
               "METHOD:" + method + "\r\n" +
               "BEGIN:VEVENT\r\n" +
               "UID:" + uid + "\r\n" +
               "DTSTAMP:" + now + "\r\n" +
               "DTSTART:" + startUtc + "\r\n" +
               "DTEND:" + endUtc + "\r\n" +
               "SUMMARY:" + escape(summary) + "\r\n" +
               "DESCRIPTION:" + escape(description) + "\r\n" +
               "LOCATION:" + escape(location) + "\r\n" +
               "END:VEVENT\r\n" +
               "END:VCALENDAR\r\n";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n");
    }
}
