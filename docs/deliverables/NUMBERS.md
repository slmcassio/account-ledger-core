# Numbers

Document every chosen constant, its value and purpose, its source or the rationale for choosing it, and why that value was chosen instead of half of it.

## Values Used in the Late Transaction Example

The [exercise](../exercise-statement.md) supplies the following values. Halving them would change its requirements.

| Value | Purpose |
|---|---|
| AED 25.00 | Fee for each negative closing day. |
| 0.04% = 0.0004 per day | Interest rate on positive closing balances. |
| Two decimal places for AED | Monetary storage and rounding precision. |
| End of Day 6 | Single capitalization of the daily interest amounts. |

The [fictional example](../examples/04-backdated-adjustment.md) uses these inputs. They are scenario data, not business constants. The comparisons below halve one amount while keeping the others unchanged.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance | Makes every balance traceable to the listed transactions. Half is still zero. |
| A: AED 1,000.00 credit | Establishes the positive balance before the delayed debit. With 500.00, Day 5 would also be negative. |
| B: AED 500.00 credit | Keeps Day 5 positive after the adjustment. With 250.00, the balance after the historical fee adjustment would be -25.00. |
| C: AED 1,200.00 debit | Makes the historical balance -200.00. A debit of 600.00 would leave it positive and produce no historical fee. |

A has Day 1 booking and value dates. B has both dates on Day 5. C is booked on Day 5 with Day 2 value date, so the example covers three already closed days, Days 2 through 4. These dates illustrate delayed delivery; they are not configurable numeric limits.

Derived results are three fees totaling 75.00, unpaid interest reduced by 1.20, a current balance of 225.00, interest capitalization of 0.58, and a final balance of 225.58. They follow from the example's inputs and adopted adjustment dates; they are not universal expected balances.

## Values Used in the Unmatched Settlement Example

The [fictional example](../examples/05-unmatched-settlement.md) uses the following scenario data. The amounts illustrate the decision; the decision does not depend on their size. Each comparison halves one amount while keeping the other inputs unchanged.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance | Makes the two listed events explain the entire balance. Half is still zero. |
| AED 0.00 active holds | Makes available balance equal ledger balance, isolating the missing authorization question. Half is still zero. |
| C: AED 500.00 credit | Provides a round starting amount above the settlement debit. Halving it to 250.00 would leave 70.00 after settlement. |
| S: AED 180.00 settlement | Makes the omitted debit visible while leaving a positive balance. Halving it to 90.00 would leave 410.00 after settlement. |

Both events have Day 1 booking and value dates. The example ends before daily closing so the comparison needs no fee or interest calculations. These dates define the example, not a new timing rule. AED uses the two decimal places required by the exercise.

The resulting ledger and available balances are both 320.00. Rejecting the settlement would leave both at 500.00, a difference of 180.00. These are derived results, not additional constants.

The report also discusses E6's AED 180.00 amount and Day 4 dates. Those values come from the exercise's event stream and remain scenario data.

## Values Used in the Authorization Example

The [fictional example](../examples/06-authorization-decisions.md) uses the following scenario inputs. Each comparison halves one amount while keeping the other request amounts unchanged and reevaluating the decisions.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance and active holds | Makes every balance and reservation traceable to the listed events. Half is still zero. |
| D: AED 10.00 settled debit | Shows that a financial movement changes the ledger independently of authorization. Halving it to 5.00 leaves 45.00 after C and 5.00 available after A. |
| C: AED 50.00 settled credit | Leaves 40.00 in the ledger, exactly enough for A. Halving it to 25.00 leaves 15.00, so A is declined. |
| A: AED 40.00 requested hold | Tests approval at exactly zero remaining availability. Halving it to 20.00 leaves enough for B to be approved as well. |
| B: AED 10.00 requested hold | Demonstrates a decline with no additional reservation. Halving it to 5.00 still produces a decline after A consumes all availability. |

AED's two decimal places and the zero minimum remaining availability come from the exercise. All events occur on Day 1 before closing; this scenario needs no fee or interest calculation. Its amounts and stopping point are not new business constants.

The conditional continuation reverses D with a 10.00 credit. This amount is derived from D, not chosen independently. It then submits a new request N for 10.00, consuming the 10.00 now available. Halving N to 5.00 would leave 5.00 available. These results illustrate the pending proposal for no automatic reevaluation; they do not approve that policy.

TODO: Record additional constants and numerical decisions as their reviews are approved.
