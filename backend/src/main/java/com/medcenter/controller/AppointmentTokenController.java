package com.medcenter.controller;

import com.medcenter.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Эндпоинты подтверждения/отклонения приёма по одноразовому токену из email.
 *
 * <p>GET-методы возвращают HTML-страницу с формой и кнопкой POST, чтобы безопасно
 * работать с почтовыми клиентами/SecGW, которые превентивно прогружают ссылки
 * (Gmail, Outlook, Mimecast, Proofpoint и т.п.). Само изменение состояния
 * выполняется только в POST-обработчике.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentTokenController {

    private final AppointmentService appointmentService;

    /** Страница «Подтвердить приём» — никаких сайд-эффектов. */
    @GetMapping(value = "/confirm", produces = MediaType.TEXT_HTML_VALUE)
    public String confirmPage(@RequestParam("token") String token) {
        return renderPage(token, "Подтверждение записи",
            "Чтобы подтвердить запись на приём, нажмите кнопку ниже.",
            "Подтвердить", "/api/v1/appointments/confirm", "#0e7490");
    }

    /** Страница «Отменить приём» — никаких сайд-эффектов. */
    @GetMapping(value = "/reject", produces = MediaType.TEXT_HTML_VALUE)
    public String rejectPage(@RequestParam("token") String token) {
        return renderPage(token, "Отмена записи",
            "Чтобы отменить запись на приём, нажмите кнопку ниже.",
            "Отменить запись", "/api/v1/appointments/reject", "#e11d48");
    }

    /** Подтверждение записи по одноразовому токену (вызывается из формы выше). */
    @PostMapping(value = "/confirm", produces = MediaType.TEXT_HTML_VALUE)
    public String confirmSubmit(@RequestParam("token") String token) {
        appointmentService.confirmByToken(token);
        return doneHtml("Запись подтверждена",
            "Спасибо! Мы передали подтверждение врачу. До встречи на приёме.", "#16a34a");
    }

    /** Отмена записи по одноразовому токену. */
    @PostMapping(value = "/reject", produces = MediaType.TEXT_HTML_VALUE)
    public String rejectSubmit(@RequestParam("token") String token) {
        appointmentService.rejectByToken(token);
        return doneHtml("Запись отменена",
            "Запись на приём отменена. Если это произошло по ошибке, оформите новую запись через личный кабинет.", "#e11d48");
    }

    private String renderPage(String token, String title, String message,
                              String buttonText, String formAction, String accent) {
        String safeToken = escape(token);
        return "<!doctype html><html lang=\"ru\"><head><meta charset=\"utf-8\">"
             + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
             + "<title>" + escape(title) + "</title></head>"
             + "<body style=\"font-family:system-ui,-apple-system,sans-serif;background:#f1f5f9;margin:0;padding:24px\">"
             + "<div style=\"max-width:480px;margin:48px auto;padding:32px;background:#fff;border-radius:16px;box-shadow:0 4px 16px rgba(15,23,42,.08)\">"
             + "<h2 style=\"color:#0f172a;margin-top:0\">" + escape(title) + "</h2>"
             + "<p style=\"color:#475569\">" + escape(message) + "</p>"
             + "<form method=\"post\" action=\"" + formAction + "\">"
             + "<input type=\"hidden\" name=\"token\" value=\"" + safeToken + "\">"
             + "<button type=\"submit\" style=\"display:inline-block;padding:12px 24px;background:" + accent + ";color:#fff;border:0;border-radius:10px;font-size:15px;cursor:pointer\">"
             + escape(buttonText) + "</button>"
             + "</form></div></body></html>";
    }

    private String doneHtml(String title, String message, String accent) {
        return "<!doctype html><html lang=\"ru\"><head><meta charset=\"utf-8\">"
             + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
             + "<title>" + escape(title) + "</title></head>"
             + "<body style=\"font-family:system-ui,-apple-system,sans-serif;background:#f1f5f9;margin:0;padding:24px\">"
             + "<div style=\"max-width:480px;margin:48px auto;padding:32px;background:#fff;border-radius:16px;box-shadow:0 4px 16px rgba(15,23,42,.08)\">"
             + "<h2 style=\"color:" + accent + ";margin-top:0\">" + escape(title) + "</h2>"
             + "<p style=\"color:#475569\">" + escape(message) + "</p>"
             + "</div></body></html>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
