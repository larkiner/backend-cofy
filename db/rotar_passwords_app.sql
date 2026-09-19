-- ============================================================
-- Rotacion de contrasenas de los usuarios Oracle de la aplicacion.
--
-- Sincroniza las credenciales de la BD con las de api/secrets.properties
-- (o las variables de entorno DB_CLIENTE_PASSWORD / DB_EMPLEADO_PASSWORD
-- en produccion). Ejecutar como un usuario con privilegio ALTER USER
-- (p. ej. SYSTEM) sobre XEPDB1, y luego reiniciar la API.
--
--   sqlplus system/<pwd>@//localhost:1521/XEPDB1 @db/rotar_passwords_app.sql
--
-- IMPORTANTE: las contrasenas aqui DEBEN coincidir EXACTAMENTE con las de
-- secrets.properties. Si cambias una, cambia la otra. No versionar valores
-- reales: este archivo es la plantilla del procedimiento; sustituye los
-- valores por los tuyos antes de ejecutar (o pasalos por variable).
-- ============================================================

-- Oracle XE 21c es MULTITENANT: SYSTEM entra por defecto al CDB root
-- (CDB$ROOT), donde estos usuarios NO existen (ORA-01918). Los usuarios de
-- la app viven en el PDB XEPDB1, asi que lo PRIMERO es cambiarse a el.
-- (Si te conectaste directo al servicio .../XEPDB1, esta linea es inocua.)
ALTER SESSION SET CONTAINER = XEPDB1;   -- ajustar si tu PDB tiene otro nombre

ALTER USER CAFETERIA_CLIENTE_APP  IDENTIFIED BY "tz5eTLWcEQsmGnUfXv2xdscn3f";
ALTER USER CAFETERIA_EMPLEADO_APP IDENTIFIED BY "gwTFw4gi3xY3JnbeziqJnLgk2q";

-- Si los usuarios estaban EXPIRED/LOCKED tras crearse, desbloquear:
-- ALTER USER CAFETERIA_CLIENTE_APP  ACCOUNT UNLOCK;
-- ALTER USER CAFETERIA_EMPLEADO_APP ACCOUNT UNLOCK;

COMMIT;
