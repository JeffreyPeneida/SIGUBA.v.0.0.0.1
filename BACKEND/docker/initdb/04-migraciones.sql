/* ============================================================
   Cambios de esquema propios del proyecto, posteriores al dump.

   A diferencia de 01-schema.sql, este script se aplica SIEMPRE,
   tambien sobre una base ya cargada, asi que cada paso comprueba
   antes si ya esta hecho.
   ============================================================ */
USE MDMQ_SIGUBA;
GO

/* ------------------------------------------------------------
   UBA_TECNICO.ID_USUARIO

   El dump dejaba UBA_TECNICO y UBA_USUARIO sin ninguna relacion:
   los tramites apuntan a UBA_TECNICO, pero quien inicia sesion
   vive en UBA_USUARIO. Resultado: a quien podia entrar al sistema
   no se le podia asignar nada, y a quien se le asignaba no podia
   entrar. Esta columna es el vinculo que faltaba.
   ------------------------------------------------------------ */
IF COL_LENGTH('PROYECTO_UBA.UBA_TECNICO', 'ID_USUARIO') IS NULL
BEGIN
    ALTER TABLE PROYECTO_UBA.UBA_TECNICO ADD ID_USUARIO INT NULL;
    PRINT '>> UBA_TECNICO.ID_USUARIO agregada';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_UBA_TECNICO_USUARIO')
BEGIN
    ALTER TABLE PROYECTO_UBA.UBA_TECNICO
        ADD CONSTRAINT FK_UBA_TECNICO_USUARIO
        FOREIGN KEY (ID_USUARIO) REFERENCES PROYECTO_UBA.UBA_USUARIO (ID_USUARIO);
    PRINT '>> FK_UBA_TECNICO_USUARIO creada';
END
GO

-- Un indice filtrado exige estas opciones; sqlcmd no siempre las trae puestas.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

-- Filtrado: un usuario no puede ser dos tecnicos, pero si puede haber
-- tecnicos historicos sin cuenta (ID_USUARIO NULL).
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_UBA_TECNICO_USUARIO')
BEGIN
    CREATE UNIQUE INDEX UX_UBA_TECNICO_USUARIO
        ON PROYECTO_UBA.UBA_TECNICO (ID_USUARIO)
        WHERE ID_USUARIO IS NOT NULL;
END
GO

/* ------------------------------------------------------------
   Enlazar lo que ya existe: primero por nombre de usuario, que es
   el dato mas fiable, y despues por cedula.
   ------------------------------------------------------------ */
UPDATE t
   SET t.ID_USUARIO = u.ID_USUARIO
  FROM PROYECTO_UBA.UBA_TECNICO t
  JOIN PROYECTO_UBA.UBA_USUARIO u
    ON UPPER(u.USUARIO) = UPPER(t.TECNICO_USUARIO)
 WHERE t.ID_USUARIO IS NULL
   AND NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_TECNICO x
                    WHERE x.ID_USUARIO = u.ID_USUARIO);
GO

UPDATE t
   SET t.ID_USUARIO = u.ID_USUARIO
  FROM PROYECTO_UBA.UBA_TECNICO t
  JOIN PROYECTO_UBA.UBA_USUARIO u
    ON u.CEDULA = t.CEDULA
 WHERE t.ID_USUARIO IS NULL
   AND NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_TECNICO x
                    WHERE x.ID_USUARIO = u.ID_USUARIO);
GO

/* ------------------------------------------------------------
   Tecnicos que quedaron sin cuenta: se les crea una con rol
   TECNICO a partir de sus propios datos. Sin cuenta no podrian
   entrar a atender lo que se les asigna.

   La contrasena queda en texto plano a proposito: PasswordMigrationRunner
   la cifra al arrancar el backend, igual que hace con las de prueba.
   ------------------------------------------------------------ */
INSERT INTO PROYECTO_UBA.UBA_USUARIO
    (NOMBRE, APELLIDO, CEDULA, FECHA_NACIMIENTO, USUARIO, MAIL,
     PASSWORD, ROL, ESTADO, TIPO_IDENTIFICACION, SSO_ID, TIPO_USUARIO)
