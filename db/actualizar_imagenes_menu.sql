-- ============================================================
-- Asigna las fotos de producto a las 5 Bebidas Calientes del menú.
--
-- Las imágenes ya están copiadas al frontend Angular en
-- cafeteria/public/img/menu/*.jpg (esa carpeta se sirve en la raíz,
-- por eso la ruta guardada es /img/menu/<archivo>.jpg — el navegador
-- la resuelve contra el origen de la SPA, sin depender del backend).
--
-- Ejecutar conectado como CAFETERIA_APP (dueño del esquema) o como
-- SYSTEM, en el PDB XEPDB1:
--   sqlplus CAFETERIA_APP/<password>@//localhost:1521/XEPDB1 @db/actualizar_imagenes_menu.sql
-- ============================================================

ALTER SESSION SET CONTAINER = XEPDB1;   -- ajustar si tu PDB tiene otro nombre

UPDATE CAFETERIA_APP.PRODUCTOS SET imagen_url = '/img/menu/espresso.jpg'
 WHERE nombre = 'Espresso';

UPDATE CAFETERIA_APP.PRODUCTOS SET imagen_url = '/img/menu/americano.jpg'
 WHERE nombre = 'Americano';

UPDATE CAFETERIA_APP.PRODUCTOS SET imagen_url = '/img/menu/cappuccino.jpg'
 WHERE nombre = 'Cappuccino';

UPDATE CAFETERIA_APP.PRODUCTOS SET imagen_url = '/img/menu/latte.jpg'
 WHERE nombre = 'Latte';

UPDATE CAFETERIA_APP.PRODUCTOS SET imagen_url = '/img/menu/chocolate-caliente.jpg'
 WHERE nombre = 'Chocolate Caliente';

-- Verificación: deben verse las 5 filas con su nueva ruta.
SELECT nombre, imagen_url FROM CAFETERIA_APP.PRODUCTOS
 WHERE nombre IN ('Espresso', 'Americano', 'Cappuccino', 'Latte', 'Chocolate Caliente');

COMMIT;
