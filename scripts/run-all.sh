#!/usr/bin/env bash
# Alle SQL-Dateien der Folgen 001–010 gegen die Compose-Datenbank ausführen.
#   docker compose up -d && ./scripts/run-all.sh
# Ohne Docker-exec: PGHOST=127.0.0.1 PGPORT=5433 PGUSER/PGPASSWORD setzen und PSQL=psql ./scripts/run-all.sh
set -euo pipefail
cd "$(dirname "$0")/.."
[ -f .env ] && set -a && . ./.env && set +a
: "${POSTGRES_USER:=shop}"
if [ -n "${PSQL:-}" ]; then run() { $PSQL -v ON_ERROR_STOP=1 -q -d shop "$@"; }
else run() { docker compose exec -T db psql -U "$POSTGRES_USER" -v ON_ERROR_STOP=1 -q -d shop "$@"; }; fi

# Mit Docker: Container starten (falls nötig) und warten, bis PostgreSQL bereit ist
if [ -z "${PSQL:-}" ]; then
  echo "⏳ warte auf die Datenbank (healthy) …"
  docker compose up -d --wait db
fi

echo "▸ schema/shop.sql"; run < schema/shop.sql
for f in episodes/0{01..10}/*.sql; do
  [ -e "$f" ] || continue
  echo "▸ $f"; run < "$f" > /dev/null
done
echo "✅ alle SQL-Dateien ausgeführt"
