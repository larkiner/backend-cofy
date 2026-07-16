-- ============================================================
-- FIX: PUT /api/clientes/me (actualizar datos del cliente) falla
--      con ORA-01031 (privilegios insuficientes) sin este grant.
--
-- ClienteService.actualizarDatos ejecuta, via ClienteRepository:
--     UPDATE CAFETERIA_APP.CLIENTES SET NOMBRE = ?, TELEFONO = ? WHERE ID = ?
-- pero ROL_CLIENTE solo tenia UPDATE sobre PASSWORD_HASH (ver
-- fix_grant_update_password_cliente.sql). El modelo asume que el cliente
-- NO puede SELECT la tabla base, solo UPDATE columna por columna, asi que
-- se concede UPDATE sobre las columnas editables del perfil.
--
-- Ejecutar como el DUENO del esquema (CAFETERIA_APP) o un DBA.
-- El GRANT es idempotente: re-ejecutarlo no hace dano.
-- ============================================================

GRANT UPDATE (NOMBRE, TELEFONO) ON CAFETERIA_APP.CLIENTES TO ROL_CLIENTE;

-- Si en tu instalacion los permisos se conceden directamente al usuario
-- de la aplicacion en vez de al rol, usa en su lugar:
-- GRANT UPDATE (NOMBRE, TELEFONO) ON CAFETERIA_APP.CLIENTES TO CAFETERIA_CLIENTE_APP;

-- Verificacion (debe listar UPDATE para CLIENTES / NOMBRE y TELEFONO):
--   SELECT grantee, privilege, column_name
--   FROM   dba_col_privs
--   WHERE  table_name = 'CLIENTES' AND column_name IN ('NOMBRE', 'TELEFONO');
