# Numbers

Document every chosen constant, its value and purpose, its source or the rationale for choosing it, and why that value was chosen instead of half of it.

## Rounding

The exercise requires two decimal places for AED and three for BHD. The smallest stored units are AED 0.01 and BHD 0.001. Half of either unit cannot be stored at its required precision.

HALF_UP is the approved project mode. It favors recipients of positive interest at exact ties and accepts the resulting upward bias. The [rounding examples](../research/01-rounding-research.md#example) use exact decimal inputs around these boundaries; they do not set business constants.

## Values Used in the Late Transaction Example

The [exercise](../exercise-statement.md) supplies the fee, rate, and precision below. The capitalization day is the endpoint of its six-day scenario. Halving a supplied value would change the rule or scenario it defines.

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

The [fictional example](../examples/06-authorization-decisions.md) uses the following scenario inputs. Each comparison starts a fresh scenario with one amount halved and the other request amounts unchanged.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance and active holds | Makes every balance and reservation traceable to the listed events. Half is still zero. |
| D: AED 10.00 settled debit | Shows that a financial movement changes the ledger independently of authorization. Halving it to 5.00 leaves 45.00 after C and 5.00 available after A. |
| C: AED 50.00 settled credit | Leaves 40.00 in the ledger, exactly enough for A. Halving it to 25.00 leaves 15.00, so A is declined. |
| A: AED 40.00 requested hold | Tests approval at exactly zero remaining availability. Halving it to 20.00 leaves enough for B to be approved as well. |
| B: AED 10.00 requested hold | Demonstrates a decline with no additional reservation. Halving it to 5.00 still produces a decline after A consumes all availability. |

AED's two decimal places and the zero minimum remaining availability come from the exercise. All events occur on Day 1 before closing; this scenario needs no fee or interest calculation. Its amounts and stopping point are not new business constants.

The continuation reverses D with a 10.00 credit. This amount is derived from D, not chosen independently. It then submits a new request N for 10.00, consuming the 10.00 now available. Halving N to 5.00 would leave 5.00 available. These results apply the approved policy of no automatic reevaluation.

## Values Used in the Hold Lifecycle Example

The [fictional example](../examples/07-hold-lifecycle.md) uses the following scenario data. These amounts illustrate reservation effects; they are not new business constants. Each comparison halves one input while keeping the others unchanged.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance and initial active holds | Makes all balances traceable to the listed events. Half remains zero. |
| C: AED 50.00 credit | Covers the requested reservation and leaves a visible available balance. Halving it to 25.00 would still permit the 20.00 hold, leaving 5.00 available before settlement. |
| A: AED 20.00 hold | Exceeds the settlement, making an unused portion visible. Halving it to 10.00 would put the 15.00 settlement above the held amount and would no longer illustrate an unused remainder. |
| F or P: AED 15.00 settlement | Leaves a small remainder from the 20.00 hold. Halving it to 7.50 would leave a ledger balance of 42.50 and, on the non-final path, a remaining hold of 12.50. |

All example events have Day 1 booking and value dates and occur before daily closing. These dates and the stopping point isolate the hold effects from fees and interest; they do not define a processing schedule. AED uses the two decimal places required by the exercise.

The initial available balance is 30.00. Both settlements leave a ledger balance of 35.00. Final settlement leaves no active hold and an available balance of 35.00; non-final settlement leaves 5.00 held and 30.00 available. R releases that entire remaining 5.00 without changing the ledger balance. The 5.00 is derived from `20.00 - 15.00`, not an independent constant. Releasing only 2.50 would be a different, partial release and leave 2.50 reserved.

The exercise's Auth-A hold of 200.00, settlement of 185.00, and Day 4 settlement dates are supplied scenario data. Treating that settlement as final is the approved project assumption. The released 15.00 is the derived unused portion, not an additional credit.

Days 1 through 6 are the supplied replay window. The decision to generate no automatic expiration within that window introduces no expiration duration. Halving the window would change the scenario. Mambu's documented default of seven days is an external product setting, not an adopted project constant. See the [decision and limits](AMBIGUITIES.md#hold-expiration-during-the-replay).

## Values Used in the Daily Closing Example

The [fictional example](../examples/08-daily-closing.md) uses the exercise's 0.04% daily rate and AED's two decimal places. Its interest amounts are exact at that precision. The following amounts are scenario data; each comparison halves one input while keeping the others unchanged.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance, holds, and unpaid interest | Makes the listed entries explain the entire state. Half remains zero. |
| A: AED 100.00 credit | Produces an initial interest target of 0.04. Halving A to 50.00 leaves a final balance of 150.00 and unpaid Day 1 interest of 0.06. |
| B: AED 50.00 credit | Changes the target while the first calculation is running. Halving B to 25.00 makes the first recorded interest 0.05 and the final unpaid interest 0.07. |
| C: AED 50.00 credit | Changes the target after interest has been recorded. Its equality to B shows why amounts and dates alone cannot identify a duplicate. Halving C to 25.00 makes the adjustment 0.01 and the final unpaid interest 0.07. |

The original 0.04 target is never recorded. I1 records 0.06; J1 adds 0.02, producing 0.08 in unpaid Day 1 interest. The final ledger balance is 200.00. These are derived results, not new constants. Redelivery of C preserves its identity and adds no second 50.00 credit.

Day 1 is the calculation period; B, C, and J1 have Day 2 booking dates. B and C retain Day 1 value dates, while J1 has Day 2 value date under the approved adjustment method. The example ends at 00:37 on Day 2, before that day's close and before capitalization. Its clock times separate reading, arrivals, recording, and repetition; they are not processing deadlines.

The proposed 00:00 boundary and earliest 00:30 job start reflect the user's scheduling proposal. The 30-minute interval is not a measured delivery limit or a guarantee of complete input. Halving it to 15 minutes would change the proposed start time but would not remove the need to handle late arrivals. The schedule, business time zone, and positions of closing calculations in the supplied replay remain unresolved.

TODO: Record additional constants and numerical decisions as their reviews are approved.
