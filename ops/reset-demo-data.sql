-- Empties all business data before the demo data is loaded again.
--
-- Used only by ops/reset-demo-data.sh, which runs this file together with
-- backend/src/main/resources/db/dev-data.sql in ONE transaction
-- (psql --single-transaction): if loading the demo data fails, the TRUNCATE is
-- rolled back as well and the existing data stays untouched.
--
-- Schema and Flyway history are not touched. Identity counters restart, so
-- the demo data gets the same ids as after the first deployment.

truncate table
    claims,
    payouts,
    invoices,
    credit_notes,
    policies,
    customers,
    products,
    audit_entries,
    event_publication,
    event_publication_archive,
    idempotency_records
restart identity cascade;
