package com.medcenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Типобезопасный доступ к настройкам {@code medcenter.*} из application.yml. */
@Configuration
@ConfigurationProperties(prefix = "medcenter")
@Data
public class MedcenterProperties {

    private final Uploads uploads = new Uploads();
    private final Notifications notifications = new Notifications();
    private final GoogleCalendar googleCalendar = new GoogleCalendar();

    @Data
    public static class Uploads {
        private String dir = "./uploads";
        private String publicBaseUrl = "http://localhost:8080";
    }

    @Data
    public static class Notifications {
        private boolean enabled = true;
        private String fromAddress = "no-reply@medical-center.local";
        private String fromName = "МедЦентр";
        private String publicFrontendUrl = "http://localhost:5173";
        /**
         * Публичный URL backend-API, на который пользователь попадёт по ссылкам
         * «Подтвердить» / «Отменить» из писем. Должен быть доступен снаружи.
         */
        private String publicBackendUrl = "http://localhost:8080";
        private int reminderHoursBefore = 24;
    }

    @Data
    public static class GoogleCalendar {
        private boolean enabled = false;
        private String calendarId = "primary";
        private String applicationName = "MedicalCenter";
        private String serviceAccountJson = "";
        private String impersonateEmail = "";
    }
}
