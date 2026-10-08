alter table claims add column occurred_on date;
alter table claims add column updated_at timestamp;

update claims c
set policy_id = (
    select case
        when count(p.id) = 1 and (c.policy_id is null or c.policy_id = min(p.id))
            then min(p.id)
        else null
    end
    from customers customer
    left join policies p on p.customer_id = customer.id
    where customer.customer_number = c.customer_number
);

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
    else null
end;

alter table claims drop column customer_number;
alter table claims alter column policy_id set not null;
alter table claims alter column occurred_on set not null;
alter table claims alter column claim_type type varchar(30);
alter table claims
    add constraint fk_claim_policy foreign key (policy_id) references policies(id);
