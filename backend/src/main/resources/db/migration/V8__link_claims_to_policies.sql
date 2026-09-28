lock table claims, customers, policies in share row exclusive mode;

do $$
declare
    unmapped_claim_count bigint;
    unsupported_type_count bigint;
begin
    select count(*)
    into unmapped_claim_count
    from claims c
    left join customers customer
        on customer.customer_number = c.customer_number
    left join lateral (
        select count(*) as policy_count, min(p.id) as policy_id
        from policies p
        where p.customer_id = customer.id
    ) candidate on true
    where customer.id is null
       or candidate.policy_count <> 1
       or (c.policy_id is not null and c.policy_id <> candidate.policy_id);

    if unmapped_claim_count > 0 then
        raise exception
            'V8 migration stopped: % claim(s) have no unique policy for their customer number or conflict with their existing policy_id. Resolve the mappings before retrying.',
            unmapped_claim_count;
    end if;

    select count(*)
    into unsupported_type_count
    from claims
    where upper(trim(claim_type)) not in (
        'AUTO', 'KFZ',
        'HOME', 'HAUS', 'HAUSRAT',
        'LIFE', 'LEBEN',
        'LIABILITY', 'HAFTPFLICHT'
    );

    if unsupported_type_count > 0 then
        raise exception
            'V8 migration stopped: % claim(s) have a claim_type that cannot be mapped to ClaimType. Resolve the values before retrying.',
            unsupported_type_count;
    end if;
end $$;

alter table claims add column occurred_on date;
alter table claims add column updated_at timestamp;

update claims c
set policy_id = candidate.policy_id
from customers customer
join lateral (
    select min(p.id) as policy_id
    from policies p
    where p.customer_id = customer.id
    having count(*) = 1
) candidate on true
where c.customer_number = customer.customer_number
  and c.policy_id is null;

update claims
set occurred_on = cast(created_at as date)
where occurred_on is null;

update claims
set updated_at = created_at
where updated_at is null;

update claims
set claim_type = case upper(trim(claim_type))
    when 'AUTO' then 'AUTO'
    when 'KFZ' then 'AUTO'
    when 'HOME' then 'HOME'
    when 'HAUS' then 'HOME'
    when 'HAUSRAT' then 'HOME'
    when 'LIFE' then 'LIFE'
    when 'LEBEN' then 'LIFE'
    when 'LIABILITY' then 'LIABILITY'
    when 'HAFTPFLICHT' then 'LIABILITY'
end;

alter table claims alter column policy_id set not null;
alter table claims alter column occurred_on set not null;
alter table claims alter column claim_type type varchar(30);
alter table claims
    add constraint fk_claim_policy foreign key (policy_id) references policies(id);
