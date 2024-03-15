package com.mfemachat.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class CorsConfig implements WebFluxConfigurer {

  @SuppressWarnings("null")
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
      .addMapping("/**")
      .allowedOrigins("http://localhost:3000")
      .allowedMethods(
        "PUT",
        "DELETE",
        "GET",
        "HEAD",
        "OPTIONS",
        "POST",
        "PATCH"
      )
      .allowedHeaders("*")
        .allowCredentials(true)
      .maxAge(3600);
    // Add more mappings...
  }
}
