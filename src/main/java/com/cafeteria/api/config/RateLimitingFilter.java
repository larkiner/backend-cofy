package com.cafeteria.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting simple (ventana fija en memoria) para los endpoints de
 * autenticación. Frena la fuerza bruta de credenciales y el intento de
 * adivinar las claves de invitación empleado/admin del registro.
 *
 * En memoria a propósito, coherente con el resto del proyecto (una sola
 * instancia). Para varias instancias, sustituir por un contador
 * compartido (Redis) o un rate limiter en el gateway.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RUTA_PROTEGIDA = "/api/auth/";
    /** 429 Too Many Requests (no está en las constantes del API servlet). */
    private static final int SC_TOO_MANY_REQUESTS = 429;
    private static final int MAX_SOLICITUDES = 10;
    private static final Duration VENTANA = Duration.ofMinutes(1);
    /** Cota de memoria: nº máximo de IPs rastreadas a la vez. */
    private static final int MAX_IPS = 10_000;

    private record Contador(Instant inicioVentana, int conteo) {
    }

    private final Map<String, Contador> contadores = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(RUTA_PROTEGIDA);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (excedeLimite(request.getRemoteAddr())) {
            response.setStatus(SC_TOO_MANY_REQUESTS);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Demasiadas solicitudes. Espera un momento e inténtalo de nuevo.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean excedeLimite(String ip) {
        Instant ahora = Instant.now();
        if (contadores.size() > MAX_IPS) {
            contadores.values().removeIf(c -> ventanaExpirada(c, ahora));
        }

        Contador actualizado = contadores.compute(ip, (clave, actual) -> {
            if (actual == null || ventanaExpirada(actual, ahora)) {
                return new Contador(ahora, 1);
            }
            return new Contador(actual.inicioVentana(), actual.conteo() + 1);
        });

        return actualizado.conteo() > MAX_SOLICITUDES;
    }

    private static boolean ventanaExpirada(Contador contador, Instant ahora) {
        return contador.inicioVentana().plus(VENTANA).isBefore(ahora);
    }
}
