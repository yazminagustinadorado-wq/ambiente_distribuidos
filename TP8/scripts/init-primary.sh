#!/bin/sh
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replicator';"
printf '\nhost replication replicator all scram-sha-256\n' >> "$PGDATA/pg_hba.conf"
