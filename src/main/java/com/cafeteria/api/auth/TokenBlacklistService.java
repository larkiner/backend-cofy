package com.cafeteria.api.auth;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista de revocación de JWT (logout). Como las sesiones son STATELESS
 * (sin estado en el servidor, ver SecurityConfig), un JWT firmado sigue
 * siendo válido hasta que expira aunque el usuario haga logout; esta
 * lista en memoria marca su "jti" como revocado mientras dure su vida útil.
 *
 * En memoria a propósito: no hay tabla para esto en el esquema de Oracle
 * (gestionado por scripts externos) y esta API corre en una sola
 * instancia. Si se despliega con más de una instancia, hay que
 * reemplazar esto por un almacén compartido (p. ej. Redis).
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revocados = new ConcurrentHashMap<>();

    public void revocar(String jti, Instant expiracion) {
        limpiarExpirados();
        revocados.put(jti, expiracion);
    }

    public boolean estaRevocado(String jti) {
        Instant expiracion = revocados.get(jti);
        if (expiracion == null) {
            return false;
        }
        if (expiracion.isBefore(Instant.now())) {
            revocados.remove(jti);
            return false;
        }
        return true;
    }

    /** Evita que la lista crezca sin límite con tokens ya vencidos. */
    private void limpiarExpirados() {
        Instant ahora = Instant.now();
        revocados.values().removeIf(expiracion -> expiracion.isBefore(ahora));
    }
}
