# Principal reversals and financial corrections

## What A and B do

**Approved: method B.** Recalculate all affected fees and interest from the affected value day onward, preserving the principal reversal once and recording only differences. The comparison below explains the choice: both methods reverse principal at its original value date; **only the fee refund's value date differs**.

* **A, not adopted: effective on the correction day.** The refund returns funds now; earlier fees remain in past balances.
* **B, approved: effective on the original fee date.** The refund returns funds now and offsets that fee in reconstructed past balances.

Both record refunds on the actual correction day. B neutralizes the fee at its original value date while preserving when funds were returned. Returning only principal was not selected.

## One fee, two outcomes

Open with AED 2,500.00. Debit 3,000.00 with booking/value Day 5. Reverse it with booking Day 9 and value Day 5.

Focus on the 25.00 fee for Day 5, charged with booking/value Day 6. Both methods refund it on Day 9. With principal cancelled, Day 6 has 2,500.00 before this fee:

| Question | A: current date | B: original fee date |
|---|---|---|
| Refund recorded on | Day 9 | Day 9 |
| Refund effective from | Day 9 | Day 6 |
| Corrected Day 6 balance | 2,500 - 25 = 2,475.00 | 2,500 - 25 + 25 = 2,500.00 |
| Day 6 interest at 0.04% | 0.99 | 1.00 |

These are reconstructed balances and historical calculations, not funds delivered in the past. Both record and make the refund available on Day 9; B uses Day 6 only as its economic date, without rewriting earlier credits. Every interest difference remains pending until the next eligible regular payment, including corrections of interest already paid.

This follows one of four fees. The others have value dates after Day 6; all four produce a 100.00 refund in the full Day 9 example. The [complete calculation](examples/08-reversal-15-day-simulation.md) shows every event and corrected day.

## Before or after payment

The full example compares reversal on Day 9 or Day 12, with one payment on Day 10 and consultation on Day 15. It uses a 25.00 daily fee, 0.04% daily interest and HALF_UP rounding. Its [schedule and calculation assumptions](examples/08-reversal-15-day-simulation.md#inputs-and-assumed-schedule) are illustrative.

| Reversal / method | Paid Day 10 | Balance Day 10 | Balance Day 15 | Pending Day 15 |
|---|---|---|---|---|
| Day 9 / A | 8.94 | 2,508.94 | 2,508.94 | 5.00 |
| Day 9 / B | 9.00 | 2,509.00 | 2,509.00 | 5.00 |
| Day 12 / A | 4.00 | -621.00 | 2,504.00 | 9.79 |
| Day 12 / B | 4.00 | -621.00 | 2,504.00 | 10.00 |

With reversal on Day 9, eligible corrections join ordinary interest on Day 10: A pays `5.00 + 3.94`; B pays `5.00 + 4.00`. With reversal on Day 12, the 4.00 already paid stays unchanged; corrections remain pending. Day 15 pays nothing.

## Fixed rules

Every correction concerns a legitimate transaction; error correction is excluded to simplify the exercise.

The [supplied E9](../exercise-statement.md) credits AED 620.00 to reverse E7, with booking Day 6 and value Day 2. Preserve all records and their order, including E10 after E9 and E6's confirmed AED 180.00 debit.

The [job in D](06-daily-closing-research.md#agreed-operation) selects `booking_date <= D-1`, then applies value dates. E9 and adjustments booked on Day 6 cannot change the Day 6 payment referencing Day 5, even if processed first.

Under [study 07](07-overdraft-fees-research.md#rule-and-approved-base), exclude only the evaluated period's own fee components and adjustments already included in its base. Keep other periods' charges and refunds at their actual dates.

Append only `corrected amount - (original + all earlier adjustments)`, including paid adjustments, with links to the affected components. Fee differences affect funds. [Interest differences stay pending](../deliverables/AMBIGUITIES.md#interest-adjustments-wait-for-payment), unavailable and earning nothing; settle each once. Preserve HALF_UP and earlier payments.

## Approved decisions and remaining limits

* **Scope:** recalculate every affected fee and daily interest component from the affected value day onward, within the applicable calculation boundary. This does not automatically refund every fee.
* **Dates:** book each reversal fee refund on the actual correction day and use the original charge's value date. This is a limited exception to the [late transaction policy](../deliverables/AMBIGUITIES.md#late-transaction-adjustments). Interest corrections retain both dates on the actual correction day and remain pending. See the [approved resolution](../deliverables/AMBIGUITIES.md#reversal-compensation).

Restoring principal, historical balances, net fees, interest and authorizations are different outcomes. Criterion 6's blanket restoration claim is unsupported. [Authorizations](../deliverables/AMBIGUITIES.md#authorization-and-ledger-responsibilities) are not automatically reevaluated.

Study 06 remains approved with checkpoints, ordinary assessment dates, missing eligible inputs and the final E7 fee count open. Old examples 04 and 08 establish no current replay totals. [Study 09](09-daily-interest-research.md#rule-and-approved-calculation) now approves exact multiplication and one daily currency rounding, without carrying fractions between days. That separate approval does not settle the open bases. [Study 10](10-interest-capitalization-research.md) now approves actual payment dates and monthly payment on the first business day for the previous month. Its calendar mapping, capitalization amounts and final balances remain unresolved. Study 08 remains approved with these dependencies pending; its simulation supplies no calendar policy. No negative payable policy is adopted.

## Sources and limits

[ISO 20022][iso], camt.053.001.08 §3.4.2.15.4, p. 61, defines opposite reversal directions, not compensation entitlement. [Canopy][canopy], “Retroactive Interest and Fee Adjustments,” illustrates historical effective dates and linked differences; its reversal flow also changes original metadata. Neither determines this project's policy or overrides immutable records.

[iso]: https://www.iso20022.org/sites/default/files/documents/messages/mdr_part_2/ISO20022_MDRPart2_BankToCustomerCashManagement_2018_2019_v1_0.pdf#page=61
[canopy]: https://docs.canopyservicing.com/v2/docs/transaction-reversals
