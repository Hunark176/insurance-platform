#!/usr/bin/env bash
# Resets the public demo to its original demo data.
#
#   1. Writes a full backup (pg_dump, custom format) to ~/insurance-platform/backups
#      and aborts if that fails. The last 14 backups are kept.
#   2. Empties all business tables and loads the demo data in ONE transaction.
#      If anything fails, PostgreSQL rolls back and the previous data remains –
#      the site is never left empty.
#
# Usage on the server (repository clone in ~/insurance-platform):
#   ops/reset-demo-data.sh --yes
#
# Optional nightly run (only if you want it), e.g. at 04:00 via `crontab -e`:
#   0 4 * * * cd ~/insurance-platform && ops/reset-demo-data.sh --yes >> backups/reset.log 2>&1
#
# Restore a backup:
#   docker compose -f docker-compose.prod.yml exec -T db \
#     sh -c 'pg_restore --clean --if-exists -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < backups/<file>.dump
set -euo pipefail

if [ "${1:-}" != "--yes" ]; then
  echo "This replaces ALL business data with the demo data (a backup is taken first)."
  echo "Run again with --yes to continue."
  exit 1
fi

cd "$(dirname "$0")/.."
COMPOSE=(docker compose --project-name insurance-platform -f docker-compose.prod.yml --env-file .env)
BACKUP_DIR=backups
mkdir -p "$BACKUP_DIR"
BACKUP="$BACKUP_DIR/insurance-$(date -u +%Y%m%dT%H%M%SZ).dump"

echo "1/2 Backup -> $BACKUP"
"${COMPOSE[@]}" exec -T db sh -c 'pg_dump --format=custom -U "$POSTGRES_USER" -d "$POSTGRES_DB"' > "$BACKUP"
if [ ! -s "$BACKUP" ]; then
  echo "Backup is empty – aborting without changing any data." >&2
  rm -f "$BACKUP"
  exit 1
fi
ls -1t "$BACKUP_DIR"/insurance-*.dump | tail -n +15 | xargs -r rm --

echo "2/2 Reset demo data (single transaction)"
cat ops/reset-demo-data.sql backend/src/main/resources/db/dev-data.sql \
  | "${COMPOSE[@]}" exec -T db sh -c \
      'psql --single-transaction -v ON_ERROR_STOP=1 --quiet -U "$POSTGRES_USER" -d "$POSTGRES_DB"'

echo "Done. Backup: $BACKUP"
