insert into customers (customer_number, display_name, email, active)
select 'DEMO-1001', 'Demo Kunde', 'demo.customer@example.test', true
where not exists (select 1 from customers where customer_number = 'DEMO-1001');

insert into products (code, name, coverage_limit, premium, active)
select 'DEMO-COMPREHENSIVE', 'Demo Versicherungsschutz', 100000.00, 49.90, true
where not exists (select 1 from products where code = 'DEMO-COMPREHENSIVE');

insert into policies (customer_id, product_id, valid_from, valid_to, coverage_limit, premium, status)
select c.id, p.id, current_date - 30, current_date + 365, p.coverage_limit, p.premium, 'ACTIVE'
from customers c cross join products p
where c.customer_number = 'DEMO-1001'
  and p.code = 'DEMO-COMPREHENSIVE'
  and not exists (
      select 1 from policies existing
      where existing.customer_id = c.id and existing.product_id = p.id
  );

insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select p.id, current_date - 5, 'AUTO', 'Auffahrunfall A2', 3200.50, 'RECEIVED', current_timestamp, current_timestamp
from policies p
join customers c on c.id = p.customer_id
join products product on product.id = p.product_id
where c.customer_number = 'DEMO-1001'
  and product.code = 'DEMO-COMPREHENSIVE'
  and not exists (select 1 from claims where policy_id = p.id and description = 'Auffahrunfall A2');

insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select p.id, current_date - 3, 'LIABILITY', 'Wasserschaden Nachbar', 850.00, 'RECEIVED', current_timestamp, current_timestamp
from policies p
join customers c on c.id = p.customer_id
join products product on product.id = p.product_id
where c.customer_number = 'DEMO-1001'
  and product.code = 'DEMO-COMPREHENSIVE'
  and not exists (select 1 from claims where policy_id = p.id and description = 'Wasserschaden Nachbar');

insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select p.id, current_date - 1, 'AUTO', 'Parkschaden Supermarkt', 1200.00, 'RECEIVED', current_timestamp, current_timestamp
from policies p
join customers c on c.id = p.customer_id
join products product on product.id = p.product_id
where c.customer_number = 'DEMO-1001'
  and product.code = 'DEMO-COMPREHENSIVE'
  and not exists (select 1 from claims where policy_id = p.id and description = 'Parkschaden Supermarkt');
