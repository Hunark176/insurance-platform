-- Keep applied migrations immutable; follow-up schema changes belong in a new version.
alter table claims drop column if exists customer_number;
