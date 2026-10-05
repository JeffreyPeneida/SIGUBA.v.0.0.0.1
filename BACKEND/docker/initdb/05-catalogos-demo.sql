/* ============================================================
   Datos de catalogo para poder probar los flujos completos.

   El dump dejaba catalogos vacios o a medias: ningun tipo de
   evidencia, un solo nivel de infestacion (y solo para roedores)
   y una sola dependencia. Con eso la inspeccion de aves o
   cucarachas no tenia niveles que elegir.

   Se aplica SIEMPRE, igual que 04-migraciones.sql: cada fila se
   inserta solo si no existe ya otra con el mismo nombre, asi que
   no duplica nada ni pisa lo que se haya editado en Catalogos.
   Las especies se buscan por nombre, no por id.
   ============================================================ */
USE MDMQ_SIGUBA;
GO

SET NOCOUNT ON;
GO

/* ---------------------------------------------- niveles de infestacion */
DECLARE @niveles TABLE (ESPECIE VARCHAR(20), NOMBRE VARCHAR(50), DESCRIPCION VARCHAR(300));
INSERT INTO @niveles VALUES
    ('ROEDOR',    'BAJO',  'Hasta 3 indicios'),
    ('ROEDOR',    'MEDIO', 'De 4 a 6 indicios'),
    ('ROEDOR',    'ALTO',  'Más de 6 indicios o ejemplares vivos a la vista'),
    ('PALOMA',    'BAJO',  'Menos de 10 aves, sin nidos'),
    ('PALOMA',    'MEDIO', 'De 10 a 50 aves o nidos aislados'),
    ('PALOMA',    'ALTO',  'Más de 50 aves o nidos en varios puntos'),
    ('CUCARACHA', 'BAJO',  'Indicios aislados, sin ejemplares vivos'),
    ('CUCARACHA', 'MEDIO', 'Ootecas o ejemplares en un solo ambiente'),
    ('CUCARACHA', 'ALTO',  'Ejemplares vivos en varios ambientes');

INSERT INTO PROYECTO_UBA.UBA_NIVEL_INFESTACION (ID_ESPECIE, NOMBRE, DESCRIPCION)
SELECT e.ID_ESPECIE, n.NOMBRE, n.DESCRIPCION
  FROM @niveles n
  JOIN PROYECTO_UBA.UBA_ESPECIE e ON UPPER(e.NOMBRE) LIKE n.ESPECIE + '%'
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_NIVEL_INFESTACION x
                    WHERE x.ID_ESPECIE = e.ID_ESPECIE AND UPPER(x.NOMBRE) = n.NOMBRE);
PRINT '>> Niveles de infestacion: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregados';
GO

/* ---------------------------------------------------- tipos de evidencia */
-- Los mismos indicios que ofrece la pantalla de inspeccion.
DECLARE @evidencias TABLE (ESPECIE VARCHAR(20), NOMBRE VARCHAR(100));
INSERT INTO @evidencias VALUES
    ('ROEDOR', 'MADRIGUERAS'), ('ROEDOR', 'HECES'), ('ROEDOR', 'SENDEROS'),
    ('ROEDOR', 'ROEDURAS'), ('ROEDOR', 'OTROS'),
    ('PALOMA', 'NIDOS'), ('PALOMA', 'HECES'), ('PALOMA', 'PLUMAS'),
    ('PALOMA', 'PICOTEOS'), ('PALOMA', 'OTROS'),
    ('CUCARACHA', 'OOTECAS'), ('CUCARACHA', 'EJEMPLARES VIVOS'), ('CUCARACHA', 'OTROS');

INSERT INTO PROYECTO_UBA.UBA_TIPO_EVIDENCIA (ID_ESPECIE, NOMBRE)
SELECT e.ID_ESPECIE, v.NOMBRE
  FROM @evidencias v
  JOIN PROYECTO_UBA.UBA_ESPECIE e ON UPPER(e.NOMBRE) LIKE v.ESPECIE + '%'
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_TIPO_EVIDENCIA x
                    WHERE x.ID_ESPECIE = e.ID_ESPECIE AND UPPER(x.NOMBRE) = v.NOMBRE);
PRINT '>> Tipos de evidencia: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregados';
GO

/* ---------------------------------------------------------- dependencias */
-- Solo aplican al tipo de denunciante "DEPENDIENTE MUNICIPAL".
DECLARE @dependencias TABLE (NOMBRE VARCHAR(150));
INSERT INTO @dependencias VALUES
    ('SECRETARÍA DE AMBIENTE'),
    ('AGENCIA METROPOLITANA DE CONTROL'),
    ('EMPRESA PÚBLICA METROPOLITANA DE ASEO (EMASEO EP)'),
    ('EMPRESA PÚBLICA METROPOLITANA DE AGUA POTABLE (EPMAPS)'),
    ('SECRETARÍA DE EDUCACIÓN');

INSERT INTO PROYECTO_UBA.UBA_DEPENDENCIA (NOMBRE, ID_TIPO)
SELECT d.NOMBRE, t.ID_TIPO
  FROM @dependencias d
 CROSS JOIN (SELECT TOP 1 ID_TIPO FROM PROYECTO_UBA.UBA_TIPO_DENUNCIANTE
              WHERE UPPER(NOMBRE) LIKE '%DEPENDIENTE%' ORDER BY ID_TIPO) t
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_DEPENDENCIA x WHERE UPPER(x.NOMBRE) = d.NOMBRE);
PRINT '>> Dependencias: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregadas';
GO

/* ---------------------------------------------- catalogos simples cortos */
INSERT INTO PROYECTO_UBA.UBA_PREDIO (NOMBRE)
SELECT v.NOMBRE FROM (VALUES ('PÚBLICO NO MUNICIPAL'), ('ESPACIO PÚBLICO (PARQUE, QUEBRADA, VÍA)'),
                             ('LOTE BALDÍO')) v(NOMBRE)
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_PREDIO x WHERE UPPER(x.NOMBRE) = v.NOMBRE);
PRINT '>> Predios: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregados';

INSERT INTO PROYECTO_UBA.UBA_TIPO_RODENTICIDA (NOMBRE)
SELECT v.NOMBRE FROM (VALUES ('BLOQUE PARAFINADO'), ('POLVO DE CONTACTO')) v(NOMBRE)
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_TIPO_RODENTICIDA x WHERE UPPER(x.NOMBRE) = v.NOMBRE);
PRINT '>> Tipos de rodenticida: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregados';

INSERT INTO PROYECTO_UBA.UBA_AREA_INSPECCION (NOMBRE)
SELECT v.NOMBRE FROM (VALUES ('PERIURBANA')) v(NOMBRE)
 WHERE NOT EXISTS (SELECT 1 FROM PROYECTO_UBA.UBA_AREA_INSPECCION x WHERE UPPER(x.NOMBRE) = v.NOMBRE);
PRINT '>> Areas de inspeccion: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' agregadas';
GO
