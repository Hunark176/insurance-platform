-- Annual aggregate limit ("Jahreshöchstleistung").
--
-- Besides the coverage limit per loss, liability products usually cap the total
-- paid for all losses of one policy year, typically at twice the coverage limit
-- ("2-fache Maximierung"). The factor is a product term; like the coverage limit
-- it is copied to each policy at issuance as an absolute amount, so later product
-- changes do not alter existing contracts. NULL means: per-loss limit only.

alter table products add column annual_aggregate_factor integer;
alter table products
    add constraint ck_products_annual_aggregate_factor
        check (annual_aggregate_factor is null or annual_aggregate_factor between 1 and 10);

alter table policies add column annual_aggregate_limit numeric(19, 2);
alter table policies
    add constraint ck_policies_annual_aggregate_limit
        check (annual_aggregate_limit is null or annual_aggregate_limit >= coverage_limit);

-- Private liability: twice the coverage limit per policy year. Other demo products
-- (motor, household contents, life) keep the per-loss limit only.
update products set annual_aggregate_factor = 2 where code = 'PRIVAT-HAFTPFLICHT';

update policies p
set annual_aggregate_limit = p.coverage_limit * pr.annual_aggregate_factor
from products pr
where pr.id = p.product_id
  and pr.annual_aggregate_factor is not null;

-- Supports summing approved claims of a policy within a policy year.
create index idx_claims_policy_status_occurred_on on claims (policy_id, status, occurred_on);
