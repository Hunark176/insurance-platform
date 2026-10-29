alter table claims
    add column version bigint not null default 0;

alter table claims
    alter column updated_at set not null;

alter table policies
    add column version bigint not null default 0;

alter table payouts
    add column version bigint not null default 0;
