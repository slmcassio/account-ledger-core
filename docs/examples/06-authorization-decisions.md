# Ledger balance, holds, and authorization decisions

## Reference and scope

This fictional example illustrates the [authorization research](../research/04-authorization-decisions-research.md). It separates the ledger's financial entries from authorization holds and decisions.

The first four events illustrate the agreed distinction. A separately marked continuation illustrates the proposal for later balance corrections, which remains under review.

## Inputs

* Account: DEMO-002, denominated in AED, opening at AED 0.00.
* No authorization records or active holds exist at the start.
* Process the events in the listed order. All have Day 1 booking and value dates. The example ends before Day 1 closes, so no fee, interest, or capitalization entry occurs.
* D and C are confirmed financial movements. A and B are authorization requests, not settled payments.
* The tables contain every event in the scenario. No hold is settled, released, or expired during the example.
* Amounts use two decimal places. The account, amounts, dates, and stopping point are scenario data, not additional business rules.

## Events and results

All amounts are AED. Each balance is the state after the event.

| Event | Financial entry | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| D: settled debit | -10.00 | -10.00 | 0.00 | -10.00 |
| C: settled credit | +50.00 | 40.00 | 0.00 | 40.00 |
| A: request to hold 40.00, approved | 0.00 | 40.00 | 40.00 | 0.00 |
| B: request to hold 10.00, declined | 0.00 | 40.00 | 40.00 | 0.00 |

The ledger records `0.00 - 10.00 + 50.00 = 40.00`. A and B add no financial entry; 0.00 in that column means no ledger balance change.

| Request | Ledger balance before | Active holds before | Requested hold | Availability if approved | Decision |
|---|---:|---:|---:|---:|---|
| A | 40.00 | 0.00 | 40.00 | 0.00 | Approve |
| B | 40.00 | 40.00 | 10.00 | -10.00 | Decline |

A reaches exactly zero, which the exercise permits: `40.00 - 0.00 - 40.00 = 0.00`.

B would leave `40.00 - 40.00 - 10.00 = -10.00`, so it is declined. That negative result is the outcome of a check, not a posted debit or an active hold. Actual available balance remains 0.00.

The resulting state has three distinct parts: ledger balance 40.00, A's active hold of 40.00, and a decision history containing A's approval and B's decline.

## Continuation under the pending proposal

The following two events occur next, still on Day 1 before closing. For this example, R reverses D by appending a credit of 10.00 linked to D. D remains recorded. No daily charges or interest have been recorded, so this example needs no rule for correcting them.

Under the proposed policy, the balance change does not automatically reconsider B. N is a new explicit authorization request for 10.00.

| Event | Financial entry | Ledger balance | Active holds | Available balance |
|---|---:|---:|---:|---:|
| R: reversal of D | +10.00 | 50.00 | 40.00 | 10.00 |
| N: new request to hold 10.00, approved | 0.00 | 50.00 | 50.00 | 0.00 |

After R, the ledger balance is `40.00 + 10.00 = 50.00` and available balance is `50.00 - 40.00 = 10.00`. The authorization system receives the updated balance. Under the proposal, B's decline remains recorded and B creates no hold.

N then passes the check: `50.00 - 40.00 - 10.00 = 0.00`. Active holds are now A's 40.00 and N's 10.00. B still has no hold. R was not known when A or B was processed; it is known when N is processed.

An alternative policy could append a corrective decision for B, but it would need its own rules. The credit R and such a decision would still be separate actions.

## Sources and limits

The [exercise](../exercise-statement.md) supplies monetary precision, the authorization condition, and the distinction between available and ledger balances. The [research](../research/04-authorization-decisions-research.md#sources-and-limits) explains the Stripe example and its limits.

This scenario does not calculate the exercise's account balances. It supplies an immediate reversal as an example input and does not establish a general reversal or historical adjustment policy. The continuation is conditional on the authorization proposal being adopted.
