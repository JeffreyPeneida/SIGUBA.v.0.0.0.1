# SIGUBA

Sistema de gestión de denuncias de plagas urbanas del MDMQ.
Angular (frontend) + Spring Boot (backend) + SQL Server.

## Arrancar

```bash
docker compose up -d --build
```

| Servicio   | URL / puerto                      |
|------------|-----------------------------------|
| Frontend   | http://localhost:4200             |
| Backend    | http://localhost:8080/api         |
| SQL Server | `localhost:1434` (sa)             |
| MinIO      | API `localhost:9000`, consola http://localhost:9001 |

El primer arranque tarda unos minutos: descarga la imagen de SQL Server
(~600 MB), compila el backend con Maven y el frontend con npm.

Para cambiar las contraseñas, copiar `.env.example` a `.env`.

```bash
docker compose up -d --build --no-deps frontend   # aplicar cambios solo del frontend
docker compose up -d --build --no-deps backend    # aplicar cambios solo del backend
docker compose logs -f backend    # ver logs
docker compose down               # parar (conserva los datos)
docker compose down -v            # parar y borrar la base
```

## Cómo se carga la base

`docker-compose.yml` levanta estas piezas en orden:

1. **`sqlserver`** — SQL Server 2022. Sólo se publica para amd64, así que en
   Apple Silicon corre emulado (`platform: linux/amd64`). Un *healthcheck* con
   `sqlcmd` marca el contenedor como sano cuando acepta conexiones.
2. **`db-init`** — contenedor de un solo uso que ejecuta
   `BACKEND/docker/init-db.sh`: espera al *healthcheck*, aplica
   `initdb/01-schema.sql` (base, esquema, 28 tablas, datos, 34 FKs, índices) y
   `initdb/02-app-user.sql` (login de aplicación), y verifica el resultado.
   En cada arranque aplica además `04-migraciones.sql`,
   `05-catalogos-demo.sql` (niveles de infestación por especie, tipos de
   evidencia, dependencias, predios...) y `06-perfilamiento.sql` (pantallas y
   permisos por rol). Son idempotentes: sobre una base ya cargada no duplican
   nada ni pisan lo editado en Catálogos o en Perfiles.
3. **`minio`** y **`minio-init`** — almacén de archivos (S3). `minio-init` crea
   el bucket `siguba` una sola vez. Las imágenes de denuncias, inspecciones y
   ubicaciones viven aquí; la base sólo guarda su ruta (`RUTA_IMAGEN`).
4. **`backend`** — arranca sólo cuando `db-init` y `minio-init` terminan con
   éxito (`service_completed_successfully`).

`01-schema.sql` se **genera** a partir de `bd_uba - sql_server.sql`; no se
edita a mano. El dump original no se podía ejecutar tal cual, ver abajo.

## Desarrollo sin Docker

```bash
# base de datos en Docker, aplicaciones en local
docker compose up -d sqlserver db-init

cd BACKEND  && mvn spring-boot:run                    # :8080
cd FRONTEND && npm install && npm start -- --proxy-config proxy.conf.json   # :4200
```

`application.properties` ya apunta a `localhost:1434` con el usuario `siguba`,
que es el que crea `02-app-user.sql`.

## Configuración

El backend se configura por variables de entorno estándar de Spring, que
sobrescriben `application.properties`:

| Variable                       | En Docker                            |
|--------------------------------|--------------------------------------|
| `SPRING_DATASOURCE_URL`        | `jdbc:sqlserver://sqlserver:1433;...`|
| `SPRING_DATASOURCE_USERNAME`   | `siguba`                             |
| `SPRING_DATASOURCE_PASSWORD`   | `Siguba_Dev123*`                     |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`| `none`                               |

`ddl-auto=none` es deliberado: el esquema lo define `01-schema.sql`, no
Hibernate.

| `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` | `http://minio:9000`, `siguba`, ... |
| `MINIO_PUBLIC_URL`             | `http://localhost:9000` (la que usa el navegador) |

El frontend llama a `/api` en su propio origen. En Docker lo reenvía nginx
(`FRONTEND/nginx.conf`) y con `ng serve` lo hace `proxy.conf.json`. Así el
bundle no lleva host ni puerto embebidos y no hace falta CORS.

## Usuarios de prueba

El dump original no traía ningún usuario, así que no había forma de entrar.
`BACKEND/docker/initdb/03-usuarios-demo.sql` crea estos al inicializar la base:

