-- Store claim audit timestamps as absolute points in time.
--
-- created_at/updated_at were "timestamp without time zone" holding UTC values
-- (the auditing clock is Clock.systemUTC()). Without a zone, the API returned
-- values like 2026-09-24T17:00:00 that browsers interpret as local time.
-- Interpreting the existing values as UTC keeps every instant unchanged; the
-- JPA mapping now uses java.time.Instant (serialized with a trailing "Z"),
-- consistent with audit_entries.occurred_at.
--
-- Dependent indexes (e.g. idx_claims_status_created_at) are rebuilt by PostgreSQL.

alter table claims
    alter column created_at type timestamp with time zone using created_at at time zone 'UTC',
    alter column updated_at type timestamp with time zone using updated_at at time zone 'UTC';
