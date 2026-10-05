#!/usr/bin/env bash
# Espera a que SQL Server acepte conexiones y aplica los scripts de /initdb.
#
# Sólo carga el esquema si la base todavía no está inicializada: este
# contenedor se vuelve a ejecutar en cada `docker compose up`, y 01-schema.sql
# borra y recrea todas las tablas. Sin esta guarda, cada arranque destruiría
# los trámites y usuarios registrados.
#
# Para forzar la recarga:  FORCE_DB_INIT=1 docker compose up db-init
set -euo pipefail

SQLCMD=/opt/mssql-tools18/bin/sqlcmd
[ -x "$SQLCMD" ] || SQLCMD=/opt/mssql-tools/bin/sqlcmd

run() { "$SQLCMD" -S "$DB_HOST" -U sa -P "$MSSQL_SA_PASSWORD" -C -b "$@"; }

# Devuelve la salida de una consulta de un solo valor, o "" si sqlcmd falla.
# sqlcmd escribe los errores en stdout, asi que el llamador debe validar que lo
# devuelto sea realmente un numero.
scalar() { run -h -1 -W -Q "SET NOCOUNT ON; $1" 2>&1 | tr -d '[:space:]' || true; }

es_numero() { case "$1" in ''|*[!0-9]*) return 1 ;; *) return 0 ;; esac; }

echo "Esperando a SQL Server en $DB_HOST ..."
for i in $(seq 1 90); do
    if run -Q "SELECT 1" >/dev/null 2>&1; then
        echo "SQL Server disponible (intento $i)."
        break
    fi
    if [ "$i" -eq 90 ]; then
        echo "SQL Server no respondio tras 90 intentos." >&2
        exit 1
    fi
    sleep 2
done

# Aceptar conexiones no significa que MDMQ_SIGUBA este lista: tras un reinicio
# SQL Server la sigue recuperando y responde "cannot be autostarted during
# server shutdown or startup" un rato despues de declararse sano. Por eso se
# reintenta la consulta real en vez de fiarse de DATABASEPROPERTYEX.
tablas=""
for i in $(seq 1 60); do

    existe=$(scalar "SELECT COUNT(*) FROM sys.databases WHERE name = 'MDMQ_SIGUBA';")

    if es_numero "$existe" && [ "$existe" -eq 0 ]; then
        echo "MDMQ_SIGUBA no existe: se creara desde cero."
        tablas=0
        break
    fi

    if es_numero "$existe"; then
        n=$(scalar "SELECT COUNT(*) FROM MDMQ_SIGUBA.sys.tables t
             JOIN MDMQ_SIGUBA.sys.schemas s ON s.schema_id = t.schema_id
             WHERE s.name = 'PROYECTO_UBA';")
        if es_numero "$n"; then
            echo "MDMQ_SIGUBA lista ($n tablas)."
            tablas=$n
            break
        fi
    fi

    [ "$i" -eq 1 ] && echo "MDMQ_SIGUBA aun recuperandose, esperando ..."
    sleep 2
done

if ! es_numero "$tablas"; then
    echo "MDMQ_SIGUBA no quedo disponible tras 120s." >&2
    exit 1
fi

if [ "${FORCE_DB_INIT:-0}" != "1" ] && [ "$tablas" -gt 0 ]; then

    echo "MDMQ_SIGUBA ya tiene $tablas tablas: no se recarga."
    echo "Usa FORCE_DB_INIT=1 para reconstruirla desde cero."

    # Las migraciones si se aplican: son los cambios de esquema posteriores
    # al dump, y una base ya cargada es precisamente la que los necesita.
    echo ">> Aplicando 04-migraciones.sql"
    run -i /initdb/04-migraciones.sql

    echo ">> Aplicando 05-catalogos-demo.sql"
    run -i /initdb/05-catalogos-demo.sql

    echo ">> Aplicando 06-perfilamiento.sql"
    run -i /initdb/06-perfilamiento.sql

    exit 0
fi

echo ">> Aplicando 01-schema.sql (esquema + datos)"
run -i /initdb/01-schema.sql

echo ">> Aplicando 02-app-user.sql (login de aplicacion)"
run -i /initdb/02-app-user.sql \
    -v APP_DB_USER="$APP_DB_USER" APP_DB_PASSWORD="$APP_DB_PASSWORD"

if [ "${SEED_DEMO_USERS:-1}" = "1" ]; then
    echo ">> Aplicando 03-usuarios-demo.sql (usuarios de prueba)"
    run -i /initdb/03-usuarios-demo.sql
else
    echo ">> Usuarios de prueba omitidos (SEED_DEMO_USERS=0)"
fi

# Va al final: los datos de prueba tambien tienen que quedar migrados.
echo ">> Aplicando 04-migraciones.sql"
run -i /initdb/04-migraciones.sql

# Completa los catalogos que el dump dejo vacios; no duplica lo existente.
echo ">> Aplicando 05-catalogos-demo.sql"
run -i /initdb/05-catalogos-demo.sql

echo ">> Aplicando 06-perfilamiento.sql"
run -i /initdb/06-perfilamiento.sql

echo ">> Verificacion"
run -d MDMQ_SIGUBA -Q "SET NOCOUNT ON;
SELECT COUNT(*) AS tablas FROM sys.tables t
  JOIN sys.schemas s ON s.schema_id = t.schema_id WHERE s.name = 'PROYECTO_UBA';
SELECT COUNT(*) AS foreign_keys FROM sys.foreign_keys f
  JOIN sys.tables t ON t.object_id = f.parent_object_id
  JOIN sys.schemas s ON s.schema_id = t.schema_id WHERE s.name = 'PROYECTO_UBA';
SELECT (SELECT COUNT(*) FROM PROYECTO_UBA.UBA_BARRIO)      AS barrios,
       (SELECT COUNT(*) FROM PROYECTO_UBA.UBA_PARROQUIA)   AS parroquias,
       (SELECT COUNT(*) FROM PROYECTO_UBA.UBA_ADMIN_ZONAL) AS admin_zonales;"

echo "Inicializacion completada."