SELECT t.NOMBRE, t.APELLIDO, t.CEDULA, '1990-01-01',
       t.TECNICO_USUARIO,
       ISNULL(t.CORREO, t.TECNICO_USUARIO + '@quito.gob.ec'),
       'Tecnico2026', 'TECNICO', ISNULL(t.ESTADO, 'ACTIVO'), 'CEDULA', '', 1
  FROM PROYECTO_UBA.UBA_TECNICO t
 WHERE t.ID_USUARIO IS NULL
   AND t.TECNICO_USUARIO IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_USUARIO u
                    WHERE UPPER(u.USUARIO) = UPPER(t.TECNICO_USUARIO));
GO

UPDATE t
   SET t.ID_USUARIO = u.ID_USUARIO
  FROM PROYECTO_UBA.UBA_TECNICO t
  JOIN PROYECTO_UBA.UBA_USUARIO u
    ON UPPER(u.USUARIO) = UPPER(t.TECNICO_USUARIO)
 WHERE t.ID_USUARIO IS NULL
   AND NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_TECNICO x
                    WHERE x.ID_USUARIO = u.ID_USUARIO);
GO

/* ------------------------------------------------------------
   Y al reves: usuarios con rol TECNICO que no tenian ficha de
   tecnico, por lo que no aparecian al asignar.
   ------------------------------------------------------------ */
INSERT INTO PROYECTO_UBA.UBA_TECNICO
    (NOMBRE, APELLIDO, CEDULA, TECNICO_USUARIO, CORREO, ESTADO, ID_USUARIO)
SELECT u.NOMBRE, u.APELLIDO, u.CEDULA, u.USUARIO,
       ISNULL(u.MAIL, u.USUARIO + '@quito.gob.ec'), u.ESTADO, u.ID_USUARIO
  FROM PROYECTO_UBA.UBA_USUARIO u
 WHERE u.ROL = 'TECNICO'
   AND NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_TECNICO t
                    WHERE t.ID_USUARIO = u.ID_USUARIO);
GO

/* ------------------------------------------------------------
   La contrasena de UBA_TECNICO ya no la usa nadie: el login es
   unico y vive en UBA_USUARIO. Se vacia para no dejar credenciales
   duplicadas rondando.
   ------------------------------------------------------------ */
UPDATE PROYECTO_UBA.UBA_TECNICO
   SET PASSWORD = NULL
 WHERE PASSWORD IS NOT NULL;
GO

/* ------------------------------------------------------------
   UBA_INFORME_INSPECCION

   Informe tecnico de la inspeccion (modelo oficial de la UBA), uno
   por tramite. Los datos del lugar se copian del tramite al crear el
   borrador y aqui quedan editables: el informe recoge lo que el
   tecnico constato, que no siempre coincide con lo denunciado.

   Las listas (participantes, detalle por especie, recomendaciones por
   entidad) se guardan como JSON: son propias de cada informe y no se
   consultan por separado.
   ------------------------------------------------------------ */
