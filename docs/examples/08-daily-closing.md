# Daily closing with late arrivals

**Earlier proposal:** This example uses known records without the subsequently approved booking cutoff. Its sequence and results await alignment with the [current decision](../deliverables/AMBIGUITIES.md#daily-calculation-timing); they are not current expected results.

## Scenario

This fictional example illustrates the [proposed closing process](../research/06-daily-closing-research.md). One transaction arrives while interest is being calculated; another arrives after the result is recorded.

## Inputs and assumptions

* Account: DEMO-003, denominated in AED, opening at AED 0.00. No holds, fees, or unpaid interest exist initially.
* Apply the exercise's daily interest rate of 0.04% to positive closing balances. Every interest amount below is already exact at AED's two decimal places.
* Day 1 ends at 00:00 on Day 2. Its interest calculation starts at 00:30. These times illustrate the proposed schedule in one business time zone; they do not select a production time zone.
* Each credit is recorded when it arrives. B and C are distinct legitimate transactions delivered later, even though their amounts and dates match.
* The example ends at 00:37 on Day 2, before Day 2 closes and before capitalization. The tables list every input event and generated record. No other transactions occur.

## Input events

All amounts are AED. Processing follows the listed order.

| Event | Received and recorded | `booking_date` | `value_date` | Credit |
|---|---|---|---|---:|
| A | Day 1, 14:00 | Day 1 | Day 1 | 100.00 |
| B | Day 2, 00:31 | Day 2 | Day 1 | 50.00 |
| C | Day 2, 00:34 | Day 2 | Day 1 | 50.00 |

At 00:36 on Day 2, C is delivered again with the same event identity. This is a repeated delivery, not another credit.

## Calculation order

All times below are on Day 2. Interest targets are calculated from the listed credits, not supplied as unexplained scenario inputs.

| Time | Action and known records | Result |
|---|---|---|
| 00:30 | Read A; Day 1 balance is 100.00. | Calculate `100.00 * 0.0004 = 0.04`; record nothing yet. |
| 00:31 | Record B while the calculation is running. | Ledger balance becomes 150.00. |
| 00:32 | Try to record the old result; detect the intervening change. Read A and B again. | Discard the unrecorded 0.04. Record I1: `150.00 * 0.0004 = 0.06` unpaid interest for Day 1. |
| 00:33 | Repeat the calculation with A, B, and I1 known. | Desired 0.06 minus recorded 0.06 equals zero. Append nothing. |
| 00:34 | Record C after I1. | Ledger balance becomes 200.00. |
| 00:35 | Recalculate Day 1 with A, B, C, and I1 known. | Desired interest is `200.00 * 0.0004 = 0.08`. Record J1: `0.08 - 0.06 = 0.02` additional unpaid interest. |
| 00:36 | Receive C again with the same identity. | Detect the duplicate. Append no credit or interest adjustment. |
| 00:37 | Query the result and repeat the calculation. | Report ledger balance 200.00 and unpaid Day 1 interest 0.08. The remaining difference is zero. Append nothing. |

The validity check and recording at 00:32 and 00:35 are indivisible operations under the proposal. No other change intervenes in those successful recording steps.

## Records and dates

A, B, and C are the only credits to the account. I1 records unpaid interest of 0.06 for Day 1 at 00:32 on Day 2. It is not an account credit.

J1 is a separate adjustment linked to C. Its `booking_date` and `value_date` are both Day 2 under the approved late transaction treatment. Its breakdown attributes the 0.02 increase to unpaid interest for Day 1. It changes the future interest payment, not the ledger balance.

The earlier 0.04 was never recorded, so no compensation for that amount is needed. I1 and J1 remain unchanged after subsequent queries and calculations.

## Result and limits

At 00:37 on Day 2, the ledger balance is `100.00 + 50.00 + 50.00 = 200.00`. Unpaid interest for Day 1 is `0.06 + 0.02 = 0.08`. Neither interest record adds money to the account before capitalization.

The [exercise](../exercise-inputs/exercise-statement.md) supplies the rate, precision, and deferred capitalization requirement. The [late transaction research](../research/02-booking-and-value-dates-research.md) supplies the adopted adjustment treatment. The account, amounts, times, and stopping point are scenario data. This example does not choose a reversal policy or implement a concurrency mechanism.
