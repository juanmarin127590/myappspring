package com.myapp.util.config;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.myapp.util.security.CustomUserDetailsService;
import com.myapp.util.security.jwt.JwtAuthenticationFilter;
import com.myapp.util.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring Security
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    // Inyectar UserDetailsService para el AuthenticationManager
    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Exponer el AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomUserDetailsService customUserDetailsService)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST sin estado (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        // ... (Reglas #1 y #2 para /h2-console/ y /api/auth/ siguen igual)

                        // 1. Permite el acceso sin autenticación a la consola H2
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()

                        // 2.ENDPOINTS PÚBLICOS (POST), LOGIN Y CATÁLOGO
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/usuarios")).permitAll()

                        // Categorías (GET) - Permite sólo lectura a TODOS
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/categorias/**"))
                        .permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/productos/**"))
                        .permitAll()

                        // 4. ENDPOINTS ADMINISTRATIVOS (ROLE_ADMINISTRADOR) - PRIORIDAD
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos/admin/**"))
                        .hasRole("ADMINISTRADOR")

                        // Categorías (CRUD)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/categorias/**"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/categorias/**"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/categorias/**"))
                        .hasRole("ADMINISTRADOR")

                        // Productos (CRUD)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/productos"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/productos/**"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/productos/**"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/productos/admin/**"))
                        .hasRole("ADMINISTRADOR") // Si tienes endpoints específicos de admin

                        // Direcciones
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/direcciones/admin/**"))
                        .hasRole("ADMINISTRADOR")

                        // Usuario (CRUD)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/usuarios/**"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/usuarios/**"))
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/usuarios/**"))
                        .hasRole("ADMINISTRADOR")

                        // 3. ENDPOINTS DE USUARIO AUTENTICADO
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/pedidos"))
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/pedidos"))
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/pedidos/{id}"))
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/pedidos/{id}/cancelar"))
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/direcciones/**")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/carrito/**")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pagos/process")).authenticated()

                        // 6. Todas las demás peticiones requieren autenticación
                        .anyRequest().authenticated())

                         // Manejador de accesos denegados para depuración y respuesta clara
                        .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // log simple (usa logger en producción)
                            System.out.println("ACCESS DENIED: " + request.getMethod() + " " + request.getRequestURI()
                                    + " - Reason: " + accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"access_denied\",\"message\":\""
                                    + accessDeniedException.getMessage() + "\"}");
                        }))

        ;

        // Agregar el filtro JWT antes del filtro de Basic/Form Login
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
