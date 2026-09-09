# Daily closing with late arrivals

## Scenario

This fictional example applies the [approved booking cutoff](../deliverables/AMBIGUITIES.md#daily-calculation-timing). A new booking changes current funds without becoming eligible for every historical calculation.

## Inputs and assumptions

* Account DEMO-003 opens at AED 0.00, with no holds, fees or unpaid interest.
* Apply the exercise's 0.04% daily rate and AED's two decimal places.
* A arrives on Day 1. B arrives during the Day 2 calculation; C arrives after its result is recorded. Each credit is recorded on receipt. B and C have distinct supplied IDs despite matching amounts and dates.
* Assume complete inputs and a historical review of Day 1 on Day 3, processing B then C. This is an illustrative checkpoint, not an approved replay schedule or concurrency mechanism.
* No payment or other financial movement occurs. The example reports only Day 1 interest; Day 2's ordinary accrual is outside this calculation breakdown.

## Input events

All amounts are AED; process the events in this order.

| Event | Received | `booking_date` | `value_date` | Credit |
|---|---|---|---|---:|
| A | Day 1 | Day 1 | Day 1 | 100.00 |
| B | Day 2, during calculation | Day 2 | Day 1 | 50.00 |
| C | Day 2, after calculation | Day 2 | Day 1 | 50.00 |

C is delivered again with the same ID on Day 2. Skip it without inspecting its payload or repeating its financial effect.

## Calculation order

| Step | Eligible inputs and result |
|---|---|
| Day 2 calculation for Day 1 | The cutoff is booking Day 1. Only A qualifies: `100.00 * 0.0004 = 0.04`. Record original accrual I1 of 0.04. |
| B and C recorded on Day 2 | Current funds rise to 150.00, then 200.00. Their Day 2 bookings remain excluded from the Day 2 job, despite Day 1 value dates. Repeating that calculation still targets 0.04 and adds nothing. |
| Day 3 historical review, B | Booking cutoff Day 2 admits B. Revised target is `150.00 * 0.0004 = 0.06`. Append J-B, linked to B: `0.06 - 0.04 = 0.02`. |
| Same review, C | Target becomes `200.00 * 0.0004 = 0.08`. Append J-C, linked to C: `0.08 - (0.04 + 0.02) = 0.02`. |
| Repeat the completed review | `0.08 - (0.04 + 0.02 + 0.02) = 0.00`. Append nothing. |

## Records and dates

A, B and C are the only financial credits. I1 is an unpaid ordinary accrual for Day 1. J-B and J-C each have booking and value dates Day 3, their actual correction day, with a breakdown referencing Day 1. They remain pending until the next eligible monthly payment and never rewrite I1.

## Result and limits

Current ledger balance is `100.00 + 50.00 + 50.00 = 200.00`. Day 1 unpaid interest is 0.04 after the Day 2 calculation and `0.04 + 0.02 + 0.02 = 0.08` after the Day 3 review. Pending amounts have no ledger effect.

The [Authorization payment contract](../deliverables/AMBIGUITIES.md#yield-calculation-and-payment) still requires the current source account counter for a financial payment. An excluded booking can advance that counter and require recalculation with the same amount. Validation and atomic recording of calculation records remain a separate [open proposal](../deliverables/AMBIGUITIES.md#pending-calculation-decisions).

The account, amounts, arrival order and review checkpoint are scenario inputs. This example sets no clock time, time zone, general input-completeness policy or final replay total.
