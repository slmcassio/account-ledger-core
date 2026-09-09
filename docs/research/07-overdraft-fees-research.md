# Daily overdraft fee assessment

**Source:** Research from `main` at `ec4e20cc9eb9f788e5195be574b384ec97108f32`, reconciled with the local architectural decisions.

**Integration status:** The assessment base is adopted in [AMBIGUITIES](../deliverables/AMBIGUITIES.md#overdraft-fee-assessment-base). The ordinary assessment dates below are decisions from the source review that still await incorporation into this worktree's deliverables. The [account type fee configuration and zero BHD fee](../deliverables/AMBIGUITIES.md#overdraft-fee-currency) are adopted.

## Rule and approved base

The [exercise](../exercise-inputs/exercise-statement.md#nonnegotiable-rules) requires AED 25.00 per negative closing day per account, with the assessment day as the fee's value date. Zero incurs no fee. [Study 12](12-fee-currency-research.md) records the [adopted exception](../deliverables/AMBIGUITIES.md#overdraft-fee-currency): fees configured by account type in its own currency, with AED 25.00 for ACC-001's type and BHD 0.000 for ACC-002's type.

Calculate historical day H using [study 06's booking cutoff and value-date filters](06-daily-closing-research.md#agreed-operation). Holds and pending interest have no ledger effect.

**Approved choice:** Remove only H's own included fee components and adjustments. Keep other periods' charges and refunds at their actual dates. Charge only if this base is negative, preventing a fee from sustaining itself. The reported ledger balance still retains all entries passing both filters.

## Approved assessment dates

Assess the ordinary fee for historical day H on H+1, setting booking and value dates to that actual assessment day. This follows the daily job's schedule: H identifies the balance period, while the fee starts affecting funds when assessed. Corrections use the dates below.

## Corrections

Apply [study 02's adjustment method](02-booking-and-value-dates-research.md#approved-adjustment-method) to each account and day:

`corrected fee - (original fee + all earlier adjustments)`

A positive difference debits; a negative difference refunds. **All earlier adjustments count even when excluded from the historical balance.** Repeating unchanged targets yields zero. Review days chronologically. Both adjustment dates are the correction day except for [reversal fee refunds](08-reversals-research.md#correcting-fees-and-interest-after-a-reversal), which retain the original fee value date.

## Calculation snapshots

Both hypothetical AED accounts open at 0.00, before capitalization, with no holds or unlisted financial entries. Assume complete inputs for a Day 6 job referencing Day 5.

**Late debit.** Credits of 100.00 and 20.00 have matching booking and value dates on Days 1 and 2, respectively. A 110.00 debit has booking Day 5 and value Day 1. Neither period has prior fees.

Day 1's base becomes `100.00 - 110.00 = -10.00`; Day 2's becomes `100.00 + 20.00 - 110.00 = 10.00`. Their fee targets are 25.00 and zero. If assessed on Day 6, the 25.00 adjustment has both dates on Day 6 and changes neither historical balance.

**Earlier period's fee.** A 10.00 debit has both dates on Day 1. Its 25.00 fee was assessed, booked, and valued on Day 2. A 20.00 credit has booking Day 5 and value Day 1. There are no prior adjustments.

Day 1's base is `-10.00 + 20.00 = 10.00`: the fee's value date already excludes it. Its target becomes zero, requiring a 25.00 refund, with both dates Day 6 if assessed then. Day 2's base remains `-10.00 - 25.00 + 20.00 = -15.00`. That other period's fee remains effective; the refund does not backdate.

## Pending decisions

Final fee counts and balances depend on [the open calculation decisions](06-daily-closing-research.md#decisions-still-open), applying [the approved payment dates](10-interest-capitalization-research.md#approved-capitalization-date). These isolated examples establish no final replay totals; [NUMBERS](../deliverables/NUMBERS.md#values-used-in-the-overdraft-fee-snapshots) records their inputs.

## Sources and limits

[Mambu's date documentation][dates], “Available transaction dates” and “Booking date input under accounting closure,” distinguishes dates and permits some historical adjustments. It establishes neither this project's assessment base nor its correction dates.

[dates]: https://docs.mambu.com/docs/booking-date-vs-value-date/#booking-date-input-under-accounting-closure
