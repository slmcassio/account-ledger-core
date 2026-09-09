# Backdated transaction and adjustment

This fictional scenario applies the [approved booking cutoff](../deliverables/AMBIGUITIES.md#daily-calculation-timing). Its explicit review checkpoint illustrates the policy and does not set checkpoints or final balances for the supplied replay.

## Scenario

This fictional example illustrates the [approved adjustment method](../research/02-booking-and-value-dates-research.md). One account opens at AED 0.00. The late debit is a legitimate transaction delivered after it occurred. System error corrections are outside this example's scope.

## Rules and assumptions

Apply the [exercise's rules](../exercise-inputs/exercise-statement.md): AED 25.00 per negative closing day, daily interest of 0.04% on positive closing balances, and one interest credit at the end of Day 6. Round each daily accrual to two decimal places with the approved HALF_UP mode.

The adjustment method and interpretation of fee value dates are adopted project choices. The closing schedule below is specific to this example.

Days 1 through 4 have recorded ordinary accruals before the late debit arrives on Day 5. Assume complete inputs and a historical review on Day 6 with booking cutoff Day 5, followed by Day 5 ordinary calculation and the monthly payment. Day 5 ends the month; Day 6 starts the new month. No other transactions occur. This review position is example data, not a general scheduling decision.

## Example

All amounts are AED. This table lists every entry that affects the account balance, in processing order. It excludes unpaid interest accruals and their correction, which are shown below.

| Record | `booking_date` | `value_date` | Account entry | Current ledger balance after entry |
|---|---|---|---:|---:|
| A: deposit | Day 1 | Day 1 | +1,000.00 | 1,000.00 |
| B: deposit | Day 5 | Day 5 | +500.00 | 1,500.00 |
| C: late debit | Day 5 | Day 2 | -1,200.00 | 300.00 |
| J: adjustment linked to C | Day 6 | Day 6 | -75.00 | 225.00 |
| Interest payment | Day 6 | Day 6 | +1.72 | 226.72 |

Before C, each of Days 1 through 4 had a closing balance of 1,000.00, no fee, and `1,000 * 0.0004 = 0.40` interest. C changes the balance from Day 2 onward. B only affects Day 5 onward.

| Day | Balance before C | Balance after C and J | Fee included in J | Interest before C | Corrected interest |
|---|---:|---:|---:|---:|---:|
| 1 | 1,000.00 | 1,000.00 | 0.00 | 0.40 | 0.40 |
| 2 | 1,000.00 | -200.00 | 25.00 | 0.40 | 0.00 |
| 3 | 1,000.00 | -200.00 | 25.00 | 0.40 | 0.00 |
| 4 | 1,000.00 | -200.00 | 25.00 | 0.40 | 0.00 |

Each of Days 2 through 4 becomes `1,000 - 1,200 = -200`. J's 75.00 debit applies on Day 6, so it does not reduce these historical balances. Its breakdown attributes 25.00 to each negative day.

J contains two components for each of Days 2, 3, and 4:

* Charge 25.00 in fees per day: `3 * 25.00 = 75.00`, debited from the account.
* Reduce unpaid interest by 0.40 per day: `3 * -0.40 = -1.20`. Interest already calculated falls from 1.60 to 0.40. This correction reduces the future eligible interest payment. All interest differences remain pending, including corrections for previously paid periods.

C already debited 1,200.00. J has both dates on Day 6 and reduces the current balance from 300.00 to 225.00. A query limited to records before C still reproduces the earlier balances.

Day 5's base is `1,000.00 + 500.00 - 1,200.00 = 300.00`. J has value Day 6, so it does not reduce this base. Day 5 earns `300.00 * 0.0004 = 0.12`, with no fee, calculated on Day 6.

The Day 6 payment excludes the -1.20 interest correction booked that day. It settles the four original accruals plus the new Day 5 accrual: `1.60 + 0.12 = 1.72`. The closing ledger balance is **AED 226.72**; -1.20 remains pending for the next eligible monthly settlement, where the eligible total's sign determines credit, debit or no movement.

Day 6 interest is calculated on Day 7: `226.72 * 0.0004 = 0.090688`, rounded once to 0.09. It belongs to the new month and is not part of the Day 6 payment. Repeating the historical review produces `0.00 - (0.40 - 0.40) = 0.00` for each corrected day, even after the original accruals were paid.
