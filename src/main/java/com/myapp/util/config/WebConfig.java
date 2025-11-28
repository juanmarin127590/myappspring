package com.myapp.util.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    // Habilitar CORS en Spring Boot: Debemos decirle a Spring Boot que acepte
    // peticiones desde cualquier origen (tu app web o móvil).

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Permite peticiones desde cualquier origen a cualquier endpoint
                registry.addMapping("/**")
                        .allowedOriginPatterns("*") // Usa patterns en lugar de origins si usas credenciales
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*") // Importante para dejar pasar 'Authorization'
                        .allowCredentials(true);
            }
        };
    }
}
