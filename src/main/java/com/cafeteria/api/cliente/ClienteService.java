package com.cafeteria.api.cliente;

import com.cafeteria.api.cliente.dto.ActualizarClienteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Perfil del cliente autenticado. Las lecturas van por la vista
 * VW_CLIENTE_PERFIL (ROL_CLIENTE no puede SELECT la tabla base); las
 * escrituras usan un UPDATE por columna en ClienteRepository.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClientePerfilRepository clientePerfilRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(value = "clienteTransactionManager", readOnly = true)
    public ClientePerfil miPerfil(String email) {
        return buscarPerfil(email);
    }

    /**
     * Actualiza nombre y teléfono del cliente. El id se obtiene de la vista
     * (no hay SELECT a la tabla); tras el UPDATE se relee el perfil para
     * devolver los datos ya actualizados.
     */
    @Transactional("clienteTransactionManager")
    public ClientePerfil actualizarDatos(String email, ActualizarClienteRequest request) {
        ClientePerfil perfil = buscarPerfil(email);
        clienteRepository.actualizarDatos(perfil.getId(), request.nombre().trim(), request.telefono());
        return buscarPerfil(email);
    }

    private ClientePerfil buscarPerfil(String email) {
        return clientePerfilRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Perfil no encontrado"));
    }
}
