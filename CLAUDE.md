# CLAUDE.md

Este archivo guía a Claude Code (claude.ai/code) cuando trabaja con código en este repositorio.

## Qué es esto

`cafeteria-api` — una API REST en Spring Boot 4.1 / Java 21 para una cadena de cafeterías. Sirve a dos frontends distintos: una app de pedidos para clientes y un panel interno para el personal (pedidos, inventario, turnos, métricas). Usa Oracle como base de datos.

## Comandos

```bash
./gradlew build          # compila + testea + empaqueta
./gradlew bootRun         # levanta la API en :8080
./gradlew test            # corre todos los tests
./gradlew test --tests "com.cafeteria.api.ApiApplicationTests"   # un solo test
```

En Windows fuera de Git Bash, usar `gradlew.bat` en vez de `./gradlew`.

**Antes de arrancar:** la app resuelve los secretos (contraseñas de BD, secreto JWT, claves de registro) desde variables de entorno o desde `./secrets.properties` (gitignored). Sin esos valores **no arranca** (fail-closed). Para desarrollo, copiar `secrets.properties.example` a `secrets.properties` y rellenarlo. `application.properties` ya no contiene secretos, solo referencias `${VAR}`.

Este repo no tiene Flyway/Liquibase — el esquema de Oracle, las vistas, los triggers y los grants por rol se crean con scripts SQL externos (mencionados en comentarios como "Script 4", "Script 6", etc.) que viven fuera de este código. La API asume que ese esquema ya existe. Debe haber una instancia de Oracle accesible en `//localhost:1521/XEPDB1` (ver `application.properties`) para que la app arranque o para tests que toquen algún datasource.

## Arquitectura

### Dos datasources, una sola JVM, frontera de seguridad dura

El hecho arquitectónico central de este código: la app se conecta a Oracle como **dos usuarios de BD distintos**, cada uno con su propio pool de conexiones, `EntityManagerFactory` y `PlatformTransactionManager`, cableados por paquete:

- **`clienteDataSource`** (`ClienteDataSourceConfig`, `@Primary`) — se conecta como `CAFETERIA_CLIENTE_APP`. Es dueño de los paquetes `menu`, `cliente`, `sucursal`, `pedido` (el lado de cara al cliente: ver menú/sucursales, registrarse, gestionar su perfil, crear/pagar sus propios pedidos).
- **`empleadoDataSource`** (`EmpleadoDataSourceConfig`) — se conecta como `CAFETERIA_EMPLEADO_APP`. Es dueño del paquete `interno` (lado del personal: gestión de pedidos, inventario, turnos, métricas, ventas de mostrador).

Cada declaración `@EnableJpaRepositories(basePackages = ...)` fija los repositorios de ese árbol de paquetes al `EntityManagerFactory`/`TransactionManager` correspondiente. **Cualquier repositorio/entidad nueva debe ir en el paquete correcto**, o no quedará cableada al datasource cuyo usuario de BD realmente tiene permiso de tocarla. Los métodos de servicio que escriben deben nombrar explícitamente el transaction manager, ya que ninguno de los dos es el default global fuera de las búsquedas `@Primary`:

```java
@Transactional("clienteTransactionManager")   // o "empleadoTransactionManager"
```

Esto es defensa en profundidad a propósito: aunque el código de la aplicación tenga un bug, los grants de Oracle subyacentes (`ROL_CLIENTE` vs `ROL_EMPLEADO`/`ROL_SUPERVISOR`) bloquean el acceso a tablas fuera de alcance a nivel de BD (`ORA-00942`/`ORA-01031`).

Algunas entidades son proyecciones de solo lectura sobre vistas de Oracle, marcadas `@Immutable` (por ejemplo `PedidoClienteVista` → `VW_PEDIDOS_CLIENTE`, `PedidoEmpleadoVista`). Usar estas para los caminos de lectura en vez de armar joins a mano.

### Autenticación: login unificado, JWT, rol en el claim

`auth/` es código compartido que habla con **ambos** datasources. Está dividido por responsabilidad (SRP): `RegistroService` (alta de cuentas), `SesionService` (login + logout), `PasswordService` (cambio de contraseña) y `CredencialService` — el único lugar que sabe distinguir cliente de trabajador al buscar/verificar credenciales, de modo que agregar un tercer tipo de cuenta solo toca esa clase (OCP). No hay una abstracción de "usuario" de Spring Security más allá de un claim del JWT:

