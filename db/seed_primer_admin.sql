-- ============================================================
-- BOOTSTRAP DEL PRIMER ADMIN
--
-- Desde que el personal solo lo crea un ADMIN (POST /api/interno/
-- trabajadores), el PRIMER admin hay que sembrarlo a mano: si no,
-- no habria nadie que pueda crear personal (problema del huevo y la
-- gallina).
--
-- Ejecutar UNA vez como el dueno del esquema (CAFETERIA_APP) o un DBA.
--
-- Credenciales que deja creadas:
--   email:      admin@cafeteria.local
--   contrasena: Admin12345   <-- CAMBIALA tras el primer login
--                                 con PUT /api/auth/password
-- El hash es BCrypt (mismo algoritmo que usa la app).
-- ============================================================

-- Asigna al admin la primera sucursal ACTIVA (o cualquiera si no hay
-- activas). No hay que rellenar nada a mano. Para verlas antes:
--    SELECT id, nombre, estado FROM CAFETERIA_APP.SUCURSALES;

INSERT INTO CAFETERIA_APP.TRABAJADORES
        (ID, NOMBRE, EMAIL, TELEFONO, PASSWORD_HASH, ROL, SUCURSAL_ID)
VALUES  (CAFETERIA_APP.SEQ_TRABAJADORES.NEXTVAL,
         'Admin Inicial',
         'admin@cafeteria.local',
         NULL,
         '$2a$10$3PztDPZMnQx7byoSPnH7G.MqFeN2b8x4znBVvTvIK4b9/IrANTJQi',
         'ADMIN',
         COALESCE(
             (SELECT MIN(ID) FROM CAFETERIA_APP.SUCURSALES WHERE ESTADO = 'ACTIVA'),
             (SELECT MIN(ID) FROM CAFETERIA_APP.SUCURSALES)));

COMMIT;

-- Alternativa: si quieres una sucursal concreta, reemplaza el bloque
-- COALESCE(...) por el numero de id, p. ej.  1

-- Nota: si la tabla TRABAJADORES tiene un trigger BEFORE INSERT que
-- asigna el ID automaticamente, quita la columna ID y su NEXTVAL del
-- INSERT. Las columnas FECHA_CONTRATACION y ESTADO se dejan a su valor
-- por defecto (SYSDATE / 'ACTIVO').
