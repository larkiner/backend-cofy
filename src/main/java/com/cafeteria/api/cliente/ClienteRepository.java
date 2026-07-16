package com.cafeteria.api.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Solo INSERT (registro) y UPDATE de password/datos; las lecturas van por las vistas. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Cambio de contraseña sin cargar la entidad: ROL_CLIENTE solo
     * tiene UPDATE sobre la columna password_hash, no SELECT a la tabla.
     */
    @Modifying
    @Transactional("clienteTransactionManager")
    @Query("update Cliente c set c.passwordHash = :hash where c.id = :id")
    void actualizarPasswordHash(@Param("id") Long id, @Param("hash") String hash);

    /**
     * Actualiza los datos editables del cliente sin cargar la entidad
     * (misma razón que arriba: ROL_CLIENTE no tiene SELECT sobre la tabla,
     * solo UPDATE por columna — ver db/fix_grant_update_datos_cliente.sql).
     * Limpia el contexto para que la relectura por la vista devuelva lo nuevo.
     */
    @Modifying(clearAutomatically = true)
    @Transactional("clienteTransactionManager")
    @Query("update Cliente c set c.nombre = :nombre, c.telefono = :telefono where c.id = :id")
    int actualizarDatos(@Param("id") Long id,
                        @Param("nombre") String nombre,
                        @Param("telefono") String telefono);
}
