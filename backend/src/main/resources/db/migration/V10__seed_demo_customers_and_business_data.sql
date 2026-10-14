-- Synthetic demonstration data for the live environment. Names and .test emails
-- are fictional; existing customer, product, and business records are untouched.
with demo_customers(customer_number, display_name, email) as (
    values
        ('DEMO-1001', 'Anna Schneider', 'anna.schneider.1001@example.test'),
        ('DEMO-1002', 'Jonas Weber', 'jonas.weber.1002@example.test'),
        ('DEMO-1003', 'Lena Fischer', 'lena.fischer.1003@example.test'),
        ('DEMO-1004', 'Matteo Wagner', 'matteo.wagner.1004@example.test'),
        ('DEMO-1005', 'Emilia Becker', 'emilia.becker.1005@example.test'),
        ('DEMO-1006', 'Leon Hoffmann', 'leon.hoffmann.1006@example.test'),
        ('DEMO-1007', 'Mia Schulz', 'mia.schulz.1007@example.test'),
        ('DEMO-1008', 'Elias Koch', 'elias.koch.1008@example.test'),
        ('DEMO-1009', 'Clara Richter', 'clara.richter.1009@example.test'),
        ('DEMO-1010', 'Ben Wolf', 'ben.wolf.1010@example.test'),
        ('DEMO-1011', 'Sophia Neumann', 'sophia.neumann.1011@example.test'),
        ('DEMO-1012', 'Paul Schwarz', 'paul.schwarz.1012@example.test'),
        ('DEMO-1013', 'Amelie Zimmermann', 'amelie.zimmermann.1013@example.test'),
        ('DEMO-1014', 'Noah Braun', 'noah.braun.1014@example.test'),
        ('DEMO-1015', 'Hannah Krueger', 'hannah.krueger.1015@example.test'),
        ('DEMO-1016', 'Felix Hartmann', 'felix.hartmann.1016@example.test'),
        ('DEMO-1017', 'Emma Lange', 'emma.lange.1017@example.test'),
        ('DEMO-1018', 'Luis Werner', 'luis.werner.1018@example.test'),
        ('DEMO-1019', 'Marie Schmitz', 'marie.schmitz.1019@example.test'),
        ('DEMO-1020', 'Oskar Krause', 'oskar.krause.1020@example.test'),
        ('DEMO-1021', 'Laura Meier', 'laura.meier.1021@example.test'),
        ('DEMO-1022', 'Emil Lehmann', 'emil.lehmann.1022@example.test'),
        ('DEMO-1023', 'Johanna Schmid', 'johanna.schmid.1023@example.test'),
        ('DEMO-1024', 'Theo Schulze', 'theo.schulze.1024@example.test'),
        ('DEMO-1025', 'Lina Maier', 'lina.maier.1025@example.test'),
        ('DEMO-1026', 'David Keller', 'david.keller.1026@example.test'),
        ('DEMO-1027', 'Charlotte Frank', 'charlotte.frank.1027@example.test'),
        ('DEMO-1028', 'Anton Peters', 'anton.peters.1028@example.test'),
        ('DEMO-1029', 'Ida Berger', 'ida.berger.1029@example.test'),
        ('DEMO-1030', 'Jakob Roth', 'jakob.roth.1030@example.test'),
        ('DEMO-1031', 'Mathilda Jansen', 'mathilda.jansen.1031@example.test'),
        ('DEMO-1032', 'Samuel Lorenz', 'samuel.lorenz.1032@example.test'),
        ('DEMO-1033', 'Frieda Graf', 'frieda.graf.1033@example.test'),
        ('DEMO-1034', 'Vincent Neubert', 'vincent.neubert.1034@example.test'),
        ('DEMO-1035', 'Nora Braun', 'nora.braun.1035@example.test'),
        ('DEMO-1036', 'Tom Seidel', 'tom.seidel.1036@example.test'),
        ('DEMO-1037', 'Elisa Busch', 'elisa.busch.1037@example.test'),
        ('DEMO-1038', 'Mats Krueger', 'mats.krueger.1038@example.test'),
        ('DEMO-1039', 'Greta Vogel', 'greta.vogel.1039@example.test'),
        ('DEMO-1040', 'Erik Sommer', 'erik.sommer.1040@example.test'),
        ('DEMO-1041', 'Valerie Otto', 'valerie.otto.1041@example.test'),
        ('DEMO-1042', 'Max Brandt', 'max.brandt.1042@example.test'),
        ('DEMO-1043', 'Lotta Fuchs', 'lotta.fuchs.1043@example.test'),
        ('DEMO-1044', 'Daniel Schreiber', 'daniel.schreiber.1044@example.test'),
        ('DEMO-1045', 'Zoe Winter', 'zoe.winter.1045@example.test'),
        ('DEMO-1046', 'Moritz Haas', 'moritz.haas.1046@example.test'),
        ('DEMO-1047', 'Pauline Dietrich', 'pauline.dietrich.1047@example.test'),
        ('DEMO-1048', 'Linus Schwarz', 'linus.schwarz.1048@example.test'),
        ('DEMO-1049', 'Amalia Frank', 'amalia.frank.1049@example.test'),
        ('DEMO-1050', 'Jan Peters', 'jan.peters.1050@example.test')
)
insert into customers (customer_number, display_name, email, active)
select customer_number, display_name, email, true
from demo_customers
on conflict do nothing;

