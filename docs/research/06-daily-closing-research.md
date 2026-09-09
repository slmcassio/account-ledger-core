# Daily closing and corrections

## Agreed operation

Financial transactions update the current ledger balance when processed. An intraday deficit alone triggers no daily fee.

Run one daily job in D for reference D-1: Day 2 references Day 1, through Day 6 referencing Day 5. Select inputs cumulatively with `booking_date <= D-1`. Calculate a day's balance from the opening balance and selected entries with `value_date <= that day`. Pending interest is excluded. Booking selects eligible inputs; value date determines economic effect. Earlier receipt never admits a future booking.

Preserve supplied dates and event order: E6's confirmed AED 180.00 debit affects Day 4 onward once included; E9 credits AED 620.00 with booking Day 6 and value Day 2; E10 follows E9 with both dates Day 5. Principal transactions still update the current balance normally. Holds neither reduce the interest base nor block capitalization; settlement debits and hold changes remain separate.

The Day 6 payment references Day 5. E9 and corrections booked Day 6 cannot change it, even if processed first. The job's ordinary accrual and payment are outputs, not excluded inputs; this exception does not admit those corrections. Day 7 calculates Day 6. [Study 10](10-interest-capitalization-research.md) covers payment composition, dates and the monthly schedule.

## Receipt and missing inputs

Record actual system receipt separately from booking and value dates, without inventing receipt moments for the replay. Precision, time zone and tie handling remain unspecified. [Receipt metadata](../deliverables/AMBIGUITIES.md#booking-and-value-dates) explains which events arrived; it neither overrides the cutoff nor resolves input completeness.

E10 can arrive after the job despite its eligible Day 5 booking. Recommend placing the replay's final calculation after E10 while preserving earlier active calculations. Alternatively, calculate available inputs and correct later. Neither checkpoint is approved. Waiting only to pay cannot admit a correction booked Day 6. Receipt and validation during processing remain separate concerns.

## Correcting a recorded day

For legitimate late transactions, compare revised fees and [rounded daily interest](09-daily-interest-research.md) separately with originals plus **all earlier adjustments, including paid ones**. Append only a nonzero difference, never the principal again. Preserve records and payments; link adjustments to the transaction with a breakdown by historical day and type.

Use the actual correction day for both adjustment dates. [Reversal fee refunds](../deliverables/AMBIGUITIES.md#reversal-compensation) are the exception: current booking, original fee value date. Interest correction dates are unchanged. Positive fee differences debit; negative differences refund. Interest differences, including corrections of paid periods, [remain pending until eligible payment](10-interest-capitalization-research.md), without immediate ledger effects.

**Small check:** An AED daily amount revised from 1.50 to 2.00 creates +0.50. Repeating gives `2.00 - (1.50 + 0.50) = 0.00`, even after that adjustment is paid.

Queries append nothing. Corrections never [automatically reevaluate authorizations](04-authorization-decisions-research.md#approved-policy-later-balance-corrections).

## Decisions still open

* **Reviews:** Proposed after E7: ACC-001 Days 2 through 4 before E8; after E9: Days 2 through 5; after E10: ACC-002 Day 5. Ordinary checkpoints remain open. E8 does not close Day 5.
* **Clock and dates:** Midnight and 00:30 start remain proposals; time zone and ordinary fee assessment dates are unresolved.
* **Duplicates:** Proposed same event ID for repeat detection; equal amounts and dates are insufficient.
* **Concurrency:** Proposed validation of eligible inputs and prior results, followed by indivisible recording and retry if they changed. Excluded future bookings alone require no retry. Mechanism undecided.

[Study 07](07-overdraft-fees-research.md) defines fee bases; [08](08-reversals-research.md) defines reversal compensation; [09](09-daily-interest-research.md) defines exact daily calculation. Final E7 fee counts and replay totals remain open. Old examples 04 and 08 establish no current totals. Study 08's simulation establishes no calendar. Payment limits remain in [10](10-interest-capitalization-research.md), and negative BHD fees in study 12.

**Review status:** Study 06 remains approved with these dependencies explicitly pending.

## Sources and limits

[Microsoft's Event Sourcing pattern][events], “Pattern advantages,” covers validation and retry; “Versioning events” and “Idempotency requirements” cover compensation and duplicates. These technical precedents impose neither financial policy nor event sourcing infrastructure.

[events]: https://learn.microsoft.com/en-us/azure/architecture/patterns/event-sourcing
