# Daily overdraft fee assessment

## Rule and approved base

The [exercise](../exercise-statement.md#nonnegotiable-rules) requires AED 25.00 per negative closing day per account, with the assessment day as the fee's value date. Zero incurs no fee. [Study 12](12-fee-currency-research.md) records the approved exception: fees configured by account type in its own currency, with AED 25.00 for ACC-001's type and BHD 0.000 for ACC-002's type.

For the job in D, select inputs cumulatively by `booking_date <= D-1`, then calculate historical day H from the opening balance and entries with `value_date <= H`. Holds and pending interest, including corrections, have no ledger effect.

**Approved choice:** Remove only H's own fee components and adjustments already included in that balance. Keep other periods' charges and refunds at their actual dates. Use the configured fee only when this base is negative. This prevents a fee from sustaining itself. It changes the assessment base, not the reported ledger balance, which retains all entries passing both filters.

## Corrections

Under the [approved adjustment policy](../deliverables/AMBIGUITIES.md#late-transaction-adjustments), preserve the legitimate late transaction's dates. For each account and day, calculate:

`corrected fee - (original fee + all earlier adjustments)`

A positive difference debits the ledger; a negative difference refunds. Append a linked adjustment with both dates on the actual correction day and historical components in its breakdown. All earlier adjustments count in this comparison, even when excluded from the historical balance. Repeating unchanged targets yields zero. Review days chronologically without backdating these late transaction adjustments. [Reversal fee refunds](../deliverables/AMBIGUITIES.md#reversal-compensation) are the limited exception: current booking, original charge's value date. Interest correction dates and pending treatment remain unchanged.

## Calculation snapshots

Both hypothetical AED accounts open at 0.00, before capitalization, with no holds or financial entries beyond those listed. Assume complete inputs for a Day 6 job referencing Day 5. These examples choose no ordinary schedule or replay checkpoints.

**Late debit.** Credits of 100.00 and 20.00 have matching booking and value dates on Days 1 and 2, respectively. A 110.00 debit has booking Day 5 and value Day 1. Neither period has prior fees.

Day 1's base becomes `100.00 - 110.00 = -10.00`; Day 2's becomes `100.00 + 20.00 - 110.00 = 10.00`. Their fee targets are 25.00 and zero. If assessed on Day 6, the 25.00 adjustment has both dates on Day 6 and changes neither historical balance.

**Earlier period's fee.** A 10.00 debit has both dates on Day 1. Its existing 25.00 fee belongs to Day 1 but was assessed, booked, and valued on Day 2, an illustrative date. A 20.00 credit has booking Day 5 and value Day 1. There are no prior adjustments.

Day 1's base is `-10.00 + 20.00 = 10.00`: the fee's value date already excludes it. Its target becomes zero, requiring a 25.00 refund, with both dates Day 6 if assessed then. Day 2's base remains `-10.00 - 25.00 + 20.00 = -15.00`. That other period's fee remains effective; the refund does not backdate.

## Pending decisions

Study 06 leaves checkpoints, ordinary assessment dates, and missing eligible inputs unresolved. The final fee count after E7 remains open; its principal calculations are in [NUMBERS](../deliverables/NUMBERS.md#values-used-in-the-overdraft-fee-snapshots). E9's Day 6 booking excludes it from the Day 5 cutoff; E10 stays after E9.

Study 08 now defines reversal compensation. [Study 10](10-interest-capitalization-research.md) now defines payment dates and their effect on later bases. These snapshots establish neither final balances nor total fees.

## Sources and limits

[Mambu's date documentation][dates], “Available transaction dates” and “Booking date input under accounting closure,” distinguishes dates and permits some historical adjustments. It establishes neither this project's assessment base nor its correction dates.

[dates]: https://docs.mambu.com/docs/booking-date-vs-value-date/#booking-date-input-under-accounting-closure