insert into products (code, name, coverage_limit, premium, active)
values
    ('DEMO-COMPREHENSIVE', 'Privat-Komfortschutz', 100000.00, 49.90, true),
    ('AUTO-KOMFORT', 'Kfz-Versicherung Komfort', 120000.00, 684.00, true),
    ('HAUSRAT-PLUS', 'Hausratversicherung Plus', 100000.00, 129.00, true),
    ('PRIVAT-HAFTPFLICHT', 'Private Haftpflichtversicherung', 10000000.00, 68.00, true),
    ('RISIKO-LEBEN', 'Risikolebensversicherung', 300000.00, 420.00, true)
on conflict do nothing;

insert into policies (customer_id, product_id, valid_from, valid_to, coverage_limit, premium, status)
select c.id,
       p.id,
       current_date - (165 + mod(cast(substring(c.customer_number, 6) as integer), 120)),
       current_date + (200 - mod(cast(substring(c.customer_number, 6) as integer), 120)),
       p.coverage_limit,
       p.premium,
       'ACTIVE'
from customers c
join products p on p.code = case
    when c.customer_number = 'DEMO-1001' then 'DEMO-COMPREHENSIVE'
    when cast(substring(c.customer_number, 6) as integer) <= 1013 then 'AUTO-KOMFORT'
    when cast(substring(c.customer_number, 6) as integer) <= 1025 then 'HAUSRAT-PLUS'
    when cast(substring(c.customer_number, 6) as integer) <= 1037 then 'PRIVAT-HAFTPFLICHT'
    when cast(substring(c.customer_number, 6) as integer) <= 1049 then 'RISIKO-LEBEN'
    else 'AUTO-KOMFORT'
end
where c.customer_number between 'DEMO-1001' and 'DEMO-1050'
  and not exists (
      select 1
      from policies existing
      where existing.customer_id = c.id and existing.product_id = p.id
  );

insert into policies (customer_id, product_id, valid_from, valid_to, coverage_limit, premium, status)
select c.id, p.id, current_date - 45, current_date + 320, p.coverage_limit, p.premium, 'ACTIVE'
from (
    values
        ('DEMO-1002', 'HAUSRAT-PLUS'),
        ('DEMO-1003', 'PRIVAT-HAFTPFLICHT'),
        ('DEMO-1004', 'RISIKO-LEBEN'),
        ('DEMO-1005', 'HAUSRAT-PLUS'),
        ('DEMO-1006', 'PRIVAT-HAFTPFLICHT'),
        ('DEMO-1007', 'RISIKO-LEBEN'),
        ('DEMO-1008', 'HAUSRAT-PLUS'),
        ('DEMO-1009', 'PRIVAT-HAFTPFLICHT'),
        ('DEMO-1010', 'RISIKO-LEBEN'),
        ('DEMO-1011', 'HAUSRAT-PLUS'),
        ('DEMO-1012', 'PRIVAT-HAFTPFLICHT'),
        ('DEMO-1013', 'RISIKO-LEBEN'),
        ('DEMO-1014', 'AUTO-KOMFORT'),
        ('DEMO-1015', 'RISIKO-LEBEN'),
        ('DEMO-1016', 'PRIVAT-HAFTPFLICHT')
) seed(customer_number, product_code)
join customers c on c.customer_number = seed.customer_number
join products p on p.code = seed.product_code
where not exists (
    select 1
    from policies existing
    where existing.customer_id = c.id and existing.product_id = p.id
);

insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select p.id, current_date - 5, 'AUTO', 'Auffahrunfall auf der A2', 3200.50, 'RECEIVED',
       current_timestamp - interval '5 days', current_timestamp - interval '5 days'
from policies p
join customers c on c.id = p.customer_id
join products product on product.id = p.product_id
where c.customer_number = 'DEMO-1001'
  and product.code = 'DEMO-COMPREHENSIVE'
  and not exists (
      select 1 from claims existing
      where existing.policy_id = p.id and existing.description = 'Auffahrunfall auf der A2'
  );

insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select p.id, current_date - 3, 'LIABILITY', 'Wasserschaden in der Nachbarwohnung', 850.00, 'IN_REVIEW',
       current_timestamp - interval '3 days', current_timestamp - interval '2 days'
from policies p
join customers c on c.id = p.customer_id
join products product on product.id = p.product_id
where c.customer_number = 'DEMO-1001'
  and product.code = 'DEMO-COMPREHENSIVE'
  and not exists (
      select 1 from claims existing
      where existing.policy_id = p.id and existing.description = 'Wasserschaden in der Nachbarwohnung'
  );

insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select p.id, current_date - 1, 'AUTO', 'Parkschaden auf einem Supermarktparkplatz', 1200.00, 'RECEIVED',
       current_timestamp - interval '1 day', current_timestamp - interval '1 day'
