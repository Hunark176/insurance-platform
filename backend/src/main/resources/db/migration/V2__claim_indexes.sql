create index if not exists idx_claims_customer_number on claims(customer_number);
create index if not exists idx_claims_policy_id on claims(policy_id);
create index if not exists idx_claims_status_created_at on claims(status, created_at);
