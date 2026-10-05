/* ============================================================
   Usuarios de prueba para el entorno de desarrollo.

   ATENCION: contrasenas conocidas y en texto plano. Este script
   es solo para desarrollo local. Antes de cualquier despliegue
   real hay que borrarlo o poner SEED_DEMO_USERS=0, y cifrar las
   contrasenas (ver la seccion "Pendiente" del README).

   El dump original no traia ningun usuario, por lo que sin esto
   no habia forma de iniciar sesion.
   ============================================================ */
USE MDMQ_SIGUBA;
GO

-- Usuarios de la aplicacion (tabla UBA_USUARIO, con rol).
MERGE PROYECTO_UBA.UBA_USUARIO AS destino
USING (VALUES
    ('ADMINISTRADOR', 'SIGUBA',   '1700000001', 'admin',   'Admin2026',   'ADMIN'),
    ('TECNICO',       'CAMPO',    '1700000002', 'tecnico', 'Tecnico2026', 'TECNICO'),
    ('USUARIO',       'CONSULTA', '1700000003', 'usuario', 'Usuario2026', 'USUARIO'),
    ('ANA',           'TORRES',   '1700000004', 'atorres', 'Tecnico2026', 'TECNICO')
) AS origen (NOMBRE, APELLIDO, CEDULA, USUARIO, PASSWORD, ROL)
ON destino.USUARIO = origen.USUARIO
WHEN NOT MATCHED THEN
    INSERT (NOMBRE, APELLIDO, CEDULA, FECHA_NACIMIENTO, USUARIO, MAIL,
            PASSWORD, ROL, ESTADO, TIPO_IDENTIFICACION, SSO_ID, TIPO_USUARIO)
    VALUES (origen.NOMBRE, origen.APELLIDO, origen.CEDULA, '1990-01-01',
            origen.USUARIO, origen.USUARIO + '@quito.gob.ec',
            origen.PASSWORD, origen.ROL, 'ACTIVO', 'CEDULA', '', 1);
GO

-- No se insertan filas en UBA_TECNICO: la ficha de tecnico se deriva de la
-- cuenta con rol TECNICO en 04-migraciones.sql. Tener dos altas separadas era
-- justo lo que producia tecnicos sin cuenta y cuentas sin ficha.

PRINT 'Usuarios de prueba listos.';
GO