from policies p
join customers c on c.id = p.customer_id
join products product on product.id = p.product_id
where c.customer_number = 'DEMO-1001'
  and product.code = 'DEMO-COMPREHENSIVE'
  and not exists (
      select 1 from claims existing
      where existing.policy_id = p.id and existing.description = 'Parkschaden auf einem Supermarktparkplatz'
  );

with demo_claims as (
    select c.id as customer_id,
           c.customer_number,
           p.id as policy_id,
           product.code as product_code,
           cast(substring(c.customer_number, 6) as integer) as customer_sequence,
           case product.code
               when 'AUTO-KOMFORT' then 'AUTO'
               when 'HAUSRAT-PLUS' then 'HOME'
               when 'PRIVAT-HAFTPFLICHT' then 'LIABILITY'
               else 'LIFE'
           end as claim_type,
           case product.code
               when 'AUTO-KOMFORT' then case mod(cast(substring(c.customer_number, 6) as integer), 3)
                   when 0 then 'Auffahrunfall im Stadtverkehr'
                   when 1 then 'Steinschlag an der Windschutzscheibe'
                   else 'Parkschaden am abgestellten Fahrzeug'
               end
               when 'HAUSRAT-PLUS' then case mod(cast(substring(c.customer_number, 6) as integer), 3)
                   when 0 then 'Leitungswasserschaden in der Küche'
                   when 1 then 'Einbruch und beschädigte Wohnungstür'
                   else 'Sturmschaden am Balkon'
               end
               when 'PRIVAT-HAFTPFLICHT' then case mod(cast(substring(c.customer_number, 6) as integer), 3)
                   when 0 then 'Beschädigtes Mobiltelefon eines Dritten'
                   when 1 then 'Wasserschaden in der Nachbarwohnung'
                   else 'Beschädigtes Fahrrad bei einem Zusammenstoß'
               end
               else 'Leistungsantrag aus der Risikolebensversicherung'
           end as description,
           case product.code
               when 'AUTO-KOMFORT' then 450.00 + mod(cast(substring(c.customer_number, 6) as integer), 6) * 175.00
               when 'HAUSRAT-PLUS' then 700.00 + mod(cast(substring(c.customer_number, 6) as integer), 6) * 850.00
               when 'PRIVAT-HAFTPFLICHT' then 320.00 + mod(cast(substring(c.customer_number, 6) as integer), 6) * 950.00
               else 25000.00 + mod(cast(substring(c.customer_number, 6) as integer), 6) * 5000.00
           end as amount
    from customers c
    join products product on product.code = case
        when cast(substring(c.customer_number, 6) as integer) <= 1013 then 'AUTO-KOMFORT'
        when cast(substring(c.customer_number, 6) as integer) <= 1025 then 'HAUSRAT-PLUS'
        when cast(substring(c.customer_number, 6) as integer) <= 1037 then 'PRIVAT-HAFTPFLICHT'
        when cast(substring(c.customer_number, 6) as integer) <= 1049 then 'RISIKO-LEBEN'
        else 'AUTO-KOMFORT'
    end
    join policies p on p.customer_id = c.id and p.product_id = product.id
    where c.customer_number between 'DEMO-1002' and 'DEMO-1050'
      and mod(cast(substring(c.customer_number, 6) as integer), 2) = 0
)
insert into claims (policy_id, occurred_on, claim_type, description, amount, status, created_at, updated_at)
select demo_claims.policy_id,
       current_date - (15 + mod(customer_sequence, 150)),
       claim_type,
       description,
       amount,
       case mod(customer_sequence, 4)
           when 0 then 'RECEIVED'
           when 1 then 'IN_REVIEW'
           when 2 then 'APPROVED'
           else 'REJECTED'
       end,
       current_timestamp - ((15 + mod(customer_sequence, 150)) * interval '1 day'),
       current_timestamp - ((8 + mod(customer_sequence, 150)) * interval '1 day')
from demo_claims
where not exists (
    select 1
    from claims existing
    where existing.policy_id = demo_claims.policy_id
      and existing.description = demo_claims.description
);

insert into invoices (policy_id, customer_id, amount, currency, status)
select p.id, p.customer_id, p.premium, 'EUR',
       case when mod(p.id, 3) = 0 then 'PAID' else 'OPEN' end
from policies p
join customers c on c.id = p.customer_id
where c.customer_number between 'DEMO-1001' and 'DEMO-1050'
  and not exists (
      select 1 from invoices existing where existing.policy_id = p.id
  );

insert into payouts (claim_id, policy_id, amount, currency, status)
select c.id, c.policy_id, c.amount, 'EUR', 'EXECUTED'
from claims c
join policies p on p.id = c.policy_id
join customers customer on customer.id = p.customer_id
where customer.customer_number between 'DEMO-1001' and 'DEMO-1050'
  and c.status = 'APPROVED'
  and not exists (
      select 1 from payouts existing where existing.claim_id = c.id
  );
