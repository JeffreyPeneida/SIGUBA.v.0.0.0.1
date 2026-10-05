/* ============================================================
   Perfilamiento: que pantallas ve cada rol y que puede hacer en
   ellas (ver, crear, editar, eliminar).

   Los roles siguen siendo los tres del enum RolUsuario (ADMIN,
   TECNICO, USUARIO); lo configurable es su acceso. Antes ese
   acceso estaba escrito a mano en el menu, en las rutas y en cada
   controlador, y cambiarlo exigia tocar codigo en los tres sitios.

   Se aplica SIEMPRE y es idempotente:
   - UBA_PANTALLA: se insertan las que falten y se sincronizan RUTA y
     ACCIONES, que dependen del codigo. Nombre, icono, seccion, orden y
     visibilidad los edita el administrador (Perfiles > Menu lateral) y no
     se pisan.
   - UBA_ROL_PERMISO solo recibe las filas que faltan: lo que el
     administrador haya cambiado en "Perfiles" no se pisa.
   ============================================================ */
USE MDMQ_SIGUBA;
GO

SET NOCOUNT ON;
GO

IF OBJECT_ID('PROYECTO_UBA.UBA_PANTALLA', 'U') IS NULL
BEGIN
    CREATE TABLE PROYECTO_UBA.UBA_PANTALLA (
        CLAVE     VARCHAR(40)  NOT NULL,
        NOMBRE    VARCHAR(80)  NOT NULL,
        RUTA      VARCHAR(80)  NOT NULL,
        ICONO     VARCHAR(40)  NOT NULL,
        SECCION   VARCHAR(40)  NOT NULL,
        ORDEN     INT          NOT NULL,
        -- Acciones que tienen sentido en la pantalla, separadas por coma.
        ACCIONES  VARCHAR(60)  NOT NULL,
        CONSTRAINT PK_UBA_PANTALLA PRIMARY KEY (CLAVE)
    );
    PRINT '>> UBA_PANTALLA creada';
END
GO

IF OBJECT_ID('PROYECTO_UBA.UBA_ROL_PERMISO', 'U') IS NULL
BEGIN
    CREATE TABLE PROYECTO_UBA.UBA_ROL_PERMISO (
        ROL       VARCHAR(20) NOT NULL,
        PANTALLA  VARCHAR(40) NOT NULL,
        VER       BIT NOT NULL DEFAULT 0,
        CREAR     BIT NOT NULL DEFAULT 0,
        EDITAR    BIT NOT NULL DEFAULT 0,
        ELIMINAR  BIT NOT NULL DEFAULT 0,
        CONSTRAINT PK_UBA_ROL_PERMISO PRIMARY KEY (ROL, PANTALLA),
        CONSTRAINT FK_UBA_ROL_PERMISO_PANTALLA
            FOREIGN KEY (PANTALLA) REFERENCES PROYECTO_UBA.UBA_PANTALLA (CLAVE),
        CONSTRAINT CK_UBA_ROL_PERMISO_ROL CHECK (ROL IN ('ADMIN', 'TECNICO', 'USUARIO'))
    );
    PRINT '>> UBA_ROL_PERMISO creada';
END
GO

-- Visibilidad en el menu y pantalla activa: editables desde Perfiles.
IF COL_LENGTH('PROYECTO_UBA.UBA_PANTALLA', 'EN_MENU') IS NULL
BEGIN
    ALTER TABLE PROYECTO_UBA.UBA_PANTALLA ADD EN_MENU BIT NOT NULL
        CONSTRAINT DF_UBA_PANTALLA_EN_MENU DEFAULT 1;
    PRINT '>> UBA_PANTALLA.EN_MENU agregada';
END
GO

IF COL_LENGTH('PROYECTO_UBA.UBA_PANTALLA', 'ACTIVA') IS NULL
BEGIN
    ALTER TABLE PROYECTO_UBA.UBA_PANTALLA ADD ACTIVA BIT NOT NULL
        CONSTRAINT DF_UBA_PANTALLA_ACTIVA DEFAULT 1;
    PRINT '>> UBA_PANTALLA.ACTIVA agregada';
END
GO

