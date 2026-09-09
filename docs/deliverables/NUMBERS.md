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

Example 04 applies the [approved booking cutoff](AMBIGUITIES.md#daily-calculation-timing) with an illustrative Day 6 historical review. Its results describe that fixture, not the supplied replay.

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
| B: AED 500.00 credit | Keeps the balance positive after the Day 6 fee correction. With 250.00, the balance after the historical fee adjustment and before payment would be -25.00. |
| C: AED 1,200.00 debit | Makes the historical balance -200.00. A debit of 600.00 would leave it positive and produce no historical fee. |

A has Day 1 booking and value dates. B has both dates on Day 5. C is booked on Day 5 with Day 2 value date, so the example covers three already closed days, Days 2 through 4. These dates illustrate delayed delivery; they are not configurable numeric limits.

Derived results are three corrective fees totaling 75.00, a pending interest correction of -1.20 and a balance of 225.00 before payment. Both correction dates are Day 6. Day 5's base remains 300.00 and earns 0.12. Day 6 pays the original 1.60 plus 0.12, totaling 1.72, and excludes the correction booked that day. Closing balance is 226.72, with -1.20 pending. Day 7 calculates Day 6 interest: `226.72 * 0.0004 = 0.090688`, rounded to 0.09 for the next month's payment. These are fixture results, not final replay balances.

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

The [fictional example](../examples/08-daily-closing.md) uses the exercise's 0.04% rate and AED precision. Its inputs are scenario data, not business constants; each comparison below halves one input independently.

* Opening balance, holds and unpaid interest are 0.00 to isolate the listed events; half remains zero.
* A credits 100.00, producing Day 1 interest of 0.04. Halving A gives an original 0.02 and a revised 0.06 after both late credits become eligible.
* B and C each credit 50.00, with distinct IDs. Halving either gives a revised base of 175.00 and daily interest of 0.07. Equal amounts and dates do not make distinct IDs duplicates.

The Day 2 job uses booking cutoff Day 1 and records 0.04. B and C are booked Day 2 and valued Day 1: they raise current funds to 200.00 but are excluded from that job. At the illustrative Day 3 historical review, each adds 0.02 to the recorded Day 1 interest, totaling `0.04 + 0.02 + 0.02 = 0.08`. Both adjustments have booking and value dates Day 3. These dates expose the cutoff and correction treatment without setting a general checkpoint.

The proposed midnight boundary and 00:30 start remain unresolved alongside the time zone. The 30-minute gap is not a measured delivery limit or completeness guarantee; halving it would change only the proposed start time. The example selects no clock times.

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

The snapshots use a Day 6 job and Day 5 booking cutoff so every listed input is eligible. Credits or debits with Day 1 value date illustrate a historical effect. The first snapshot's second credit and the second snapshot's existing fee have Day 2 value dates. That fee's Day 2 assessment, booking and value dates follow the approved H+1 ordinary schedule for Day 1. Late transaction corrections in these snapshots assessed on Day 6 have both dates on Day 6; the [reversal refund exception](AMBIGUITIES.md#reversal-compensation) does not apply to them. These dates isolate cutoff and adjustment effects; they introduce no business deadline or replay checkpoint.

For the supplied E7 scenario, the Day 2 principal balance is `1,200.00 - 950.00 - 620.00 = -370.00`. Day 3 adds 400.00, giving 30.00; Day 4 subtracts E5's 185.00 and E6's 180.00, giving -335.00. These calculations include E7 and exclude E9, fees, and capitalization. They establish no final number of assessments. See the [open dependencies](AMBIGUITIES.md#overdraft-fee-assessment-base).

## Values Used in the Reversal Illustration

The [study 08 comparison](../research/08-reversals-research.md#one-fee-two-outcomes) and [complete calculation](../research/examples/08-reversal-15-day-simulation.md) use illustrative inputs, not the supplied E7/E9 replay. Method B is approved; method A illustrates the alternative not adopted.

* AED 2,500.00 opening balance gives daily interest of exactly 1.00 at the supplied 0.04% rate. Halving the opening balance alone gives 0.50 before the debit and a 1,750.00 deficit after it.
* The 3,000.00 debit creates a 500.00 deficit. Halving only the debit leaves 1,000.00 positive, so it would no longer illustrate overdraft fees. The reversal amount equals the debit; it is derived, not another constant.
* The exercise supplies the 25.00 fee, 0.04% daily rate and AED's two decimal places; HALF_UP is approved. Each 25.00 fee changes the unrounded daily interest by 0.01. Changing these inputs changes the exercise's rule.
* No initial holds or pending interest keeps the financial effects traceable. Zero remains zero when halved. Exact multiplication and one daily currency rounding follow the [approved daily interest rule](AMBIGUITIES.md#daily-interest-calculation). The other assumptions remain illustrative.

Day 1 is the opening, Day 5 the debit's booking/value date, Days 9 and 12 alternative reversal bookings, and Day 10 the only payment. Day 15 is consultation after the routine for reference Day 14. These dates compare correction before and after the payment, not a general calendar. The ordinary job precedes financial events, with reference and cumulative booking cutoff D-1. Its fee for H is assessed, booked and valued on H+1; an immediate corrective checkpoint after reversal is illustrative. H+1 follows the approved ordinary fee schedule; the corrective checkpoint remains illustrative and does not settle study 06's general checkpoint questions.

The four or seven fees and their 100.00 or 175.00 refunds follow from those inputs. Reversal refunds are booked on the actual correction day and valued on each original charge's date under B; interest corrections keep both dates on the correction day and remain pending. The linked calculation records the derived daily targets, actual payments and final balances, including Day 10's payment in later interest bases. These results establish no current replay total.

## Interest Settlement and Calendar Examples

The [approved payment decision](AMBIGUITIES.md#interest-payment-schedule-and-capitalization) maps Day 6 to the first business day of a new month and Day 5 to the preceding month end. Dates 26, 27, 28, 29, 30 and 01 illustrate a 30-day month; they are scenario labels, not business constants or an actual calendar. Halving them would not preserve this mapping. Day 6 interest is calculated on Day 7 and paid on the first business day of the following month.

[Study 10](../research/10-interest-capitalization-research.md) supplies independent eligible components: `0.10 + 0.20 - 0.02 = 0.28` credits; `0.10 - 0.30 = -0.20` debits and takes a zero balance to -0.20. A zero eligible total settles its components without moving funds. These examples illustrate the sign rule, not final payments. Halving an input changes the total; zero remains zero when halved.

Its capitalization example credits 0.01 to 12.49, giving 12.50. At 0.0004, Day 7 calculates `12.50 * 0.0004 = 0.005`, rounded to 0.01; without the credit, `12.49 * 0.0004 = 0.004996` rounds to 0.00. These inputs expose a rounding boundary; halving them no longer probes that boundary.

## E10 Installment Values

The [exercise](../exercise-inputs/exercise-statement.md#event-stream) supplies E10's BHD 10.000 credit to ACC-002, three installments and Day 5 booking and value dates. E10 remains after E9 in the supplied replay order. These are scenario inputs, not business constants. Halving the amount or changing the count or dates would change E10.

BHD's required three decimal places give a minimum stored unit of 0.001. Half of that unit is 0.0005, which cannot be stored at that precision. Thus `10,000 = 3 * 3,333 + 1` minimum units. The [approved allocation](AMBIGUITIES.md#e10-installment-allocation) assigns the remaining unit to installment 3, deriving BHD 3.333, 3.333 and 3.334, totaling 10.000. The position is a convention, not a monetary constant. HALF_UP does not determine it.

Under the [approved receipt scenario](AMBIGUITIES.md#e10-installment-allocation), E10 arrives on Day 6 after E9 and after Day 5 interest was recorded as 0.000. The revised target is `10.000 * 0.0004 = 0.004`, so append +0.004 with both dates Day 6. It is excluded from Day 6 payment and remains pending until the next eligible monthly payment. This derived correction does not establish final balances or installment event counts.

Criterion 7 gives `3 * 3.334 = 10.002`, an excess of BHD 0.002. Rounding each exact third independently with HALF_UP gives `3 * 3.333 = 9.999`, leaving BHD 0.001 unallocated. These are derived comparisons, not permitted changes to the original credit.

## Overdraft Fee Currency Values

The [approved configuration](AMBIGUITIES.md#overdraft-fee-currency) uses AED 25.00 for ACC-001's account type, retaining the exercise's amount, and BHD 0.000 for ACC-002's account type, an explicit project exception. Halving AED 25.00 changes the supplied amount; half of the chosen zero BHD fee remains zero. Neither value defines an exchange rate.

[Study 12](../research/12-fee-currency-research.md) uses ACC-002's supplied zero opening balance and E10 credit as scenario data, with principal separate from interest. Its hypothetical BHD -1.000 base illustrates that zero fees do not prevent negative balances; halving it to -0.500 preserves that conclusion. It is not a new account, movement or constant.

## Account Event Counter

The user selected **1** for each account's first transaction that creates a snapshot and an increment of **1** for each subsequent snapshot. These values give each snapshot the next whole-number position in that account's history. Half of either value would introduce fractional positions, which are not part of this counting scheme. A declined authorization records its decision and ID without a snapshot or counter increment; it does not consume a position.

The candidate counter is the calculation base's last recorded counter plus one, under the [snapshot decision](AMBIGUITIES.md#snapshot-recording-and-retries). The architecture's counters 10 and 11 illustrate a concurrency scenario; they are not configuration values.

TODO: Record additional constants and numerical decisions as their reviews are approved.
