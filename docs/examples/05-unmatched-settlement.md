# Settlement without a local authorization

## Reference and scope

This fictional example illustrates the [approved settlement interpretation](../research/03-unmatched-settlements-research.md). SETTLEMENT reports a legitimate, externally confirmed settlement. Its missing local authorization is reported without preventing the debit.

The example ends immediately after the settlement on Day 1, before daily closing. It does not calculate fees or interest and does not select a closing or correction policy.

## Inputs

* Account: DEMO-001, denominated in AED, opening at AED 0.00.
* No authorization records or active holds exist at the start.
* Process the two events below in the listed order. They are all the events in this scenario; no fees, interest entries, or other records have been processed.
* S is a confirmed settlement referring to Auth-Missing. No authorization with that ID exists locally. No authorization or hold is created to match it.
* Amounts have two decimal places. The account, amounts, and dates are scenario data, not additional business rules.

## Events and results

All amounts are AED. Both events retain their supplied dates.

| Record | `booking_date` | `value_date` | Account entry | Ledger balance after | Active holds after | Available balance after |
|---|---|---|---:|---:|---:|---:|
| C: CREDIT | Day 1 | Day 1 | +500.00 | 500.00 | 0.00 | 500.00 |
| S: SETTLEMENT, reference Auth-Missing | Day 1 | Day 1 | -180.00 | 320.00 | 0.00 | 320.00 |

Append S's debit and report that Auth-Missing has no matching authorization in the local ledger. Preserve the reference on S. C remains unchanged, and no authorization or hold is invented.

The ledger balance is `0.00 + 500.00 - 180.00 = 320.00`. Available balance is `320.00 - 0.00 = 320.00`.

## Comparison with rejection

The same confirmed settlement produces different local results under the two interpretations:

| Interpretation | Settlement debit recorded | Missing authorization reported | Ledger balance | Active holds | Available balance |
|---|---:|---|---:|---:|---:|
| Reject because the reference is unknown | 0.00 | Yes | 500.00 | 0.00 | 500.00 |
| Record the confirmed settlement, as approved | 180.00 | Yes | 320.00 | 0.00 | 320.00 |

Under the approved assumption, rejection omits a confirmed debit and overstates the local balance by `500.00 - 320.00 = 180.00`. The diagnostic identifies missing information; it does not reverse the financial movement.

## Sources and limits

The [exercise](../exercise-statement.md) supplies AED precision and the available balance formula. The [research](../research/03-unmatched-settlements-research.md#sources-and-limits) explains the Stripe precedent and its limits. External confirmation is an explicit project assumption, not something proved by the absence of a local authorization.
