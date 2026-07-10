package com.cafeteria.api.config;

import com.cafeteria.api.auth.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Matriz de autorización por rol (el rol viaja en el JWT):
 *
 *  PÚBLICO ......... menú, sucursales, registro y login
 *  CLIENTE ......... su perfil, sus pedidos y pagos
 *  Personal ........ tablero de pedidos, entrega, su perfil y turnos
 *  SUPERVISOR/ADMIN  métricas y creación de turnos
 *  ADMIN ........... inventario (productos y categorías)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] ROLES_PERSONAL =
            {"BARISTA", "CAJERO", "SUPERVISOR", "ADMIN"};

    private final JwtAuthFilter jwtAuthFilter;

    /** Orígenes permitidos del frontend (separados por coma, configurables por entorno). */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API REST sin sesiones de servidor: CSRF no aplica
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---------- Público ----------
                // /error debe ser público: si no, cualquier error (p. ej. una
                // validación 400) en una petición sin token se reenvía a /error,
                // vuelve a pasar por seguridad y se enmascara como 401 sin cuerpo.
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/menu/**", "/api/auth/**", "/api/sucursales/**").permitAll()
                // ---------- Clientes ----------
                .requestMatchers("/api/clientes/**", "/api/pedidos/**").hasRole("CLIENTE")
                // ---------- Solo ADMIN: inventario ----------
                .requestMatchers("/api/interno/productos/**", "/api/interno/categorias/**").hasRole("ADMIN")
                // ---------- Supervisor/Admin: métricas, directorio y asignación de turnos ----------
                .requestMatchers("/api/interno/metricas/**").hasAnyRole("SUPERVISOR", "ADMIN")
                .requestMatchers("/api/interno/trabajadores").hasAnyRole("SUPERVISOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/interno/turnos").hasAnyRole("SUPERVISOR", "ADMIN")
                // ---------- Cualquier rol del personal ----------
                .requestMatchers("/api/interno/**").hasAnyRole(ROLES_PERSONAL)
                // ---------- Resto: autenticado ----------
                .anyRequest().authenticated())
            // Sin token (o inválido) en ruta protegida => 401, no 403
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                (request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado")))
            // Valida el JWT antes del resto de la cadena de seguridad
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** BCrypt: mismo algoritmo con el que se guardan los password_hash. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Permite las peticiones del frontend Angular en desarrollo. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
