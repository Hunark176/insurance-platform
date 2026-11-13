-- Cancellation with an effective date (loss-occurrence principle).
--
-- Until now a cancelled policy counted as never covering anything: coverage was
-- checked against status = 'ACTIVE', so open claims for losses that occurred
-- before the cancellation could no longer be approved, and such losses could
-- not be reported afterwards. Coverage is now determined by the loss date
-- alone: from valid_from up to and including the earlier of valid_to and
-- cancelled_on (the last covered day).

alter table policies add column cancelled_on date;

-- Policies cancelled before this migration have no recorded cancellation date.
-- None exist in the demo data. If there are any, the migration date is used as
-- an upper bound so losses during the term stay covered; correct the value
-- manually if the actual cancellation date is known.
update policies
set cancelled_on = least(current_date, valid_to)
where status = 'CANCELLED';

-- A cancellation date exists exactly for cancelled policies and never lies
-- after the end of the term.
alter table policies
    add constraint ck_policies_cancellation
        check ((status = 'CANCELLED') = (cancelled_on is not null)
            and (cancelled_on is null or cancelled_on <= valid_to));
