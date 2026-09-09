# Daily closing and corrections

## Agreed operation

Financial transactions update the current ledger balance when processed. An intraday deficit alone triggers no daily fee.

Run one daily job in D for reference D-1: Day 2 references Day 1, through Day 6 referencing Day 5. Select inputs cumulatively with `booking_date <= D-1`. Calculate a day's balance from the opening balance and selected entries with `value_date <= that day`. Pending interest is excluded. Booking selects eligible inputs; value date determines economic effect. Earlier receipt never admits a future booking.

Preserve supplied dates and event order: E6's confirmed AED 180.00 debit affects Day 4 onward once included; E9 credits AED 620.00 with booking Day 6 and value Day 2; E10 follows E9 with both dates Day 5. Principal transactions still update the current balance normally. Holds neither reduce the interest base nor block capitalization; settlement debits and hold changes remain separate.

The Day 6 payment references Day 5. E9 and corrections booked Day 6 cannot change it, even if processed first. The job's ordinary accrual and payment are outputs, not excluded inputs; this exception does not admit those corrections. Day 7 calculates Day 6. [Study 10](10-interest-capitalization-research.md#approved-payment-and-components) covers payment composition, dates and the monthly schedule.

## Receipt and missing inputs

Record actual system receipt separately from booking and value dates, without inventing receipt moments for the replay. Precision, time zone and tie handling remain unspecified. Receipt metadata identifies arrivals; it neither overrides the cutoff nor resolves input completeness.

E10 can arrive after the job despite its eligible Day 5 booking. Recommend placing the replay's final calculation after E10 while preserving earlier active calculations. Alternatively, calculate available inputs and correct later. Neither checkpoint is approved. Waiting only to pay cannot admit a correction booked Day 6. Receipt and validation during processing remain separate concerns.

## Correcting a recorded day

Apply [study 02's incremental adjustment method](02-booking-and-value-dates-research.md#approved-adjustment-method) separately to fees and [rounded daily interest](09-daily-interest-research.md#corrections-and-payment). Append only nonzero differences. Positive fee differences debit; negative differences refund. The method preserves prior results, including paid adjustments, and defines correction dates with the reversal fee refund exception.

**Small check:** An AED daily amount revised from 1.50 to 2.00 creates +0.50. Repeating gives `2.00 - (1.50 + 0.50) = 0.00`, even after that adjustment is paid.

Queries append nothing. Corrections never [automatically reevaluate authorizations](04-authorization-decisions-research.md#approved-policy-later-balance-corrections).

## Decisions still open

* **Reviews:** Proposed after E7: ACC-001 Days 2 through 4 before E8; after E9: Days 2 through 5; after E10: ACC-002 Day 5. Ordinary checkpoints remain open. E8 does not close Day 5.
* **Clock and dates:** Midnight and 00:30 start remain proposals; time zone and ordinary fee assessment dates are unresolved.
* **Duplicates:** Proposed same event ID for repeat detection; equal amounts and dates are insufficient.
* **Concurrency:** Proposed validation of eligible inputs and prior results, followed by indivisible recording and retry if they changed. Excluded future bookings alone require no retry. Mechanism undecided.

Final E7 fee counts and replay totals remain open. Legacy examples [04](../examples/04-backdated-adjustment.md) and [08](../examples/08-daily-closing.md), outside research, establish no current totals. [Study 08's simulation](examples/08-reversal-15-day-simulation.md#inputs-and-assumed-schedule) establishes no calendar. Fee bases follow [07](07-overdraft-fees-research.md#rule-and-approved-base), configured amounts follow [12](12-fee-currency-research.md#rule-and-approved-exception), and payment limits remain in [10](10-interest-capitalization-research.md#remaining-questions).

**Review status:** Study 06 remains approved with these dependencies explicitly pending.

## Sources and limits

[Microsoft's Event Sourcing pattern][events], “Pattern advantages,” covers validation and retry; “Versioning events” and “Idempotency requirements” cover compensation and duplicates. These technical precedents impose neither financial policy nor event sourcing infrastructure.

[events]: https://learn.microsoft.com/en-us/azure/architecture/patterns/event-sourcing
