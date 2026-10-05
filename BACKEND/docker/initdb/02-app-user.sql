/* Login de aplicacion con permisos acotados a MDMQ_SIGUBA.
   Evita que el backend se conecte como 'sa'. */
USE master;
GO
IF SUSER_ID('$(APP_DB_USER)') IS NULL
    EXEC('CREATE LOGIN [$(APP_DB_USER)] WITH PASSWORD = ''$(APP_DB_PASSWORD)'', '
       + 'DEFAULT_DATABASE = [MDMQ_SIGUBA], CHECK_POLICY = OFF');
GO
USE MDMQ_SIGUBA;
GO
IF DATABASE_PRINCIPAL_ID('$(APP_DB_USER)') IS NULL
    EXEC('CREATE USER [$(APP_DB_USER)] FOR LOGIN [$(APP_DB_USER)]');
GO
ALTER ROLE db_datareader ADD MEMBER [$(APP_DB_USER)];
ALTER ROLE db_datawriter ADD MEMBER [$(APP_DB_USER)];
ALTER ROLE db_ddladmin  ADD MEMBER [$(APP_DB_USER)];
GO
PRINT 'Usuario de aplicacion listo.';
GO
