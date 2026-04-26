package com.medcenter.service.scheduling;

import com.medcenter.config.MedcenterProperties;
import com.medcenter.domain.Appointment;
import com.medcenter.domain.enums.AppointmentStatus;
import com.medcenter.repository.AppointmentRepository;
import com.medcenter.service.notification.AppointmentNotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Раз в 5 минут просматривает приёмы, до которых осталось ровно ~24 часа,
 * и отправляет напоминание ({@link AppointmentNotificationFacade#onReminder}).
 *
 * Окно — {@code [now+22h, now+25h]} с пометкой {@code reminderSentAt}, чтобы каждое письмо
 * отправлялось максимум один раз. Часов до напоминания — настраивается в {@code application.yml}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentNotificationFacade notificationFacade;
    private final MedcenterProperties properties;

    /** Запуск раз в 5 минут. */
    @Scheduled(fixedDelayString = "PT5M", initialDelay = 30_000)
    @Transactional
    public void sendUpcomingReminders() {
        int hoursBefore = properties.getNotifications().getReminderHoursBefore();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowFrom = now.plusHours(hoursBefore - 2L);
        LocalDateTime windowTo   = now.plusHours(hoursBefore + 1L);

        List<Appointment> due = appointmentRepository
            .findByStatusInAndAppointmentDateBetweenAndReminderSentAtIsNull(
                List.of(AppointmentStatus.PLANNED, AppointmentStatus.CONFIRMED),
                windowFrom, windowTo);

        if (due.isEmpty()) return;
        log.info("Reminder scheduler: {} напоминаний для отправки (окно {} → {})", due.size(), windowFrom, windowTo);
        for (Appointment a : due) {
            try {
                notificationFacade.onReminder(a);
                a.setReminderSentAt(LocalDateTime.now());
                appointmentRepository.save(a);
            } catch (Exception e) {
                log.error("Reminder для appt={} не отправлен: {}", a.getId(), e.getMessage(), e);
            }
        }
    }
}
