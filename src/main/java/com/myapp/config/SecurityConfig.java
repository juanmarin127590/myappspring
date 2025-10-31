package com.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring Security
public class SecurityConfig {

    // Define un codificador de contraseñas (Patrón de Diseño: Factory Method)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt es el estándar de la industria
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // Configura Jackson para ser más flexible con el Content-Type,
        // o maneja el charset de forma explícita.
        // Aunque el problema principal es la serialización, esto ayuda con el warning.
        return converter;
    }

    // Define la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST sin estado (Stateless)
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Permite el acceso sin autenticación a la consola H2
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()

                        // 2. REGISTRO PÚBLICO (POST): Permite a cualquier persona crear un usuario
                        // (Registro)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/usuario")).permitAll()

                       // 3. ENDPOINTS PÚBLICOS DE CATÁLOGO (Categorías y Productos)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/categorias")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/categorias/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/productos")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/productos/**")).permitAll()


                        // ADMINISTRADOR
                        // 4. ENDPOINTS ADMINISTRATIVOS (Categorías) - CRUD
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/categorias/**")).hasRole("ADMINISTRADOR")
                        
                       // 5. ENDPOINTS ADMINISTRATIVOS (Productos) - CRUD + Listar todos
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/productos/admin")).hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/productos")).hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/productos/**")).hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/productos/**")).hasRole("ADMINISTRADOR")

                       // 6. Acciones de Administrador (Usuarios) - CRUD
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/usuarios/**")).hasRole("ADMINISTRADOR")

                       // 7. Todas las demás peticiones requieren autenticación
                        .anyRequest().authenticated())
                        
                // Habilita la autenticación básica
                .httpBasic(httpBasic -> {
                })
                // Configuración para permitir el iframe de la consola H2
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

}
