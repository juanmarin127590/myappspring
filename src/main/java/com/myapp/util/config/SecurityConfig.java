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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 💥 JWT es stateless
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Permite el acceso sin autenticación a la consola H2
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()

                        // 2. REGISTRO PÚBLICO (POST): Permite a cualquier persona crear un usuario
                        // (Registro)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll() // para Login y otros de autenticación
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/usuario")).permitAll()

                       // 3. ENDPOINTS PÚBLICOS DE CATÁLOGO (Categorías y Productos)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/categorias/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/productos/**")).permitAll()

                        // 4. ENDPOINTS DE USUARIO AUTENTICADO
                        // (Todos los demás endpoints de Direcciones, Pedidos, Carrito, etc., deben ser .authenticated() o .hasRole())
                        // Gestión de Direcciones (CRUD)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/direcciones/**")).authenticated()

                        // Gestión de Pedidos (CRUD)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos/**")).authenticated()
                
                        // Gestión de Carrito de Compras (CRUD) - USUARIO AUTENTICADO
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/carrito/**")).authenticated()
                
                        // Gestión de Pedidos (CRUD) - USUARIO AUTENTICADO
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos")).authenticated() // POST y GET de historial
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos/**")).authenticated() // GET detalle, PUT cancelar

                        // 5. ENDPOINTS ADMINISTRATIVOS (ROLE_ADMINISTRADOR)
                        // La gestión administrativa de cualquier recurso debe ser para ROLE_ADMINISTRADOR

                        //Categorías
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/categorias/**")).hasRole("ADMINISTRADOR")
                        
                        // Productos
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/productos/admin")).hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/productos")).hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/productos/**")).hasRole("ADMINISTRADOR")
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/productos/**")).hasRole("ADMINISTRADOR")

                        // Direcciones (Administrador)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/direcciones/admin/**")).hasRole("ADMINISTRADOR")

                        // Pedidos (Administrador) - Listar todos y actualizar estado
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/pedidos/admin/**")).hasRole("ADMINISTRADOR")

                       // Usuarios
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/usuarios/**")).hasRole("ADMINISTRADOR")

                       // 6. Todas las demás peticiones requieren autenticación
                        .anyRequest().authenticated())
                        
                // Habilita la autenticación básica
                .httpBasic(httpBasic -> {})
                // Configuración para permitir el iframe de la consola H2
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        // Agregar el filtro JWT antes del filtro de Basic/Form Login
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);        

        return http.build();
    }

}
