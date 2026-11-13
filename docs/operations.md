# Betrieb

Anleitungen für den Server mit `docker-compose.prod.yml`. Alle Befehle laufen im
Repository-Klon `~/insurance-platform`. Vorher den Klon aktualisieren, damit die
Skripte dem deployten Stand entsprechen:

```bash
cd ~/insurance-platform && git pull --ff-only
```

Abkürzung für die folgenden Befehle:

```bash
alias dc='docker compose --project-name insurance-platform -f docker-compose.prod.yml --env-file .env'
```

## Zustand prüfen

```bash
dc ps                      # Health-Status aller Container
dc logs --tail=100 backend # JSON-Logs, eine Zeile pro Eintrag
dc exec backend curl -fsS http://localhost:8080/actuator/health
```

`/actuator/health` meldet `DOWN`, wenn Events länger als 30 Minuten nicht
zugestellt wurden (Billing hängt). Die App bleibt dabei in Betrieb, weil der
Container-Healthcheck nur die Readiness prüft. Ursache im Log suchen
(`Event publications incomplete`), beheben; die Zustellung wird automatisch
wiederholt.

## Backup und Wiederherstellung

```bash
mkdir -p backups
dc exec -T db sh -c 'pg_dump --format=custom -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
  > backups/insurance-$(date -u +%Y%m%dT%H%M%SZ).dump

# Wiederherstellen (überschreibt die aktuellen Daten):
dc exec -T db sh -c 'pg_restore --clean --if-exists -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
  < backups/<datei>.dump
```

Das Volume `insurance-platform_insurance-postgres-data` nie mit
`docker compose down -v` löschen.

## Demodaten zurücksetzen

Besucher können über die öffentlichen Demo-Zugänge Daten ändern. Das Skript
stellt den Ausgangszustand wieder her:

```bash
ops/reset-demo-data.sh --yes
```

1. Es schreibt zuerst ein Backup nach `backups/` und bricht ohne Änderung ab,
   wenn das nicht gelingt (die letzten 14 Backups bleiben erhalten).
2. Danach leert es alle Geschäftstabellen und spielt
   `backend/src/main/resources/db/dev-data.sql` ein – **in einer einzigen
   Transaktion**. Schlägt das Einspielen fehl, wird alles zurückgerollt und
   die bisherigen Daten bleiben. Die Seite ist also nie leer.

Ergebnis: 50 Demo-Kunden mit 65 Policen, Schadenfällen und Abrechnungen wie
nach dem ersten Deployment. Schema und Flyway-Historie bleiben unverändert.
`DemoDataResetIntegrationTest` prüft genau dieses SQL in der CI gegen
PostgreSQL, einschließlich des Rollbacks bei einem Fehler.

Das Zurücksetzen läuft **nicht automatisch**. Wer es nächtlich möchte, trägt es
selbst ein (`crontab -e`):

```cron
0 4 * * * cd ~/insurance-platform && ops/reset-demo-data.sh --yes >> backups/reset.log 2>&1
```

## PostgreSQL-Major-Upgrade (z. B. 15 → 16)

Ein Datenverzeichnis funktioniert nur mit der Major-Version, die es angelegt
hat. Ein Upgrade erfolgt deshalb über Dump und Restore in ein **neues** Volume;
das alte bleibt bis zur erfolgreichen Prüfung als Rückfallebene erhalten.

1. **Vorbereiten (Branch, PR):** In `docker-compose.prod.yml` das Image auf
   `postgres:16-alpine` ändern und das Volume umbenennen
   (`insurance-postgres16-data`, auch unter `volumes:`). Dieselbe Version in
   `docker-compose.yml` und in den Testcontainers-Tests eintragen. CI muss grün
   sein. Noch **nicht** mergen.
2. **Backup** wie oben erstellen und Größe prüfen.
3. **Wartungsfenster:** `dc stop backend caddy`
4. **Mergen** – das Deployment startet PostgreSQL 16 mit leerem neuem Volume;
   das Backend wartet auf die Datenbank und Flyway legt das Schema samt
   Demodaten an.
5. **Daten übernehmen:** `dc stop backend caddy`, dann das Backup einspielen:

   ```bash
   dc exec -T db sh -c 'pg_restore --clean --if-exists -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
     < backups/<datei>.dump
   dc up -d
   ```

6. **Prüfen:** `dc ps` (alle `healthy`), Seite und Swagger öffnen, Anzahl der
   Policen und Claims mit dem Stand vor dem Upgrade vergleichen.
7. **Aufräumen** nach einigen Tagen: altes Volume löschen
   (`docker volume rm insurance-platform_insurance-postgres-data`).

**Rückweg:** Image und Volume-Namen im Compose-File zurückstellen und neu
deployen; das alte Volume ist unverändert.
