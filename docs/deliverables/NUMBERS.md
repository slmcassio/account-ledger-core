# Numbers

Document every chosen constant, its value and purpose, its source or the rationale for choosing it, and why that value was chosen instead of half of it.

## Rounding

The exercise requires two decimal places for AED and three for BHD. The smallest stored units are AED 0.01 and BHD 0.001. Half of either unit cannot be stored at its required precision.

HALF_UP is the approved project mode. It favors recipients of positive interest at exact ties and accepts the resulting upward bias. The [rounding examples](../research/01-rounding-research.md#example) use exact decimal inputs around these boundaries; they do not set business constants.

## Daily Interest Calculation

The exercise supplies 0.04% per day, or `0.0004`. Halving it changes the required rate; no annual divisor applies. The zero boundary excludes nonpositive balances and remains zero when halved.

The [approved daily calculation](AMBIGUITIES.md#daily-interest-calculation) uses exact multiplication followed by one daily HALF_UP rounding, with no fractions carried between days. Currency precision plus the rate's four fractional places gives at most six fractional places for an AED product or seven for BHD. These are derived scale bounds, not limits on integer digits or total precision; halving them would not preserve every exact product. Corrections subtract rounded daily monetary amounts exactly, and payments sum eligible unpaid components.

The [study's independent bases](../research/09-daily-interest-research.md) are example inputs, not replay balances or new business constants. AED 12.49 and 12.50 and BHD 1.249 expose daily rounding boundaries. Halving them no longer probes those boundaries and can produce a base outside currency precision. Two days at AED 465.00 expose the difference between daily rounding and rounding after aggregation; using one day removes that comparison. Halving the base to 232.50 retains a discrepancy but reverses its direction. The correction example supplies daily amounts 1.00, +0.20 and 1.30 to distinguish the original, prior adjustment and revised target; halving an input changes the difference, not that method. These are illustrative monetary amounts, not new constants. The small calculations stay in the study; these examples determine no final payment.

## Values Used in the Late Transaction Example

These results retain example 04's earlier closing schedule. Its final payment includes Day 6 interest and awaits alignment with the [approved booking cutoff](AMBIGUITIES.md#daily-calculation-timing); it is not a current expected payment.

The [exercise](../exercise-inputs/exercise-statement.md) supplies the fee, rate, and precision below. The capitalization day is the endpoint of its six-day scenario. Halving a supplied value would change the rule or scenario it defines.

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

These calculations retain example 08's earlier proposal without the subsequently approved booking cutoff. They await alignment and are not current expected results.

The [fictional example](../examples/08-daily-closing.md) uses the exercise's 0.04% daily rate and AED's two decimal places. Its interest amounts are exact at that precision. The following amounts are scenario data; each comparison halves one input while keeping the others unchanged.

| Input | Purpose and reason for the value |
|---|---|
| AED 0.00 opening balance, holds, and unpaid interest | Makes the listed entries explain the entire state. Half remains zero. |
| A: AED 100.00 credit | Produces an initial interest target of 0.04. Halving A to 50.00 leaves a final balance of 150.00 and unpaid Day 1 interest of 0.06. |
| B: AED 50.00 credit | Changes the target while the first calculation is running. Halving B to 25.00 makes the first recorded interest 0.05 and the final unpaid interest 0.07. |
| C: AED 50.00 credit | Changes the target after interest has been recorded. Its equality to B shows why amounts and dates alone cannot identify a duplicate. Halving C to 25.00 makes the adjustment 0.01 and the final unpaid interest 0.07. |

The original 0.04 target is never recorded. I1 records 0.06; J1 adds 0.02, producing 0.08 in unpaid Day 1 interest. The final ledger balance is 200.00. These are derived results, not new constants. Redelivery of C preserves its identity and adds no second 50.00 credit.

Day 1 is the calculation period; B, C, and J1 have Day 2 booking dates. B and C retain Day 1 value dates, while J1 has Day 2 value date under the approved adjustment method. The example ends at 00:37 on Day 2, before that day's close and before capitalization. Its clock times separate reading, arrivals, recording, and repetition; they are not processing deadlines.

The proposed 00:00 boundary and earliest 00:30 job start reflect the user's scheduling proposal. The 30-minute interval is not a measured delivery limit or a guarantee of complete input. Halving it to 15 minutes would change the proposed start time but would not remove the need to handle late arrivals. Clock times, business time zone, and positions of closing calculations in the supplied replay remain unresolved; the previous-day reference and booking cutoff are approved.

## Values Used in the Pending Interest Illustration

The [decision example](AMBIGUITIES.md#interest-adjustments-wait-for-payment) assumes three daily results change from AED 1.50 to 2.00. These illustrate reconciliation, not a new rate or a calculated replay result. One original amount is already paid; two remain unpaid. Each difference is 0.50, totaling 1.50. The next payment includes 3.00 of unpaid originals plus that adjustment, totaling 4.50 for these components.

Halving the original 1.50 to 0.75 while retaining the target makes each difference 1.25; halving the target to 1.00 instead makes each difference -0.50. The arithmetic changes, not the policy. D5, D15, D20, D25, and D30 identify illustrative historical days, correction, and payment; they define no monthly calendar or system limit.

## Values Used in the Overdraft Fee Snapshots

[Study 07](../research/07-overdraft-fees-research.md#calculation-snapshots) uses the exercise's AED 25.00 fee, zero eligibility boundary, and two decimal places. Halving the fee or changing that boundary would change the supplied rule. The approved assessment base is a project interpretation, not another monetary constant.

Both hypothetical accounts open at 0.00 with no holds; half of zero remains zero. These inputs make the listed financial entries explain each balance. No capitalization is included, and pending interest has no ledger effect. Each comparison below halves one input while retaining the other scenario data.

* First snapshot, credit 100.00: provides the initial balance against the delayed debit. Halving it to 50.00 gives Day 1 and Day 2 bases of -60.00 and -40.00, requiring a 25.00 target for each period.
* First snapshot, credit 20.00: restores Day 2 to a positive base after that debit. Halving it to 10.00 leaves Day 1 at -10.00 and Day 2 at exactly zero, which still requires no Day 2 fee.
* First snapshot, delayed debit 110.00: exceeds the first credit but not both credits combined. Halving it to 55.00 leaves bases of 45.00 and 65.00, requiring no fee for either period.
* Second snapshot, debit 10.00: provides the original deficit. Halving it to 5.00 while retaining the supplied 25.00 fee and late credit gives Day 1 and Day 2 bases of 15.00 and -10.00.
* Second snapshot, late credit 20.00: removes the original deficit while leaving Day 2 negative after the earlier period's dated fee. Halving it to 10.00 gives Day 1 and Day 2 bases of zero and -25.00. Day 1's corrected target remains zero.

The first snapshot's original inputs give Day 1 and Day 2 bases of -10.00 and 10.00. With no previously recorded fees for those periods, their correction amounts are 25.00 and zero. The second gives bases of 10.00 and -15.00. Day 1's target changes from its recorded 25.00 to zero, requiring a 25.00 refund. These are derived historical calculations, not final account balances or a total assessment count.

The snapshots use a Day 6 job and Day 5 booking cutoff so every listed input is eligible. Credits or debits with Day 1 value date illustrate a historical effect. The first snapshot's second credit and the second snapshot's existing fee have Day 2 value dates. That fee's Day 2 assessment and booking are scenario data, not an approved ordinary schedule. Late transaction corrections in these snapshots assessed on Day 6 have both dates on Day 6; the [reversal refund exception](AMBIGUITIES.md#reversal-compensation) does not apply to them. These dates isolate cutoff and adjustment effects; they introduce no business deadline or replay checkpoint.

For the supplied E7 scenario, the Day 2 principal balance is `1,200.00 - 950.00 - 620.00 = -370.00`. Day 3 adds 400.00, giving 30.00; Day 4 subtracts E5's 185.00 and E6's 180.00, giving -335.00. These calculations include E7 and exclude E9, fees, and capitalization. They establish no final number of assessments. See the [open dependencies](AMBIGUITIES.md#overdraft-fee-assessment-base).

## Values Used in the Reversal Illustration

The [study 08 comparison](../research/08-reversals-research.md#one-fee-two-outcomes) and [complete calculation](../research/examples/08-reversal-15-day-simulation.md) use illustrative inputs, not the supplied E7/E9 replay. Method B is approved; method A illustrates the alternative not adopted.

* AED 2,500.00 opening balance gives daily interest of exactly 1.00 at the supplied 0.04% rate. Halving the opening balance alone gives 0.50 before the debit and a 1,750.00 deficit after it.
* The 3,000.00 debit creates a 500.00 deficit. Halving only the debit leaves 1,000.00 positive, so it would no longer illustrate overdraft fees. The reversal amount equals the debit; it is derived, not another constant.
* The exercise supplies the 25.00 fee, 0.04% daily rate and AED's two decimal places; HALF_UP is approved. Each 25.00 fee changes the unrounded daily interest by 0.01. Changing these inputs changes the exercise's rule.
* No initial holds or pending interest keeps the financial effects traceable. Zero remains zero when halved. Exact multiplication and one daily currency rounding follow the [approved daily interest rule](AMBIGUITIES.md#daily-interest-calculation). The other assumptions remain illustrative.

Day 1 is the opening, Day 5 the debit's booking/value date, Days 9 and 12 alternative reversal bookings, and Day 10 the only payment. Day 15 is consultation after the routine for reference Day 14. These dates compare correction before and after the payment, not a general calendar. The ordinary job precedes financial events, with reference and cumulative booking cutoff D-1. Its fee for H is assessed, booked and valued on H+1; an immediate corrective checkpoint after reversal is illustrative. Neither schedule settles study 06's pending checkpoints or ordinary assessment dates.

The four or seven fees and their 100.00 or 175.00 refunds follow from those inputs. Reversal refunds are booked on the actual correction day and valued on each original charge's date under B; interest corrections keep both dates on the correction day and remain pending. The linked calculation records the derived daily targets, actual payments and final balances, including Day 10's payment in later interest bases. These results establish no current replay total.

## E10 Installment Values

The [exercise](../exercise-inputs/exercise-statement.md#event-stream) supplies E10's BHD 10.000 credit to ACC-002, three installments and Day 5 booking and value dates. E10 remains after E9 in the supplied replay order. These are scenario inputs, not business constants. Halving the amount or changing the count or dates would change E10.

BHD's required three decimal places give a minimum stored unit of 0.001. Half of that unit is 0.0005, which cannot be stored at that precision. Thus `10,000 = 3 * 3,333 + 1` minimum units. The [approved allocation](AMBIGUITIES.md#e10-installment-allocation) assigns the remaining unit to installment 3, deriving BHD 3.333, 3.333 and 3.334, totaling 10.000. The position is a convention, not a monetary constant. HALF_UP does not determine it.

Criterion 7 gives `3 * 3.334 = 10.002`, an excess of BHD 0.002. Rounding each exact third independently with HALF_UP gives `3 * 3.333 = 9.999`, leaving BHD 0.001 unallocated. These are derived comparisons, not permitted changes to the original credit.

## Overdraft Fee Currency Values

The [approved configuration](AMBIGUITIES.md#overdraft-fee-currency) uses AED 25.00 for ACC-001's account type, retaining the exercise's amount, and BHD 0.000 for ACC-002's account type, an explicit project exception. Halving AED 25.00 changes the supplied amount; half of the chosen zero BHD fee remains zero. Neither value defines an exchange rate.

[Study 12](../research/12-fee-currency-research.md) uses ACC-002's supplied zero opening balance and E10 credit as scenario data, with principal separate from interest. Its hypothetical BHD -1.000 base illustrates that zero fees do not prevent negative balances; halving it to -0.500 preserves that conclusion. It is not a new account, movement or constant.

## Account Event Counter

The user selected **1** for each account's first transaction that creates a snapshot and an increment of **1** for each subsequent snapshot. These values give each snapshot the next whole-number position in that account's history. Half of either value would introduce fractional positions, which are not part of this counting scheme. A declined authorization records its decision and ID without a snapshot or counter increment; it does not consume a position.

The candidate counter is the calculation base's last recorded counter plus one, under the [snapshot decision](AMBIGUITIES.md#snapshot-recording-and-retries). The architecture's counters 10 and 11 illustrate a concurrency scenario; they are not configuration values.

TODO: Record additional constants and numerical decisions as their reviews are approved.
