package com.cafeteria.api.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Solo INSERT (registro) y UPDATE de password; las lecturas van por las vistas. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Cambio de contraseña sin cargar la entidad: ROL_CLIENTE solo
     * tiene UPDATE sobre la columna password_hash, no SELECT a la tabla.
     */
    @Modifying
    @Transactional("clienteTransactionManager")
    @Query("update Cliente c set c.passwordHash = :hash where c.id = :id")
    void actualizarPasswordHash(@Param("id") Long id, @Param("hash") String hash);
}
