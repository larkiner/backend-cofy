-- ============================================================
-- FIX: PUT /api/auth/password fallaba para clientes con
--      ORA-01031 (privilegios insuficientes).
--
-- ClienteRepository.actualizarPasswordHash ejecuta:
--     UPDATE CAFETERIA_APP.CLIENTES SET PASSWORD_HASH = ? WHERE ID = ?
-- pero ROL_CLIENTE no tenia el UPDATE sobre esa columna. El resto del
-- modelo ya asume que SOLO puede tocar password_hash (no SELECT a la
-- tabla base), asi que se concede UPDATE a nivel de columna.
--
-- Ejecutar como el DUENO del esquema (CAFETERIA_APP) o un DBA.
-- El GRANT es idempotente: re-ejecutarlo no hace dano.
-- ============================================================

GRANT UPDATE (PASSWORD_HASH) ON CAFETERIA_APP.CLIENTES TO ROL_CLIENTE;

-- Si en tu instalacion los permisos se conceden directamente al usuario
-- de la aplicacion en vez de al rol, usa en su lugar:
-- GRANT UPDATE (PASSWORD_HASH) ON CAFETERIA_APP.CLIENTES TO CAFETERIA_CLIENTE_APP;

-- Verificacion (debe listar UPDATE para CLIENTES / PASSWORD_HASH):
--   SELECT grantee, privilege, column_name
--   FROM   dba_col_privs
--   WHERE  table_name = 'CLIENTES' AND column_name = 'PASSWORD_HASH';
