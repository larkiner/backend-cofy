package com.cafeteria.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * Politica minima de robustez de contrasenas, complementaria a la
 * restriccion de longitud (8-72) que ya validan los DTO. Rechaza los
 * patrones triviales que la longitud por si sola no cubre: contrasenas
 * solo numericas, un unico caracter repetido, secuencias obvias y las
 * mas comunes de las filtraciones publicas.
 *
 * Se aplica en el alta de cuentas ({@link RegistroService}) y en el
 * cambio de contrasena ({@link PasswordService}).
 */
final class PoliticaPassword {

    /** Muestra de contrasenas muy comunes (en minusculas). */
    private static final Set<String> COMUNES = Set.of(
            "password", "contrasena", "contraseña", "12345678", "123456789",
            "1234567890", "qwertyui", "qwerty123", "password1", "password123",
            "admin123", "iloveyou", "welcome1", "cafeteria", "aravica",
            "arabica", "coffee123", "changeme", "cambia-esto");

    private PoliticaPassword() {
    }

    /** Lanza 400 si la contrasena no cumple la politica minima. */
    static void validar(String password) {
        String normal = password.toLowerCase();

        if (COMUNES.contains(normal)) {
            throw debil();
        }
        if (password.chars().allMatch(Character::isDigit)) {
            throw debil();
        }
        if (password.chars().distinct().count() <= 2) {
            throw debil(); // p. ej. "aaaaaaaa" o "abababab"
        }
        if (esSecuencia(normal)) {
            throw debil(); // p. ej. "abcdefgh", "87654321"
        }
    }

    /** Detecta secuencias estrictamente ascendentes o descendentes de a 1. */
    private static boolean esSecuencia(String s) {
        boolean asc = true, desc = true;
        for (int i = 1; i < s.length(); i++) {
            int diff = s.charAt(i) - s.charAt(i - 1);
            if (diff != 1) {
                asc = false;
            }
            if (diff != -1) {
                desc = false;
            }
        }
        return asc || desc;
    }

    private static ResponseStatusException debil() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La contraseña es demasiado débil o común; elige una menos predecible");
    }
}
