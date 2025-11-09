package com.myapp.util.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring Security
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // Inyectar UserDetailsService para el AuthenticationManager
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    // Nuevo Bean para el filtro JWT
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    // Exponer el AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST sin estado (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT es stateless
                .authorizeHttpRequests(authorize -> authorize
                        // ... (Reglas #1 y #2 para /h2-console/ y /api/auth/ siguen igual)

                    // 1. Permite el acceso sin autenticación a la consola H2
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()

                    // 2. REGISTRO PÚBLICO (POST) y LOGIN
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll() 
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/usuarios")).permitAll()

                    // ==========================================================
                    // SECCIÓN CORREGIDA: PRIORIZAR REGLAS RESTRICTIVAS Y ESPECIFICAR MÉTODOS
                    // ==========================================================
                    
                    // 3. ENDPOINTS ADMINISTRATIVOS (ROLE_ADMINISTRADOR) - PRIORIDAD 
                    
                    // Categorías (POST, PUT, DELETE) 
                    // Se usa un comodín general y luego se permite GET a todos
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/categorias/**")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/categorias/**")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/categorias/**")).hasRole("ADMINISTRADOR")
                    
                    // Productos (CRUD) - Usar matchers exactos para CRUD, y dejar GET para la sección pública.
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/productos")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/productos/**")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/productos/**")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/productos/admin/**")).hasRole("ADMINISTRADOR") // Si tienes endpoints específicos de admin

                    // Direcciones, Pedidos, Usuarios (Administrador) - Cualquier método
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/direcciones/admin/**")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos/admin/**")).hasRole("ADMINISTRADOR")
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/usuarios/**")).hasRole("ADMINISTRADOR")
                    
                    // 4. ENDPOINTS PÚBLICOS DE CATÁLOGO (Solo Lectura) - PRIORIDAD 
                    
                    // Categorías (GET) - Permite sólo lectura a TODOS
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/categorias/**")).permitAll() 
                    
                    // Productos (GET) - Permite sólo lectura a TODOS
                    .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/productos/**")).permitAll()
                    
                    // 5. ENDPOINTS DE USUARIO AUTENTICADO - PRIORIDAD  (El resto de la lógica de negocio)
                    
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/direcciones/**")).authenticated()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos/**")).authenticated()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/carrito/**")).authenticated()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pagos/process")).authenticated()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos")).authenticated()
                    
                    // 6. Todas las demás peticiones requieren autenticación
                    .anyRequest().authenticated())
                        
                // Habilita la autenticación básica
                //.httpBasic(httpBasic -> {})
                // Configuración para permitir el iframe de la consola H2
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        // Agregar el filtro JWT antes del filtro de Basic/Form Login
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);        

        return http.build();
    }

}
