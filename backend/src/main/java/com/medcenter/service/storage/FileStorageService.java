package com.medcenter.service.storage;

import com.medcenter.config.MedcenterProperties;
import com.medcenter.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Хранение пользовательских файлов на локальной файловой системе.
 * Каталог настраивается через {@code medcenter.uploads.dir}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024L; // 5 MB

    private final MedcenterProperties properties;

    /**
     * Сохраняет аватар пользователя и возвращает публичный URL для отображения в UI.
     * @param userId владелец файла (используется в имени файла, чтобы старые перезаписывались).
     * @param file загруженный файл.
     * @return публичный URL вида {@code /uploads/avatars/{userId}-{uuid}.{ext}}
     */
    public String saveAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл не выбран");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BadRequestException("Файл слишком большой (максимум 5 МБ)");
        }
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BadRequestException("Допустимые форматы: jpg, png, webp, gif");
        }

        String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif";
            default -> "img";
        };

        Path baseDir = Paths.get(properties.getUploads().getDir(), "avatars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать каталог для аватаров", e);
        }

        String name = userId + "-" + UUID.randomUUID() + "." + ext;
        Path target = baseDir.resolve(name);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
        log.info("Сохранён аватар: user={} path={}", userId, target);
        return "/uploads/avatars/" + name;
    }

    /** Удаляет файл по публичному URL ({@code /uploads/...}). Тихо игнорирует ошибки. */
    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) return;
        Path baseDir = Paths.get(properties.getUploads().getDir()).toAbsolutePath().normalize();
        Path file = baseDir.resolve(publicUrl.substring("/uploads/".length())).normalize();
        if (!file.startsWith(baseDir)) return; // path traversal guard
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Не удалось удалить файл {}: {}", file, e.getMessage());
        }
    }
}
