# Daily closing and repeatable recalculation

## Scope

This study uses the [exercise](../exercise-statement.md) to discuss daily closing in an active system, represented by an in-memory replay.

## Closing rule and approved approach

The closing balance includes the opening balance and financial entries with `value_date <= D`. The exercise requires daily results and immutable records, but supplies no closing times or arrival timestamps.

**Approved approach:** Calculate during event processing. Financial entries update the running balance without closing the day or establishing its daily overdraft fee.

## Proposed daily schedule

Close Day D at 00:00 on Day D+1 and start its interest job from 00:30. The business time zone remains undecided. Neither time guarantees complete input.

## Proposed handling of late arrivals

1. Read a consistent view of the account's entries and recorded calculation results. Identify the target day and included records.
2. Include recorded transactions affecting that day, even if received after midnight.
3. If the account state changes during calculation, read again and recalculate. Check and record in one indivisible operation, preventing concurrent duplicate adjustments.
4. If a relevant transaction is recorded afterward, recalculate and append any necessary difference under the applicable policy.

For legitimate late transactions, both adjustment dates are the correction day; historical days are calculation references. Execution time alone does not determine entry dates. Reversal compensation remains unresolved.

## Repeated calculations and events

Queries append nothing. Recalculations append only a nonzero `corrected amount - net amount already recorded`, per account, day, and component. Preserve original records and link adjustments to their cause.

Detect duplicate input events separately to avoid repeating financial or hold effects. Daily interest stays outside the ledger balance until the single capitalization at the end of Day 6.

Under the approved authorization policy, balance corrections do not automatically reevaluate earlier decisions. New requests use the updated balance and active holds.

## Proposed replay checkpoints

Preserve the supplied order: E10 is booked on Day 5 but follows E9, booked on Day 6. The schedule alone does not determine these proposed positions:

* Close Days 1 through 4 after E2, E3, E4, and E6, respectively.
* After E7, recalculate Days 2 through 4 before E8; close Day 5 after E8.
* After E9, recalculate affected results for Days 2 through 5.
* Process E10 next and revise ACC-002's Day 5 results.
* In this finite replay, close Day 6 after E10 and capitalize once per account.

## Example

The [fictional example](../examples/08-daily-closing.md) follows arrivals during and after calculation. Unpaid interest reaches AED 0.08. Repeated calculation, duplicate delivery, and queries add no duplicate effects.

## Sources and limits

[Microsoft's Event Sourcing pattern][events], “Pattern advantages” and “Problems and considerations,” supports version checks, immutable history, compensation, and idempotency. It does not determine this exercise's schedule or financial policies, or require distributed infrastructure.

[events]: https://learn.microsoft.com/en-us/azure/architecture/patterns/event-sourcing
