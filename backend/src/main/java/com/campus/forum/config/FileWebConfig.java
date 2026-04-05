package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileWebConfig implements WebMvcConfigurer {

    @Value("${forum.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = System.getProperty("user.dir").replace("\\", "/");
        String location = "file:" + baseDir + "/" + uploadDir + "/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(location);
    }
}

