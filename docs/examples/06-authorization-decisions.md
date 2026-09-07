# Ledger balance, holds, and authorization decisions

## Scenario

This fictional example illustrates the [authorization research](../research/04-authorization-decisions-research.md). It separates the ledger's financial entries from authorization holds and decisions.

The first four events illustrate the approved responsibilities. The continuation applies the approved policy of no automatic reevaluation after balance corrections.

## Inputs

* Account: DEMO-002, denominated in AED, opening at AED 0.00.
* No authorization records or active holds exist at the start.
* Process the events in the listed order. All have Day 1 booking and value dates. The example ends before Day 1 closes, so no fee, interest, or capitalization entry occurs.
* D and C are confirmed financial movements. A and B are authorization requests, not settled payments.
* The tables contain every event in the scenario. No hold is settled, released, or expired during the example.
* AED amounts use the two decimal places required by the exercise. The account, amounts, dates, and stopping point are scenario data.

## Events and results

All amounts are AED. Each balance is the state after the event.

| Event | Ledger change | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| D: settled debit | -10.00 | -10.00 | 0.00 | -10.00 |
| C: settled credit | +50.00 | 40.00 | 0.00 | 40.00 |
| A: request to hold 40.00, approved | 0.00 | 40.00 | 40.00 | 0.00 |
| B: request to hold 10.00, declined | 0.00 | 40.00 | 40.00 | 0.00 |

The ledger balance is `0.00 - 10.00 + 50.00 = 40.00`. A and B add no financial entry.

| Request | Ledger balance before | Active holds before | Requested hold | Availability if approved | Decision |
|---|---:|---:|---:|---:|---|
| A | 40.00 | 0.00 | 40.00 | 0.00 | Approve |
| B | 40.00 | 40.00 | 10.00 | -10.00 | Decline |

A reaches exactly zero, which the exercise permits: `40.00 - 0.00 - 40.00 = 0.00`.

B would leave `40.00 - 40.00 - 10.00 = -10.00`, so it is declined. That negative result is the outcome of a check, not a posted debit or an active hold. Actual available balance remains 0.00.

The resulting state has three distinct parts: ledger balance 40.00, A's active hold of 40.00, and a decision history containing A's approval and B's decline.

## Continuation after a balance correction

The next two events also occur on Day 1 before closing. R reverses D with an appended credit of 10.00 linked to D. No daily charges or interest need correction in this scenario.

Authorization does not automatically reconsider B after the balance change. N is a new explicit authorization request for 10.00.

| Event | Ledger change | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| R: reversal of D | +10.00 | 50.00 | 40.00 | 10.00 |
| N: new request to hold 10.00, approved | 0.00 | 50.00 | 50.00 | 0.00 |

After R, the ledger balance is `40.00 + 10.00 = 50.00` and available balance is `50.00 - 40.00 = 10.00`. Authorization uses this balance for new requests. B creates no hold.

N then passes the check: `50.00 - 40.00 - 10.00 = 0.00`. Active holds are now A's 40.00 and N's 10.00. B still has no hold. R was not known when A or B was processed; it is known when N is processed.

## Sources and limits

The [exercise](../exercise-statement.md) supplies monetary precision and the authorization condition. The immediate reversal is scenario data, not a general policy for reversals or historical adjustments.
