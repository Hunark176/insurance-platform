-- Spring Modulith event publication registry (transactional outbox).
--
-- Every event published inside a business transaction is stored here in the
-- same transaction, together with the listener that has to process it. When
-- the listener succeeds, completion_date is set; publications without a
-- completion_date are re-delivered on restart and by the periodic retry, so a
-- crash or a failing listener can no longer lose a payout or an invoice.
--
-- Schema follows the PostgreSQL schema shipped with Spring Modulith 1.4.

create table event_publication (
    id               uuid                     not null,
    listener_id      text                     not null,
    event_type       text                     not null,
    serialized_event text                     not null,
    publication_date timestamp with time zone not null,
    completion_date  timestamp with time zone,
    constraint pk_event_publication primary key (id)
);

-- Lookups for incomplete publications and for marking a publication complete.
create index idx_event_publication_completion_date
    on event_publication (completion_date);
create index idx_event_publication_serialized_event_hash
    on event_publication using hash (serialized_event);

-- Required by the entity mapping; only used with completion-mode ARCHIVE.
create table event_publication_archive (
    id               uuid                     not null,
    listener_id      text                     not null,
    event_type       text                     not null,
    serialized_event text                     not null,
    publication_date timestamp with time zone not null,
    completion_date  timestamp with time zone,
    constraint pk_event_publication_archive primary key (id)
);
