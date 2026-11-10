package com.example.demo.config;

import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class UploadConfig implements WebMvcConfigurer {

  @Value("${app.upload-dir}")
  String dir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler("/uploads/**")
      .addResourceLocations(
        Paths.get(dir).toAbsolutePath().normalize().toUri().toString() + "/"
      );
  }
}
