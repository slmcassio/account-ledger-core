# Final settlement, partial settlement, and hold release

## Scope

This fictional example illustrates the alternatives in the [hold lifecycle research](../research/05-hold-lifecycle-research.md). It does not select finality for Auth-A or an expiration policy for the exercise.

## Scenario

* Account: DEMO-003, denominated in AED, opening at AED 0.00.
* No authorization records or active holds exist at the start.
* All events have Day 1 booking and value dates. Each path ends before daily closing, so no fee, interest, or capitalization entry occurs.
* C is a confirmed credit. A is an authorization request. Each alternative settlement reports a legitimate payment already settled externally and references A.
* The listed events are complete for each path. No other holds, releases, or expirations occur.
* Amounts use two decimal places. Account identifiers, amounts, dates, and the stopping point are scenario data.

## Common starting events

Process these two events in order. All amounts are AED, and balances are shown after each event.

| Event | Ledger change | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| C: settled credit of 50.00 | +50.00 | 50.00 | 0.00 | 50.00 |
| A: request to hold 20.00, approved | 0.00 | 50.00 | 20.00 | 30.00 |

A is approved because `50.00 - 0.00 - 20.00 = 30.00`, which is at or above zero. The hold leaves the ledger balance at 50.00. A ledger change of 0.00 means no debit or credit is created.

## Alternative settlements

Each row below is a separate continuation of C and A. Choose one row for a path; do not process both settlements together.

| Next event | Ledger change | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| F: final settlement of 15.00 | -15.00 | 35.00 | 0.00 | 35.00 |
| P: partial, non-final settlement of 15.00 | -15.00 | 35.00 | 5.00 | 30.00 |

Both paths record the same debit: `50.00 - 15.00 = 35.00`.

For F, the settled portion is 15.00 and the unused portion is `20.00 - 15.00 = 5.00`. Ending the full reservation leaves no active hold. Available balance becomes `35.00 - 0.00 = 35.00`.

For P, the remaining 5.00 stays reserved. Available balance remains `35.00 - 5.00 = 30.00`. The smaller settled amount alone does not select either path; finality is an explicit input to this comparison.

## Release after the non-final settlement

This continuation follows only C, A, and P. R explicitly releases the remaining reservation of 5.00 and references A. It reports no additional settled payment.

| Next event | Ledger change | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| R: release the remaining 5.00 | 0.00 | 35.00 | 0.00 | 35.00 |

The ledger balance stays `50.00 - 15.00 = 35.00`. Active holds fall from 5.00 to zero, so available balance rises by 5.00. No credit or second settlement debit is created.

## History and limits

Preserve C, A, and the settlement records. Append information that explains the reservation reduction and release. A remains evidence of the original approval even after its active reserved amount reaches zero. This specifies the required effects without selecting an API or physical record structure.

No expiration event occurs in these paths. They do not select an expiration deadline or establish that reservations never expire.

The [research](../research/05-hold-lifecycle-research.md#sources-and-limits) records the external precedents and their limits. These figures are not the exercise's account balances.
