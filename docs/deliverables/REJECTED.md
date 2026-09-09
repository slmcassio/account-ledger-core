# Rejected Criteria and Approaches

Each refusal answers four questions: **What was rejected? Why? What did we adopt? What supports it?** Rejections below are already recorded project decisions; a research alternative alone is not a new rejection.

## Acceptance Criteria Coverage

| Criterion | Status | Reason or boundary |
|---|---|---|
| 1 | Correct at its stated boundary | Before E9 and excluding fees, Day 2 principal is `1,200 - 950 - 620 = AED -370.00`. |
| 2 | Unresolved for the current replay | The final E7 fee count depends on open review checkpoints. No approved total establishes exactly one fee. |
| 3 | Supported under the settlement assumption | E5 reports a confirmed external payment, so record its debit. |
| 4 | Rejected under the settlement assumption | Missing local authorization does not erase a confirmed debit. |
| 5 | Correct as a condition | An approved hold reduces availability, not the ledger. This does not establish Auth-B's approval. |
| 6 | Blanket claim unsupported | Principal, fees, balances and pending interest are distinct; records and payments remain. No final replay result establishes full restoration. |
| 7 | Rejected | Three BHD 3.334 installments exceed BHD 10.000. |
| 8 | Rejected | Discarding a difference violates exact daily-interest reconciliation. |

