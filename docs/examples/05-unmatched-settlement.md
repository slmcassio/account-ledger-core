# Settlement without a local authorization

## Scenario

This fictional example illustrates the [approved settlement interpretation](../research/03-unmatched-settlements-research.md). SETTLEMENT reports a legitimate, externally confirmed settlement. Its missing local authorization is reported without preventing the debit.

The example ends after the settlement on Day 1, before daily closing. No fees or interest have been recorded.

## Inputs

* Account: DEMO-001, denominated in AED, opening at AED 0.00.
* No authorization records or active holds exist at the start.
* Process the two events below in the listed order. They are all the events in this scenario.
* S is a confirmed settlement referring to Auth-Missing. No authorization with that ID exists locally.
* AED amounts use the two decimal places required by the exercise. The account, amounts, and dates are scenario data.

## Events and results

All amounts are AED.

| Record | `booking_date` | `value_date` | Account entry | Ledger balance after | Active holds after | Available balance after |
|---|---|---|---:|---:|---:|---:|
| C: CREDIT | Day 1 | Day 1 | +500.00 | 500.00 | 0.00 | 500.00 |
| S: SETTLEMENT, reference Auth-Missing | Day 1 | Day 1 | -180.00 | 320.00 | 0.00 | 320.00 |

Append S's debit, retain Auth-Missing as its reference, and report the missing authorization. Create no authorization or hold.

The ledger balance is `0.00 + 500.00 - 180.00 = 320.00`. Available balance is `320.00 - 0.00 = 320.00`.

## Comparison with rejection

For this confirmed settlement, compare the approved behavior with criterion 4:

| Behavior | Settlement debit recorded | Missing authorization reported | Ledger balance | Active holds | Available balance |
|---|---:|---|---:|---:|---:|
| Reject because the reference is unknown | 0.00 | Yes | 500.00 | 0.00 | 500.00 |
| Record the confirmed settlement, as approved | 180.00 | Yes | 320.00 | 0.00 | 320.00 |

Under the approved assumption, rejection omits a confirmed debit and overstates the local balance by `500.00 - 320.00 = 180.00`. The diagnostic identifies missing information; it does not reverse the financial movement.

## Sources and limits

The [exercise](../exercise-inputs/exercise-statement.md) supplies AED precision and the available balance formula. External confirmation is a project assumption. A missing local authorization does not prove that a payment was confirmed.
