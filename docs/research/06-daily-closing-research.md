# Daily closing and corrections

## Agreed operation

Financial transactions update the current ledger balance when processed. An intraday deficit alone triggers no daily fee.

**Project choice:** Run one daily job for the previous day, selecting input entries with `booking_date <= reference day`:

* Day 2: reference Day 1.
* Day 3: reference Day 2.
* Day 4: reference Day 3.
* Day 5: reference Day 4.
* Day 6: reference Day 5.

Include earlier bookings. Calculate a day's closing ledger balance from the opening balance and selected financial entries with `value_date <= that day`. Pending interest does not enter that balance. Booking selects the accounting cutoff; value date determines economic effect. Knowing an entry does not override the cutoff.

Preserve supplied dates and event order. E6's confirmed AED 180.00 debit affects Day 4 onward once included. E9 remains booked on Day 6 with Day 2 value date. E10 remains after E9, with both dates on Day 5. The cutoff does not prevent principal transactions from updating the current balance.

Interest accrues daily at 0.04% on positive closing ledger balances, using approved HALF_UP currency rounding. Holds neither reduce the interest base nor block capitalization. Confirmed settlement debits and matching hold changes remain separate.

**Payment choice:** Pay by the end of Day 6 using reference Day 5. Exclude E9 and adjustments booked on Day 6 even if already processed. Their position before or after the interest payment does not change this credit. The job's daily accrual and resulting payment are outputs, not input transactions subject to that cutoff. The payment is recorded on Day 6. The Day 7 job calculates Day 6 using Day 6's cumulative booking cutoff.

## Correcting a recorded day

For legitimate late transactions, keep the [difference method](02-booking-and-value-dates-research.md#approved-adjustment-method). Compare revised fees and interest separately with their original amounts plus **all previous adjustments**, including those already paid. Append only a nonzero difference; never repeat the principal.

For those late transaction adjustments, use the actual correction day for both dates. The [study 08 reversal refund exception](../deliverables/AMBIGUITIES.md#reversal-compensation) instead uses the original fee charge's value date and current booking; interest correction dates and pending treatment remain unchanged. Link it to the original transaction and retain a breakdown by historical day and type. Preserve earlier records and payments. Positive fee differences debit the ledger; negative differences refund.

**Approved interest treatment:** Keep the entire interest adjustment unpaid until the next regular payment whose booking cutoff includes it. This includes corrections for previously paid periods. Positive differences increase pending interest; negative differences reduce it. Neither creates an immediate ledger credit or debit.

Pending interest is unavailable and earns no interest. Do not calculate hypothetical interest on an adjustment as though it had been credited earlier. At payment, credit the exact sum of eligible unpaid daily accruals and adjustments, then record which components were paid. Exclude those components from later payments, but retain them when calculating future differences. This [decision](../deliverables/AMBIGUITIES.md#interest-adjustments-wait-for-payment) replaces immediate balance corrections for previously credited interest.

**Small check:** Revising 1.50 to 2.00 creates a pending 0.50 difference. Repeating gives `2.00 - (1.50 + 0.50) = 0.00`. The linked decision works through D30 and the next payment.

Queries append nothing. Corrections do not [automatically reevaluate authorizations](04-authorization-decisions-research.md#approved-policy-later-balance-corrections).

## Decisions still open

* **Reviews:** Proposed: after E7's late AED 620.00 debit, review ACC-001 Days 2 through 4 before E8; after E9's reversal, Days 2 through 5; after E10's BHD 10.000 credit, ACC-002 Day 5. Routine positions remain undecided. E8 does not close Day 5.
* **Completeness:** E10 can be processed after the job despite booking on Day 5. Handling missing eligible records is unresolved.
* **Payment limits:** Handling a negative total payable remains unresolved; no direct debit or carry rule is adopted. The [exercise](../exercise-statement.md) requires one credit at the end of Day 6. Day 6 interest is assumed payable later under the chosen cutoff; study 10 must define that payment and document the interpretation.
* **Clock and dates:** 00:00 boundary and 00:30 start remain proposals; time zone and ordinary assessment dates are unresolved.
* **Duplicate delivery:** Proposed: use the same event ID to prevent repeated financial or hold effects. Equal amounts and dates are insufficient.
* **Concurrency:** Proposed: validate entries and prior results relevant to the cutoff, then record indivisibly; retry if those changed. Excluded future bookings alone require no retry. Mechanism undecided.

Study 07 defines the approved fee assessment base. Study 08 approves recalculating all fees and interest affected by reversal and dates fee refunds at their original charge's value date, with current booking. Final fee results still depend on the unresolved recording points and ordinary assessment dates above and capitalization order in study 10. The negative BHD case remains for study 12; intermediate interest precision awaits study 09.

**Review status:** Study 06 is approved for now, with its recorded open items and dependencies explicitly pending. Revisit it when a later study affects these decisions.

## Sources and limits

[Microsoft's Event Sourcing pattern][events], “Pattern advantages,” covers validation and retry; “Versioning events” and “Idempotency requirements” cover compensation and duplicate effects. These technical precedents impose neither financial policy nor event sourcing infrastructure.

[events]: https://learn.microsoft.com/en-us/azure/architecture/patterns/event-sourcing
