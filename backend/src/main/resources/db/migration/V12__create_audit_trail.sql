create table audit_entries (
    id bigserial primary key,
    occurred_at timestamp with time zone not null,
    actor varchar(100) not null,
    action varchar(40) not null,
    entity_type varchar(40) not null,
    entity_id varchar(100) not null,
    request_id varchar(36),
    summary varchar(500)
);

create index idx_audit_entity_time
    on audit_entries (entity_type, entity_id, occurred_at desc);