| Usuario   | Contraseña    | Rol     | Entra a    |
|-----------|---------------|---------|------------|
| `admin`   | `Admin2026`   | ADMIN   | `/gui`     |
| `tecnico` | `Tecnico2026` | TECNICO | `/tecnico` |
| `usuario` | `Usuario2026` | USUARIO | `/usuario` |
| `atorres` | `Tecnico2026` | TECNICO | `/tecnico` |

Un tecnico es un usuario con rol `TECNICO`: su ficha de `UBA_TECNICO` (a donde
apunta la clave foranea de `UBA_TRAMITE`) se crea y se mantiene sola a partir de
la cuenta. No hay que darlo de alta dos veces, y ya no existe el login aparte
que tenia `UBA_TECNICO`.

Las contraseñas se siembran legibles pero **la aplicación las cifra con BCrypt
al arrancar** (`PasswordMigrationRunner`), así que en la base nunca quedan en
claro. Aun así son contraseñas conocidas: **sólo para desarrollo local**. Para no
crearlos, `SEED_DEMO_USERS=0 docker compose up -d`.

## Arquitectura

```
FRONTEND (Angular 22)                BACKEND (Spring Boot 3.3)
  src/app/                             gob/mdmq/siguba/
    core/                                Controller/   endpoints REST
      api.service.ts   único punto        Service/      lógica de negocio
                       de salida HTTP     Repository/   acceso a datos (JPA)
      auth.service.ts  sesión y rol       Entidades/    modelo persistente
      auth.guard.ts    guards de ruta     dto/          contratos de la API
      auth.models.ts   tipos              Config/       CORS, Jackson, datasource
    LOGIN/ GUI/ TECNICO/ ...              Exception/    manejo global de errores
```

**Autenticación.** La aplicación abre en `/login`. `AuthService` guarda la
sesión en `sessionStorage` y expone usuario y rol como signals.
`app.routes.ts` protege cada ruta:

| Guard           | Efecto                                                      |
|-----------------|-------------------------------------------------------------|
| `authGuard`     | exige sesión; si no, devuelve a `/login`                     |
| `permisoGuard(...)` | exige permiso sobre la pantalla (ver Perfilamiento)     |
| `invitadoGuard` | evita volver al login con la sesión ya abierta                |

Tras iniciar sesión cada rol entra a su pantalla: ADMIN a `/gui`, TECNICO a
`/tecnico`, USUARIO a `/usuario`.

**Perfilamiento.** Los roles son fijos (`ADMIN`, `TECNICO`, `USUARIO`, enum
`RolUsuario`); lo configurable es qué pantallas ve cada uno y qué puede hacer
en ellas (ver, crear, editar, eliminar). Se edita en **Perfiles y permisos**
(`/perfiles`) y vive en `UBA_PANTALLA` y `UBA_ROL_PERMISO`.

- **Backend (la puerta real).** Cada endpoint declara el permiso que exige:
  `@PreAuthorize("@permisos.puede('catalogos', 'CREAR')")`. `PermisoService`
  cachea la matriz y la invalida al guardar, así que un cambio aplica en la
  siguiente petición. Las fichas de trámite (`/tramite/{id}`, imágenes...)
  usan `@accesoTramite.puedeVer(#id)`: quien gestiona ve cualquiera; el resto,
  solo las suyas (registradas por él o con su cédula).
- **Frontend.** `PermisosService` carga `GET /api/permisos/mios`; con eso se
  arma el menú, `permisoGuard('pantalla', 'ACCION')` protege las rutas y se
  muestran u ocultan los botones.
- El ADMIN no puede perder ver/editar Perfiles (lo impide el servidor).
- **Menú lateral** (pestaña de Perfiles): nombre, icono, sección, orden y
  visibilidad de cada pantalla (`UBA_PANTALLA`). *Oculta del menú* sigue
  abriéndose por su ruta si el rol tiene permiso; *desactivada* no da acceso a
  nadie, ni en el menú ni en la API. Perfiles no se puede ocultar ni
  desactivar. `06-perfilamiento.sql` solo sincroniza ruta y acciones, así que
  no pisa lo editado aquí.