/* ------------------------------------------------------- pantallas */
MERGE PROYECTO_UBA.UBA_PANTALLA AS destino
USING (VALUES
    ('inicio',             'Inicio',              '/gui',                'fa-house',            'Operación',      10, 'VER'),
    ('registrar-denuncia', 'Nueva denuncia',      '/registrar-denuncia', 'fa-file-circle-plus', 'Operación',      20, 'VER,CREAR'),
    ('mis-denuncias',      'Mis denuncias',       '/usuario',            'fa-folder-open',      'Operación',      30, 'VER'),
    ('control-plagas',     'Control de plagas',   '/control-plagas',     'fa-bug',              'Operación',      40, 'VER,EDITAR,ELIMINAR'),
    ('inspecciones',       'Inspecciones',        '/tecnico',            'fa-helmet-safety',    'Operación',      50, 'VER,CREAR'),
    ('usuarios',           'Usuarios',            '/gestionar-usuarios', 'fa-users',            'Administración', 60, 'VER,CREAR,EDITAR,ELIMINAR'),
    ('tecnicos',           'Técnicos',            '/gestionar-tecnicos', 'fa-user-gear',        'Administración', 70, 'VER,EDITAR,ELIMINAR'),
    ('catalogos',          'Catálogos',           '/catalogos',          'fa-layer-group',      'Administración', 80, 'VER,CREAR,EDITAR,ELIMINAR'),
    ('perfiles',           'Perfiles y permisos', '/perfiles',           'fa-user-shield',      'Administración', 90, 'VER,EDITAR')
) AS origen (CLAVE, NOMBRE, RUTA, ICONO, SECCION, ORDEN, ACCIONES)
ON destino.CLAVE = origen.CLAVE
WHEN MATCHED THEN UPDATE SET
    RUTA = origen.RUTA, ACCIONES = origen.ACCIONES
WHEN NOT MATCHED THEN INSERT (CLAVE, NOMBRE, RUTA, ICONO, SECCION, ORDEN, ACCIONES)
    VALUES (origen.CLAVE, origen.NOMBRE, origen.RUTA, origen.ICONO, origen.SECCION, origen.ORDEN, origen.ACCIONES);
GO

/* --------------------------------------------- permisos por defecto */
-- Reproducen el acceso que la aplicacion tenia escrito en codigo.
INSERT INTO PROYECTO_UBA.UBA_ROL_PERMISO (ROL, PANTALLA, VER, CREAR, EDITAR, ELIMINAR)
SELECT v.ROL, v.PANTALLA, v.VER, v.CREAR, v.EDITAR, v.ELIMINAR
  FROM (VALUES
    --  rol        pantalla              ver crear editar eliminar
    ('ADMIN',   'inicio',             1, 0, 0, 0),
    ('ADMIN',   'registrar-denuncia', 1, 1, 0, 0),
    ('ADMIN',   'mis-denuncias',      0, 0, 0, 0),
    ('ADMIN',   'control-plagas',     1, 0, 1, 1),
    ('ADMIN',   'inspecciones',       1, 1, 0, 0),
    ('ADMIN',   'usuarios',           1, 1, 1, 1),
    ('ADMIN',   'tecnicos',           1, 0, 1, 1),
    ('ADMIN',   'catalogos',          1, 1, 1, 1),
    ('ADMIN',   'perfiles',           1, 0, 1, 0),

    ('TECNICO', 'inicio',             1, 0, 0, 0),
    ('TECNICO', 'registrar-denuncia', 1, 1, 0, 0),
    ('TECNICO', 'mis-denuncias',      0, 0, 0, 0),
    ('TECNICO', 'control-plagas',     1, 0, 0, 0),
    ('TECNICO', 'inspecciones',       1, 1, 0, 0),
    ('TECNICO', 'usuarios',           0, 0, 0, 0),
    ('TECNICO', 'tecnicos',           0, 0, 0, 0),
    ('TECNICO', 'catalogos',          0, 0, 0, 0),
    ('TECNICO', 'perfiles',           0, 0, 0, 0),

    ('USUARIO', 'inicio',             1, 0, 0, 0),
    ('USUARIO', 'registrar-denuncia', 1, 1, 0, 0),
    ('USUARIO', 'mis-denuncias',      1, 0, 0, 0),
    ('USUARIO', 'control-plagas',     0, 0, 0, 0),
    ('USUARIO', 'inspecciones',       0, 0, 0, 0),
    ('USUARIO', 'usuarios',           0, 0, 0, 0),
    ('USUARIO', 'tecnicos',           0, 0, 0, 0),
    ('USUARIO', 'catalogos',          0, 0, 0, 0),
    ('USUARIO', 'perfiles',           0, 0, 0, 0)
  ) v (ROL, PANTALLA, VER, CREAR, EDITAR, ELIMINAR)
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_ROL_PERMISO p
                    WHERE p.ROL = v.ROL AND p.PANTALLA = v.PANTALLA);
PRINT '>> Permisos por defecto: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregados';
GO
