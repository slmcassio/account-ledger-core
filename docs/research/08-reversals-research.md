# Principal reversals and financial corrections

**Source:** Research from `main` at `ec4e20cc9eb9f788e5195be574b384ec97108f32`, reconciled with the local architectural decisions.

**Integration status:** [Method B](../deliverables/AMBIGUITIES.md#reversal-compensation), [ordinary fee dates](../deliverables/AMBIGUITIES.md#overdraft-fee-assessment-base), and [monthly mapping and signed settlement](../deliverables/AMBIGUITIES.md#interest-payment-schedule-and-capitalization) are incorporated into the deliverables. Illustrative checkpoints establish no final replay totals.

<a id="what-a-and-b-do"></a>

## Correcting fees and interest after a reversal

Reversing a debit may also require correcting the fees and interest it affected.

**Approved rule:** Append the principal reversal once with the supplied dates, then recalculate affected fees and interest from the affected value day onward. Record only differences without rewriting earlier records.

The comparison below considers two fee refund policies. Both book refunds on the correction day; only their value dates differ:

* **A, not adopted:** use the correction day, leaving earlier fees in past balances.
* **B, approved:** use the original fee's value date, offsetting those fees in reconstructed past balances.

Returning only principal was not selected.

## One fee, two outcomes

Open with AED 2,500.00. Debit 3,000.00 with booking/value Day 5. Reverse it with booking Day 9 and value Day 5.

Focus on the 25.00 fee for Day 5, charged with booking/value Day 6. Both methods refund it on Day 9. With principal cancelled, Day 6 has 2,500.00 before this fee:

| Question | A: current date | B: original fee date |
|---|---|---|
| Refund recorded on | Day 9 | Day 9 |
| Refund effective from | Day 9 | Day 6 |
| Corrected Day 6 balance | 2,500 - 25 = 2,475.00 | 2,500 - 25 + 25 = 2,500.00 |
| Day 6 interest at 0.04% | 0.99 | 1.00 |

Both make the refund available on Day 9. B's Day 6 value date reconstructs past balances without delivering funds in the past or rewriting earlier credits.

The [complete calculation](examples/08-reversal-15-day-simulation.md) includes three more fees with value dates after Day 6, giving a 100.00 refund on Day 9.

## Before or after payment

The full example compares reversal on Day 9 or Day 12, payment on Day 10 and consultation on Day 15, with a 25.00 daily fee, 0.04% daily interest and HALF_UP rounding. Its [schedule](examples/08-reversal-15-day-simulation.md#inputs-and-assumed-schedule) is illustrative.

| Reversal / method | Paid Day 10 | Balance Day 10 | Balance Day 15 | Pending Day 15 |
|---|---|---|---|---|
| Day 9 / A | 8.94 | 2,508.94 | 2,508.94 | 5.00 |
| Day 9 / B | 9.00 | 2,509.00 | 2,509.00 | 5.00 |
| Day 12 / A | 4.00 | -621.00 | 2,504.00 | 9.79 |
| Day 12 / B | 4.00 | -621.00 | 2,504.00 | 10.00 |

With reversal on Day 9, eligible corrections join ordinary interest on Day 10: A pays `5.00 + 3.94`; B pays `5.00 + 4.00`. With reversal on Day 12, the 4.00 already paid stays unchanged; corrections remain pending. Day 15 pays nothing.

## Fixed rules

Every correction concerns a legitimate transaction; error correction is excluded to simplify the exercise.

The [supplied E9](../exercise-inputs/exercise-statement.md) credits AED 620.00 to reverse E7, with booking Day 6 and value Day 2. Preserve all records and their order, including E10 after E9 and E6's confirmed AED 180.00 debit.

Under the [daily booking cutoff](06-daily-closing-research.md#agreed-operation), E9 and adjustments booked Day 6 cannot change the payment referencing Day 5, even if processed first.

Use the approved [fee base](07-overdraft-fees-research.md#rule-and-approved-base) and [incremental comparison](02-booking-and-value-dates-research.md#approved-adjustment-method). Preserve HALF_UP and all prior adjustments, including paid ones. [Study 09](09-daily-interest-research.md#rule-and-approved-calculation) records the [adopted daily calculation](../deliverables/AMBIGUITIES.md#daily-interest-calculation): exact multiplication followed by one HALF_UP currency rounding, with no fractions carried between days. Fee differences affect funds; interest follows the [payment rules](10-interest-capitalization-research.md#approved-payment-and-components).

## Approved decisions and remaining limits

* **Scope:** respect the applicable calculation boundary; reversal does not automatically refund every fee. The [accepted decision](../deliverables/AMBIGUITIES.md#reversal-compensation) records the complete rule and rationale.
* **Dates:** [the approved fee refund dating](#correcting-fees-and-interest-after-a-reversal) is a limited exception to the [late transaction policy](02-booking-and-value-dates-research.md#approved-adjustment-method). Interest corrections retain both dates on the actual correction day and remain pending, including corrections of paid periods.

Restoring principal, historical balances, net fees, interest and authorizations are different outcomes. Criterion 6's blanket restoration claim is unsupported. [Authorizations](04-authorization-decisions-research.md#approved-policy-later-balance-corrections) are not automatically reevaluated.

Final replay bases and totals depend on [the open calculation decisions](06-daily-closing-research.md#decisions-still-open). Apply the approved [calendar](10-interest-capitalization-research.md#approved-monthly-payment) and [signed settlement](10-interest-capitalization-research.md#approved-signed-settlement).

## Sources and limits

[ISO 20022][iso], camt.053.001.08 §3.4.2.15.4, p. 61, defines opposite reversal directions, not compensation entitlement. [Canopy][canopy], “Retroactive Interest and Fee Adjustments,” illustrates historical effective dates and linked differences; its reversal flow also changes original metadata. Neither determines this project's policy or overrides immutable records.

[iso]: https://www.iso20022.org/sites/default/files/documents/messages/mdr_part_2/ISO20022_MDRPart2_BankToCustomerCashManagement_2018_2019_v1_0.pdf#page=61
[canopy]: https://docs.canopyservicing.com/v2/docs/transaction-reversals