| Pantalla            | Ruta                  | ADMIN              | TECNICO     | USUARIO     |
|---------------------|-----------------------|--------------------|-------------|-------------|
| Inicio              | `/gui`                | ver                | ver         | ver         |
| Nueva denuncia      | `/registrar-denuncia` | ver, crear         | ver, crear  | ver, crear  |
| Mis denuncias       | `/usuario`            | —                  | —           | ver         |
| Control de plagas   | `/control-plagas`     | ver, editar, elim. | ver         | —           |
| Inspecciones        | `/tecnico`            | ver, crear         | ver, crear  | —           |
| Usuarios            | `/gestionar-usuarios` | todo               | —           | —           |
| Técnicos            | `/gestionar-tecnicos` | ver, editar, elim. | —           | —           |
| Catálogos           | `/catalogos`          | todo               | —           | —           |
| Perfiles y permisos | `/perfiles`           | ver, editar        | —           | —           |

Son los valores iniciales de `06-perfilamiento.sql` y reproducen el acceso que
la aplicación tenía escrito en código.

**Sesión y cuentas.** El login emite un JWT (HS256), pero el filtro toma el rol
y el estado **de la base** (caché de 30 s, invalidada al cambiar un usuario):
desactivar una cuenta la saca en la siguiente petición y un cambio de rol
aplica sin volver a entrar. En **Usuarios** se filtra por Activos / Inactivos /
Todos y se desactiva o reactiva una cuenta; nadie puede desactivarse a sí mismo
ni cambiar su propio rol. Desactivar es una baja lógica (`ESTADO = 'INACTIVO'`).

Sin token la API responde `401`; sin permiso, `403`. El registro público
(`/api/crearUsuario`) siempre da de alta `USUARIO`, salvo que lo haga alguien
con permiso de crear en Usuarios.

**Errores.** `GlobalExceptionHandler` traduce las excepciones a códigos HTTP
coherentes (400, 401, 404, 409, 500) y deja el detalle técnico en el log en vez
de en la respuesta. Antes cada controlador atrapaba `Exception` por su cuenta y
devolvía mensajes como *"Transaction silently rolled back"*, o un 201 aunque la
operación hubiese fallado.

**Login.** `POST /api/login` recibe las credenciales en el cuerpo. La variante
`GET /api/login/{usuario}/{password}` sigue existiendo por compatibilidad, pero
deja la contraseña en los logs de acceso, en el historial del navegador y en
cualquier proxy intermedio: no usarla.

## Qué se corrigió

**El dump `bd_uba - sql_server.sql` no se podía ejecutar.** Además de faltarle
`CREATE DATABASE`, `CREATE SCHEMA` y los separadores `GO`:

- 7 `;` sueltos a mitad del `INSERT` de `UBA_BARRIO` cortaban la sentencia y
  dejaban miles de tuplas huérfanas.
- Una tupla sin `(` de apertura (`'LA ARMENIA 1',1),`) truncaba otras ~1.400
  filas. Recuperadas las 3.814 en total.
- Un `INSERT ... VALUES` de 3.814 filas supera el límite de 1.000 de SQL
  Server; ahora se emite en lotes de 900.
- `UBA_BARRIO` declaraba una FK a `UBA_PARROQUIA` 6.000 líneas antes de crearla.
  Todas las FKs se aplican ahora al final, con `ALTER TABLE`.
- FKs que apuntaban a tablas inexistentes: `TRAMITE`, `UBA_UBICACION`,
  `UBA_ESPECIE`.

**Los nombres de tabla no coincidían con las entidades JPA.** Las 27 `@Table`
usan el prefijo `UBA_`, pero 20 tablas del dump no lo tenían (`USUARIO`,
`TRAMITE`, `INSPECCION`, `TECNICO`, ...). Con `ddl-auto=none` eso significaba
que ninguna consulta encontraría su tabla.

**Faltaban columnas.** `UBA_USUARIO` no tenía 17 de las que mapea la entidad
`Usuario` (`SSO_ID`, `TIPO_USUARIO`, `ESTADO`, auditoría...), y su
`ADMIN_ZONAL` era `VARCHAR(100)` cuando la entidad lo mapea como `@JoinColumn`
(FK entera). A `UBA_TRAMITE` le faltaba `ESTADO`.

**Bugs de código que impedían arrancar Spring** (Hibernate valida las consultas
al iniciar):

- `TramiteRepository`: `t.idRegistra` no existe; el campo es `t.usuario`.
- `TecnicoRepository`: `t.usuario` → `t.tecnicoUsuario`, `t.mail` → `t.correo`,
  en JPQL, en las expresiones SpEL y en dos métodos derivados.
- `InspeccionRepository`: `findByIdCodigoInterno` / `existsByIdCodigoInterno`
  se resolvían contra `Inspeccion`, que no tiene ese campo. Nadie los invocaba
  y `TramiteRepository` ya cubre esa búsqueda; se retiraron.