**Source:** [study 13, criterion analysis](../research/13-acceptance-criteria-research.md#analysis). Its historical three-fee counterfactual rejects E6 and uses dates incompatible with current policy; it is not an approved replay result. An assessed balance day and a fee's actual charge day are different under the H+1 schedule.

## Historical Value Dates for Late Adjustments

1. **Rejected:** Giving legitimate late transaction adjustments the historical days' value dates.
2. **Reason:** That would change historical fee balances instead of applying the correction to current funds, as chosen for this policy.
3. **Adopted behavior:** Use the actual correction day for both adjustment dates. Preserve the original transaction's supplied dates and keep interest differences pending.
4. **Evidence:** In [example 04](../examples/04-backdated-adjustment.md), adjustment J uses Day 6 dates; Days 2 through 4 identify calculation periods only.

**Limit:** [Reversal fee refunds](AMBIGUITIES.md#reversal-compensation) retain their separately approved original charge value dates. This refusal does not apply to that exception.

## Immediate Balance Corrections for Paid Interest

1. **Rejected:** Immediately changing the account balance to correct interest that was already paid.
2. **Reason:** The approved simplification uses one pending treatment, regardless of whether the original interest was paid.
3. **Adopted behavior:** Keep every interest difference pending until an eligible regular payment. Preserve earlier payments and settle each unpaid component once.
4. **Evidence:** The [pending interest decision](AMBIGUITIES.md#interest-adjustments-wait-for-payment) explicitly replaces the earlier immediate correction approach.

**Limit:** Fee adjustments affect funds when recorded; reversal fee refunds also change reconstructed historical balances.

## Acceptance Criterion 4

1. **Rejected:** Refusing a settlement solely because its authorization ID is missing and omitting the debit.
2. **Reason:** Under the approved assumption, SETTLEMENT reports a legitimate payment already settled externally. Omitting it overstates funds.
3. **Adopted behavior:** Record the debit with its supplied dates and reference. Report the missing authorization; create no substitute authorization or hold.
4. **Evidence:** [Study 03](../research/03-unmatched-settlements-research.md#example) shows `AED 500.00 - 180.00 = 320.00`. Omitting the debit overstates funds by 180.00.

**Limit:** The mandatory rules alone do not resolve criterion 4. This refusal depends on the approved meaning of SETTLEMENT.

## Inferring Finality from Missing Later Settlements

1. **Rejected:** Claiming Auth-A's settlement is final because no later settlement appears.
2. **Reason:** Missing later events do not establish whether unused funds should remain reserved.
3. **Adopted behavior:** Treat Auth-A as final through an explicit scenario assumption. Release its unused AED 15.00 without a ledger credit.
4. **Evidence:** [Study 05](../research/05-hold-lifecycle-research.md#approved-policy-settlement-and-release) distinguishes final settlement from a non-final settlement that keeps the remainder held.

**Limit:** Partial, non-final settlement remains valid behavior. Only the unsupported justification was rejected.

## Waiting for the Complete Input Before Calculating

1. **Rejected:** Waiting for the entire event stream before every calculation.
2. **Reason:** The chosen active system keeps receiving transactions and calculates during processing.
3. **Adopted behavior:** Run daily calculations with the previous-day booking cutoff and append corrections when later eligible inputs change recorded results.
4. **Evidence:** [Study 06](../research/06-daily-closing-research.md#receipt-and-missing-inputs) records E10 arriving after the Day 5 calculation and creating a pending BHD +0.004 adjustment.

**Limit:** A final report after the finite replay remains possible. E10's position is approved; other replay checkpoints, clock times and general input completeness remain open.

## Principal Only and Current Dates for Reversal Fee Refunds

1. **Rejected:** Returning only principal, or using the correction day as the value date of reversal fee refunds (method A).
2. **Reason:** Reversal can change fees and interest. Current value dates would leave the refunded fees in earlier balances.
3. **Adopted behavior:** Recalculate affected components and append differences. Under method B, book fee refunds now and value them at the original charge's value date.
4. **Evidence:** [Study 08](../research/08-reversals-research.md#one-fee-two-outcomes) gives a corrected Day 6 balance of AED 2,500.00 under B, versus 2,475.00 under A.

**Limit:** Respect the calculation boundary; do not automatically refund every fee. Interest corrections retain current dates and wait for eligible payment. This does not establish criterion 6's blanket restoration or final replay totals.

## Acceptance Criterion 7

1. **Rejected:** Requiring all three E10 installments to be BHD 3.334.
2. **Reason:** Their sum is `3 * 3.334 = 10.002`, creating BHD 0.002 beyond the supplied credit.
3. **Adopted behavior:** Allocate BHD 3.333, 3.333 and 3.334, placing the remaining minimum unit in installment 3.
4. **Evidence:** [Study 11](../research/11-installments-research.md#small-example) shows `3.333 + 3.333 + 3.334 = 10.000`.

**Limit:** The contradiction is independent of remainder position. Positions 1 or 2 would also conserve the credit; HALF_UP does not select the position.

## Acceptance Criterion 8

1. **Rejected:** Discarding a difference between rounded daily accruals and the capitalized total.
2. **Reason:** The exercise requires exact equality between those amounts.
3. **Adopted behavior:** Sum eligible unpaid daily monetary amounts and adjustments exactly; settle each once.
4. **Evidence:** [Study 09](../research/09-daily-interest-research.md#why-the-stages-matter) gives two daily amounts of AED 0.19. Paying 0.37 instead of `0.19 + 0.19 = 0.38` loses 0.01.

## Extra Daily Interest Rounding and Fraction Carry

1. **Rejected:** Rounding intermediate daily interest, carrying fractions between days, or replacing daily rounding with rounding the raw total.
2. **Reason:** These methods can change a day's amount or break its reconciliation with payment.
3. **Adopted behavior:** Multiply exactly and round each day once. Calculate corrections from rounded targets, not rounded raw differences.
4. **Evidence:** [Study 09](../research/09-daily-interest-research.md#corrections-and-payment) changes an AED base from 12.49 to 12.50: daily interest rises from 0.00 to 0.01, although the raw difference rounds to zero.

**Limit:** This decision concerns daily interest. It selects no precision policy for other calculations or treatment of negative payment totals.

## Snapshots for Declined Authorizations

1. **Rejected:** Creating a snapshot and advancing the account counter for a declined authorization.
2. **Reason:** A decline changes neither funds nor holds. Advancing the counter would unnecessarily invalidate Yield's source version.
3. **Adopted behavior:** Record the decline and its supplied ID without a hold, financial posting, new snapshot or counter increment.
4. **Evidence:** The [revised snapshot decision](AMBIGUITIES.md#snapshot-recording-and-retries) replaces the earlier behavior. A recorded decline remains unchanged on redelivery.

## Preventing Negative Balances Instead of Configuring the Fee

1. **Rejected:** Making the BHD account incapable of becoming negative as the answer to the fee-currency question.
2. **Reason:** That would change account behavior. The approved simplification configures the fee while preserving confirmed debits and the new-hold approval rule.
3. **Adopted behavior:** Use BHD 0.000 for ACC-002's account type. It is an explicit exception to the exercise's literal AED fee rule.
4. **Evidence:** [Study 12](../research/12-fee-currency-research.md#negative-example-and-boundaries) records this alternative as considered and not selected: `BHD -1.000 - 0.000 = -1.000`.

**Limit:** A zero fee does not permit an unfunded hold. It introduces no conversion or separate AED obligation.

Add further refusals only when their review and rationale are recorded. Keep unresolved criteria visible rather than inventing a result.