- `JwtService` firma/parsea tokens HS256 con un claim `rol` (nombres de rol: `CLIENTE`, `BARISTA`, `CAJERO`, `SUPERVISOR`, `ADMIN`).
- `JwtAuthFilter` lee `Authorization: Bearer <token>` y, si es válido, deja un `UsernamePasswordAuthenticationToken` en el contexto con una única authority `ROLE_<rol>`. Sin token (o inválido) la petición sigue como anónima, y las rutas protegidas devuelven 401 (no 403) vía el `authenticationEntryPoint` en `SecurityConfig`.
- `POST /api/auth/logout` revoca el token actual: `TokenBlacklistService` mantiene en memoria el `jti` de cada token deslogueado hasta su expiración natural, y `JwtAuthFilter` lo rechaza como inválido a partir de ahí. Es en memoria a propósito (no hay tabla para esto en el esquema externo y la API corre en una sola instancia) — si se despliega con más de una instancia hay que reemplazarlo por un almacén compartido.
- Cambiar la contraseña invalida **todos** los tokens previos de ese usuario: `PasswordService` llama a `InvalidacionSesionService` (marca en memoria un "inválido antes de T" por email) y `JwtAuthFilter` rechaza los tokens con `iat` anterior. Misma limitación de memoria que la blacklist.
- Rate limiting en `/api/auth/**` (`RateLimitingFilter`, ventana fija en memoria por IP) contra fuerza bruta de credenciales y de las claves de registro.
- Errores: `ApiExceptionHandler` (`@RestControllerAdvice`) devuelve un JSON uniforme con el mensaje de negocio y los errores de validación; nunca expone el stack trace. `/error` es público (si no, un error en una petición sin token se enmascara como 401 vacío). El flag `app.pagos.simulacion-habilitada` debe ir en `false` en producción para que el cliente no pueda autoconfirmar su pago.
- Toda la autorización está centralizada en la cadena `authorizeHttpRequests` de `SecurityConfig`, por patrón de URL — **no hay `@PreAuthorize`/`@Secured`** en ninguna parte del código. Al agregar un endpoint nuevo, la regla de acceso se añade ahí, no en el método del controller.
- `Authentication.getName()` en los controllers es el email del usuario; los servicios vuelven a buscar la fila de cliente/trabajador por email en cada llamada en vez de confiar en un principal más rico.

**Regla de registro no obvia** (`RegistroService.registrar`): el destino de un registro se decide comparando la contraseña enviada contra dos valores "mágicos" de configuración, `app.registro.password-empleado` / `app.registro.password-admin`. Si coincide con alguno, se crea silenciosamente un `TRABAJADOR` (BARISTA o ADMIN) en vez de un `CLIENTE`, usando esa misma cadena como su contraseña inicial (hasheada con BCrypt) — se espera que la cambien después vía `PUT /api/auth/password`. Es intencional (documentado en el Javadoc de la clase) pero fácil de romper sin querer al tocar `RegistroService`.

### Dos flujos de pedido que convergen en una sola cola

- **Pedidos en línea** (`pedido/` en el datasource cliente): `PedidoService.crear` → `pagar` (registra un `Pago`, el pedido queda `PENDIENTE_PAGO`) → `confirmarPago` delega en la abstracción `PasarelaPago` (DIP). Hoy la implementa `PasarelaPagoSimulada` (en `interno/pedido/`, datasource **empleado**), que aprueba el pago en el acto; para conectar una pasarela real se agrega otra implementación sin tocar `PedidoService`.
- **Ventas de mostrador** (`interno/venta/`, `VentaMostradorService`, solo datasource empleado): el personal arma el pedido y se cobra en la misma petición — el `PagoVenta` se inserta ya `APROBADO`, sin paso de confirmación aparte.

En ambos casos, un trigger de Oracle (`TRG_PAGOS_APROBADO`) — no código de la aplicación — genera el código de retiro (`codigo_retiro`) y pasa el pedido a `PAGADO` cuando una fila de pago queda `APROBADO`. Después de escribir/aprobar un pago, el código vuelve a leer la fila del pedido en vez de construir en Java el estado post-trigger.

**Convención a respetar**: los precios de cada ítem siempre se releen de la tabla de menú/inventario en el servidor y nunca se confían del cuerpo de la petición (ver los loops de búsqueda de precio en `PedidoService.crear` y `VentaMostradorService.crear`).

### Mapa de paquetes

- `auth/` — login, registro, emisión/validación de JWT (cruza ambos datasources)
- `menu/`, `cliente/`, `sucursal/`, `pedido/` — dominio de cara al cliente, `clienteDataSource`
- `interno/pedido/`, `interno/venta/`, `interno/producto/`, `interno/trabajador/`, `interno/turno/`, `interno/metrica/`, `interno/sucursal/` — dominio de cara al personal, `empleadoDataSource`
- `config/` — las dos configuraciones de datasource más `SecurityConfig`

### Convenciones

- Los DTOs son records de Java bajo un subpaquete `dto/` por feature; se validan con `@Valid` + anotaciones de Jakarta Bean Validation en la frontera del controller.
- Las entidades usan Lombok (`@Getter`/`@Setter` o similar) y anotaciones JPA planas, mapeadas al esquema `CAFETERIA_APP` de Oracle (ver `hibernate.default_schema` en las configs de datasource).
- Los errores de dominio se lanzan como `ResponseStatusException` con mensaje en español directamente desde los servicios (no hay `@ControllerAdvice` global); las búsquedas que no deberían revelar existencia (p. ej. el pedido de otro usuario) devuelven 404 a propósito en vez de 403/401.
- Los comentarios de código y mensajes de error están en español en todo el proyecto; mantener eso al editar archivos existentes.
- El CORS está restringido a `http://localhost:4200` (el frontend Angular de desarrollo) en `SecurityConfig`.
