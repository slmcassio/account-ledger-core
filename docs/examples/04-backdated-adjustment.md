# Backdated transaction and adjustment

## Reference and scope

This fictional example illustrates the [adopted adjustment method](../research/02-booking-and-value-dates-research.md). It uses one account, opening at AED 0.00, and includes every transaction in the scenario. The amounts are invented; none of the exercise's events are used.

**Scope:** The late debit represents a legitimate transaction delivered after it occurred, for example an official transaction received from Mastercard at a later date. It does not result from a system error. Corrections related to system errors are out of scope.

## Rule and open question

Keep the [exercise's rules](../exercise-statement.md): AED 25.00 per negative closing day, daily interest of 0.04% on positive closing balances, and one interest credit at the end of Day 6. Existing records remain unchanged.

The adjustment method and interpretation of fee value dates are adopted project choices. The closing schedule below is specific to this example.

## Analysis

J is recorded on Day 5 with `booking_date = Day 5` and `value_date = Day 5`. Days 2, 3, and 4 identify the periods being corrected in its calculation breakdown. Its debit affects the account balance from Day 5 onward.

Days 1 through 4 have already closed when the late debit arrives. Day 5 closes after its adjustment. No other transactions occur through Day 6. Interest uses balances before the final interest credit.

## Example

All amounts are AED. Read these records from top to bottom:

| Record | `booking_date` | `value_date` | Account entry | Balance after |
|---|---|---|---:|---:|
| A: deposit | Day 1 | Day 1 | +1,000.00 | 1,000.00 |
| B: deposit | Day 5 | Day 5 | +500.00 | 1,500.00 |
| C: late debit | Day 5 | Day 2 | -1,200.00 | 300.00 |
| J: adjustment linked to C | Day 5 | Day 5 | -75.00 | 225.00 |
| Interest payment | Day 6 | Day 6 | +0.58 | 225.58 |

**What changed in the past?** Before C, each of Days 1 through 4 had a closing balance of 1,000.00, no fee, and `1,000 * 0.0004 = 0.40` interest. C changes the balance from Day 2 onward. B only affects Day 5 onward.

| Day | Balance before C | Balance after C and J | Fee included in J | Interest before C | Corrected interest |
|---|---:|---:|---:|---:|---:|
| 1 | 1,000.00 | 1,000.00 | 0.00 | 0.40 | 0.40 |
| 2 | 1,000.00 | -200.00 | 25.00 | 0.40 | 0.00 |
| 3 | 1,000.00 | -200.00 | 25.00 | 0.40 | 0.00 |
| 4 | 1,000.00 | -200.00 | 25.00 | 0.40 | 0.00 |

Each of Days 2 through 4 becomes `1,000 - 1,200 = -200`. J's 75.00 debit applies on Day 5, so it does not reduce these historical balances. Its breakdown attributes 25.00 to each negative day.

**What does J contain?** One adjustment, separate from C, with two components for each of Days 2, 3, and 4:

* Charge 25.00 in fees per day: `3 * 25.00 = 75.00`, debited from the account.
* Reduce unpaid interest by 0.40 per day: `3 * -0.40 = -1.20`. Interest already calculated falls from 1.60 to 0.40. It had not entered the account balance yet, so this correction reduces the future interest payment.

C has already debited 1,200.00. J only adds the corrections: today's balance goes from 300.00 to 225.00. A query limited to records before C still reproduces the earlier balances.

**How does the scenario end?** Days 5 and 6 each close at 225.00 before interest payment, earning `225 * 0.0004 = 0.09` per day. Neither incurs a fee. At the end of Day 6, credit `0.40 + 0.09 + 0.09 = 0.58`. The final account balance is **AED 225.58**.

## Sources and limits

The exercise supplies the rates and capitalization day. The transactions and closing schedule are invented. Posting the adjustment with a Day 5 value date illustrates the adopted interpretation of "day assessed" as the correction day.