IF OBJECT_ID('PROYECTO_UBA.UBA_INFORME_INSPECCION', 'U') IS NULL
BEGIN
    CREATE TABLE PROYECTO_UBA.UBA_INFORME_INSPECCION (
        ID_CODIGO_INTERNO   INT            NOT NULL,
        DOCUMENTO_ATENDIDO  NVARCHAR(300)  NULL,
        TIPO_INSPECCION     NVARCHAR(300)  NULL,
        FECHA_INSPECCION    DATETIME       NULL,
        PLAGAS              NVARCHAR(300)  NULL,
        NIVEL               NVARCHAR(50)   NULL,
        ASUNTO              NVARCHAR(300)  NULL,
        DIRECCION           NVARCHAR(500)  NULL,
        SECTOR_BARRIO       NVARCHAR(200)  NULL,
        ADMIN_ZONAL         NVARCHAR(150)  NULL,
        TIPO_LUGAR          NVARCHAR(200)  NULL,
        AREAS_SUPERVISADAS  NVARCHAR(500)  NULL,
        COORDENADAS         NVARCHAR(100)  NULL,
        BENEFICIARIOS       NVARCHAR(50)   NULL,
        PERSONA_CONTACTADA  NVARCHAR(200)  NULL,
        PARTICIPANTES       NVARCHAR(MAX)  NULL,
        DETALLE_ESPECIES    NVARCHAR(MAX)  NULL,
        FACTORES_RIESGO     NVARCHAR(1000) NULL,
        DIAGNOSTICO         NVARCHAR(MAX)  NULL,
        TIPO_CONTROL        VARCHAR(10)    NULL,
        PROGRAMA_ACTUACION  NVARCHAR(MAX)  NULL,
        CONCLUSIONES        NVARCHAR(MAX)  NULL,
        RECOMENDACIONES     NVARCHAR(MAX)  NULL,
        ELABORADO_NOMBRE    NVARCHAR(200)  NULL,
        ELABORADO_CARGO     NVARCHAR(200)  NULL,
        REVISADO_NOMBRE     NVARCHAR(200)  NULL,
        REVISADO_CARGO      NVARCHAR(200)  NULL,
        FECHA_ELABORACION   DATE           NULL,
        FECHA_ACTUALIZACION DATETIME       NULL,
        ACTUALIZADO_POR     VARCHAR(50)    NULL,
        CONSTRAINT PK_UBA_INFORME_INSPECCION PRIMARY KEY (ID_CODIGO_INTERNO),
        CONSTRAINT FK_UBA_INFORME_INSPECCION_TRAMITE
            FOREIGN KEY (ID_CODIGO_INTERNO)
            REFERENCES PROYECTO_UBA.UBA_TRAMITE (ID_CODIGO_INTERNO) ON DELETE CASCADE,
        CONSTRAINT CK_UBA_INFORME_INSPECCION_CONTROL
            CHECK (TIPO_CONTROL IS NULL OR TIPO_CONTROL IN ('ACTIVO', 'PASIVO'))
    );
    PRINT '>> UBA_INFORME_INSPECCION creada';
END
GO

/* ------------------------------------------------------------
   Pie de foto y orden de las fotos de inspeccion: el anexo
   fotografico del informe las numera y describe una a una.
   ------------------------------------------------------------ */
IF COL_LENGTH('PROYECTO_UBA.UBA_IMAGEN_INSPECCION', 'DESCRIPCION') IS NULL
BEGIN
    ALTER TABLE PROYECTO_UBA.UBA_IMAGEN_INSPECCION ADD DESCRIPCION NVARCHAR(500) NULL;
    PRINT '>> UBA_IMAGEN_INSPECCION.DESCRIPCION agregada';
END
GO

IF COL_LENGTH('PROYECTO_UBA.UBA_IMAGEN_INSPECCION', 'ORDEN') IS NULL
BEGIN
    ALTER TABLE PROYECTO_UBA.UBA_IMAGEN_INSPECCION ADD ORDEN INT NULL;
    PRINT '>> UBA_IMAGEN_INSPECCION.ORDEN agregada';
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IDX_UBA_IMAGEN_INSPECCION_TRAMITE')
BEGIN
    CREATE INDEX IDX_UBA_IMAGEN_INSPECCION_TRAMITE
        ON PROYECTO_UBA.UBA_IMAGEN_INSPECCION (ID_CODIGO_INTERNO);
END
GO

PRINT '>> Migraciones aplicadas';
SELECT t.ID_TECNICO, t.NOMBRE, t.APELLIDO, t.ID_USUARIO,
       u.USUARIO, u.ROL, u.ESTADO
  FROM PROYECTO_UBA.UBA_TECNICO t
  LEFT JOIN PROYECTO_UBA.UBA_USUARIO u ON u.ID_USUARIO = t.ID_USUARIO;
GO
