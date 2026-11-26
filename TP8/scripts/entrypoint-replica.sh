#!/bin/sh
set -e
if [ ! -f "$PGDATA/PG_VERSION" ]; then
  until pg_basebackup -D "$PGDATA" -X stream -P -R -d "host=db-primary port=5432 user=replicator password=replicator dbname=postgres application_name=tp8-replica"; do
    echo "Esperando al primario para clonar la base..." >&2
    sleep 2
  done
fi
exec docker-entrypoint.sh "$@"