- `Usuario.estado` declaraba `length = 3` pero el código escribe `'ACTIVO'` e
  `'INACTIVO'`.

**Mejoras aplicadas a la base:**

- Los barrios venian duplicados: 3.814 filas para solo 2.149 pares
  (nombre, parroquia), algunos repetidos hasta 4 veces. Se conserva la primera
  aparicion de cada par y un `UNIQUE (NOMBRE, ID_PARROQUIA)` impide que vuelva
  a ocurrir.
- SQL Server indexa las claves primarias pero **no** las foraneas: sin indice,
  cada JOIN y cada borrado en cascada recorria la tabla hija entera. Se crea un
  indice por cada una de las 34 FKs.
- Las columnas `ESTADO` eran texto libre. Ahora un `CHECK` acepta solo
  `ACTIVO`, `INACTIVO` o NULL, que es lo unico que escribe el codigo.

**Infraestructura:**

- `pom.xml` dependía de `mssql-jdbc_auth` (`.dll` de autenticación integrada de
  Windows), que no resuelve en Linux y rompía el build del contenedor.
- El `Dockerfile` usaba `maven:3.9.6-eclipse-temurin-25` y
  `eclipse-temurin:25-jre-alpine`, que no existen; el proyecto es Java 17.
  Temurin 17 tampoco publica Alpine para arm64, así que se usa
  `eclipse-temurin:17-jre`.
- El `docker-compose.yml` anterior no creaba la base ni cargaba el esquema, y
  dejaba `ddl-auto=update`, con lo que Hibernate habría intentado generar un
  esquema paralelo.

## Pendiente

**La inspección no se puede guardar.** `TECNICO/reginspeccion.ts` envía
`POST /api/guardar-inspeccion`, pero el backend no expone ese endpoint: la
pantalla responde 404 al guardar.

**`services/consulta.service.ts` está sin uso.** Ningún componente lo importa y
apunta a rutas que no existen (`/api/tecnicos`, `/api/tramites`, `/api/areas`,
`/api/motivos`, `/api/rodenticidas`, `/api/niveles/{id}`, `/api/inspecciones`,
`/api/imagenes`). Los catálogos equivalentes ya están en `CatalogoController`
y las imágenes en `/api/archivos`; puede retirarse.

**`UBA_TECNICO` conserva columnas de login muertas** (`TECNICO_USUARIO`,
`PASSWORD`). La autenticación se unificó en `UBA_USUARIO`: los técnicos son
usuarios con `rol = TECNICO`, y `UBA_TECNICO` queda como el registro profesional
al que apuntan los trámites. Esas dos columnas pueden retirarse.

**El token no se renueva.** Caduca a las 8 horas y obliga a volver a entrar; no
hay refresh token.

## Endpoints

| Controlador                | Rutas                                                        |
|----------------------------|--------------------------------------------------------------|
| `UsuarioController`        | `POST /api/login`, `/api/usuarios`, `/api/crearUsuario`, `/api/actualizarUsuario/{id}`, ... |
| `CatalogoController`       | `GET /api/admin-zonal`, `/parroquia/{zona}`, `/barrio/{parroquia}`, `/tipo-denunciante`, `/dependencia`, `/predio`, `/especie`, `/area-inspeccion`, `/motivo-inspeccion`, `/nivel-infestacion/{idEspecie}`, `/tipo-rodenticida`, `/tipo-evidencia` |
| `CatalogoAdminController`  | `GET/POST /api/catalogos/{clave}`, `PUT/DELETE /api/catalogos/{clave}/{id}` |
| `DenunciaController`       | `GET /api/tramite/codigo-preview`, `POST /api/denuncia` (multipart) |
| `TramiteController`        | `/api/tramite/*` (buscar, asignar técnico, actualizar, eliminar, restaurar) |
| `DetalleTramiteController` | `GET /api/tramite/{id}`, `/tramite-especie/{id}`, `/imagen-denuncia/{id}`, `/imagen-inspeccion/{id}` |
| `TecnicoController`        | `GET /api/tecnico`                                           |
| `ArchivoController`        | `POST /api/archivos/{carpeta}`, `GET /api/archivos/**`, `DELETE /api/archivos` |
| `AdminZonalController`, `AreaInspeccionController` | `/api/adminzonal/*`, `/api/areainspeccion/*` (rutas antiguas) |
