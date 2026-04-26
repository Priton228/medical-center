package com.medcenter.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Раздаёт статические файлы из каталога загрузок ({@code medcenter.uploads.dir}) по
 * адресу {@code /uploads/**}, чтобы клиенты могли отображать аватары и иные файлы.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MedcenterProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsDir = Paths.get(properties.getUploads().getDir()).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadsDir + "/")
            .setCachePeriod(3600);
    }
}
