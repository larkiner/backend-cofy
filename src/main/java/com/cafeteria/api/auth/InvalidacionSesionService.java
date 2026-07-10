package com.cafeteria.api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Invalida en bloque todos los tokens de un usuario emitidos antes de un
 * instante dado (p. ej. al cambiar la contraseña). Complementa a
 * {@link TokenBlacklistService}, que revoca un token concreto (logout).
 *
 * En memoria como el resto de la revocación: cubre el tiempo de vida del
 * proceso, suficiente para la ventana de expiración del token. Para varias
 * instancias o durabilidad tras reinicio, mover a un store compartido.
 */
@Service
public class InvalidacionSesionService {

    private final Map<String, Instant> invalidadosDesde = new ConcurrentHashMap<>();
    private final Duration vidaToken;

    public InvalidacionSesionService(@Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.vidaToken = Duration.ofMillis(expirationMs);
    }

    /** Marca inválido todo token del usuario emitido hasta este momento. */
    public void invalidarSesionesDe(String email) {
        limpiarObsoletos();
        // El 'iat' del JWT tiene resolución de segundos; truncamos el corte
        // al segundo para no rechazar un token reemitido en ese mismo segundo.
        invalidadosDesde.put(normalizar(email), Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }

    /** ¿El token (por su 'issued at') es anterior a la última invalidación? */
    public boolean tokenInvalidado(String email, Date emitidoEn) {
        if (emitidoEn == null) {
            return false;
        }
        Instant desde = invalidadosDesde.get(normalizar(email));
        return desde != null && emitidoEn.toInstant().isBefore(desde);
    }

    /** Una entrada más vieja que la vida de un token ya no invalida a nadie. */
    private void limpiarObsoletos() {
        Instant limite = Instant.now().minus(vidaToken);
        invalidadosDesde.values().removeIf(instante -> instante.isBefore(limite));
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
