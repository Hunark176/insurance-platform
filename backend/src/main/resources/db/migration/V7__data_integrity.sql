alter table products add constraint ck_products_amounts check (coverage_limit >= 0 and premium >= 0);
alter table policies add constraint ck_policies_dates check (valid_to > valid_from);
alter table claims add constraint ck_claims_amount check (amount >= 0);
alter table payouts add constraint ck_payouts_amount check (amount >= 0);
