package com.medvault.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve to absolute path — same logic as AuthServiceImpl.saveFile()
        Path basePath = Paths.get(uploadDir).isAbsolute()
                ? Paths.get(uploadDir)
                : Paths.get(System.getProperty("user.dir"), uploadDir);

        String location = "file:" + basePath.toAbsolutePath().toString() + "/";
        log.info("Static file serving: /uploads/** → {}", location);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}