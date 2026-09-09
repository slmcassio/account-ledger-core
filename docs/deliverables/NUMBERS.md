# Numbers

**BANK-SPEC implementation status:** Research-stage statements about unresolved replay results below are retained from remote main. The [implemented numerical outcomes](#bank-spec-replay-outcomes) at the end of this document establish the completed scenario and its temporal boundaries. No financial outcome changed during this rebase.

Each row answers four questions: **What value? Where did it come from? What is it for and why? Why not half?**

**Rule** means an exercise requirement. **Choice** means an approved project decision. **Scenario** and **Example** identify supplied and illustrative inputs. **Derived** values follow from those inputs. **Proposal** means not adopted.

Halving comparisons change one input at a time and keep the others fixed. Example results are not final replay totals. Sources below contain the complete calculations.

## Rounding

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| AED: 2 decimal places; BHD: 3 | Rule | Store and round each amount at its currency's required precision. | Fewer places would lose required monetary units. |
| AED 0.01; BHD 0.001 | Derived | Smallest stored units at those precisions. | 0.005 and 0.0005 cannot be stored at the required precision. |
| HALF_UP | Choice | Exact ties go away from zero, favoring positive interest recipients at ties. We accept that upward bias. | Not a numerical amount; halving does not apply. |

The mode does not imply a customer benefit for fees or negative adjustments. The study's small tie examples are exact inputs, not business constants. Currency alone establishes neither jurisdiction nor contract.

**Source:** [study 01, analysis and examples](../research/01-rounding-research.md#analysis). Its contractual precedents do not mandate this mode for these accounts.

## Daily Interest Calculation

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| 0.04% per day = `0.0004` | Rule | Apply the supplied daily rate to positive closing balances. No annual divisor applies. | 0.02% changes the required rate. |
| Zero interest for a base at or below zero | Rule | Exclude nonpositive balances. | Half of zero is still zero. |
| One rounding per account and day | Choice | Preserve the exact product, then round once; carry no fractions between days. | A rounding stage cannot be halved. Extra stages can change the result. |
| At most 6 fractional places for AED products; 7 for BHD | Derived | Currency scale plus the rate's 4 places preserves exact products. These are not total precision limits. | Fewer places may lose a significant fractional digit. |
| AED 12.49 and 12.50; BHD 1.249 | Example | Expose daily rounding boundaries and the risk of intermediate rounding. | Halving no longer tests those boundaries and may exceed currency precision. |
| Two days at AED 465.00 | Example | Compare daily rounding with rounding the raw sum. | One day removes the comparison. A base of 232.50 keeps a discrepancy but reverses its direction. |
| AED 1.00 original, +0.20 prior adjustment, 1.30 target | Example | Show why the next adjustment is only `1.30 - (1.00 + 0.20) = 0.10`. | Halving an input changes the difference, not the method. |

Two daily amounts of 0.19 pay 0.38; rounding their raw sum gives 0.37. Corrections subtract the original accrual and all prior adjustments, including paid ones, from the revised rounded daily target. Payments sum only eligible unpaid components exactly.

**Source:** [study 09, calculation and examples](../research/09-daily-interest-research.md#rule-and-approved-calculation). Precision and rounding stages for other calculations remain open.

## Values Used in the Late Transaction Example

All monetary values below are AED. The example uses the required 25.00 fee, daily rate and currency precision. Its Day 6 review is illustrative.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| 0.00 opening balance | Example | Make the listed movements explain the full balance. | Half remains zero. |
| A: 1,000.00 credit | Example | Establish the positive balance before the delayed debit. | 500.00 would also make Day 5 negative. |
| B: 500.00 credit | Example | Keep funds positive after the corrective fees. | 250.00 leaves -25.00 after those fees and before payment. |
| C: 1,200.00 debit | Example | Produce a historical deficit of 200.00. | 600.00 leaves a positive balance and no historical fee. |
| A: both dates Day 1; B: both Day 5; C: booking Day 5, value Day 2 | Example | Make the late debit affect Days 2 through 4. | These are scenario dates, not numeric limits. Changing them changes the affected days. |
| End of Day 6 payment | Scenario, reused in example | Apply the supplied capitalization endpoint and approved cutoff. | Day 3 changes that endpoint. |

**Derived results:** corrective fees `3 * 25.00 = 75.00`; pending interest correction -1.20; funds before payment 225.00. Both correction dates are Day 6. Day 5's base of 300.00 earns 0.12. Day 6 pays `1.60 + 0.12 = 1.72`, excluding that day's correction, and closes at 226.72. Day 7 calculates `226.72 * 0.0004 = 0.090688`, rounded to 0.09 for the next month's payment.

**Source:** [example 04](../examples/04-backdated-adjustment.md) and [study 02, example limits](../research/02-booking-and-value-dates-research.md#example).

## Values Used in the Unmatched Settlement Example

All monetary values below are AED. Both events use Day 1 for booking and value dates; the example stops before daily calculations.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| 0.00 opening balance and active holds | Example | Isolate the debit; ledger and available balances agree. | Half remains zero. |
| C: 500.00 credit | Example | Cover the settlement with a simple starting amount. | 250.00 leaves 70.00 after settlement. |
| S: 180.00 settlement | Example | Make the missing debit visible without a deficit. | 90.00 leaves 410.00. |
| E6: 180.00; both dates Day 4 | Scenario | Identify the exercise's unmatched settlement. | Changing these inputs changes E6. |

**Derived result:** `500.00 - 180.00 = 320.00`. Omitting the debit overstates both balances by 180.00. The confirmed-payment policy does not depend on these illustrative amounts.

**Source:** [example 05](../examples/05-unmatched-settlement.md) and [study 03, approved decision](../research/03-unmatched-settlements-research.md#the-approved-decision).

## Values Used in the Authorization Example

All monetary values below are AED. The events occur on Day 1 before daily calculations. The zero approval boundary is an exercise rule.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| 0.00 opening balance and holds | Example | Make every change traceable to a listed event. | Half remains zero. |
| D: 10.00 settled debit | Example | Show a financial movement independent of a hold decision. | 5.00 leaves 45.00 after C and 5.00 available after A. |
| C: 50.00 settled credit | Example | Leave exactly enough for A. | 25.00 leaves only 15.00, so A is declined. |
| A: 40.00 requested hold | Example | Test approval at exactly zero remaining availability. | 20.00 also leaves enough to approve B. |
| B: 10.00 requested hold | Example | Show a decline after A uses all availability. | 5.00 is still declined. |
| N: 10.00 new request after reversal | Example | Use the funds restored by reversing D. | 5.00 leaves 5.00 available. |

**Derived result:** C and D leave 40.00 in the ledger; A reserves 40.00. Reversing D restores 10.00 without automatically activating the declined B. The reversal amount comes from D, not a new constant.

**Source:** [example 06](../examples/06-authorization-decisions.md) and [study 04, later corrections](../research/04-authorization-decisions-research.md#approved-policy-later-balance-corrections).

## Values Used in the Hold Lifecycle Example

All monetary values below are AED. Example events use Day 1 dates and stop before daily calculations to isolate reservation effects.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| 0.00 opening balance and holds | Example | Explain balances entirely through listed events. | Half remains zero. |
| C: 50.00 credit | Example | Cover the hold and leave funds available. | 25.00 still covers it, leaving 5.00 available. |
| A: 20.00 hold | Example | Reserve more than the settlement. | 10.00 is below the settlement and leaves no unused remainder. |
| F or P: 15.00 settlement | Example | Leave an unused reservation of 5.00. | 7.50 leaves 42.50 in the ledger and 12.50 held on the non-final path. |
| R: 5.00 release | Derived | Release the entire `20.00 - 15.00` remainder. | 2.50 would be a partial release, leaving 2.50 held. |
| Auth-A: 200.00 hold; 185.00 settlement on Day 4 | Scenario | Apply the approved final-settlement assumption to E5. | Changing these inputs changes the supplied event. |
| Days 1 through 6 | Scenario | Bound the decision to generate no automatic expiration. | Three days would shorten the supplied replay. |
| Seven-day default in Mambu | External example | Show that a product may define expiry; it is not adopted here. | No project duration exists to halve. |

**Derived result:** either settlement leaves 35.00 in the ledger. Final settlement leaves no hold and 35.00 available; non-final settlement leaves 5.00 held and 30.00 available. Auth-A's unused 15.00 is released without a credit. Six replay days do not define a hold lifetime.

**Source:** [example 07](../examples/07-hold-lifecycle.md) and [study 05, policies and source limits](../research/05-hold-lifecycle-research.md#approved-policy-settlement-and-release).

## Values Used in the Daily Closing Example

All monetary values below are AED. Equal amounts with distinct IDs are separate credits.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| 0.00 opening balance, holds and unpaid interest | Example | Isolate the supplied credits. | Half remains zero. |
| A: 100.00 credit | Example | Produce original daily interest of 0.04. | 50.00 gives 0.02 originally and 0.06 after both late credits. |
| B and C: 50.00 each | Example | Show two separate corrections to the same historical day. | Halving either gives a final base of 175.00 and daily interest of 0.07. |
| Day 2 job; Day 1 cutoff; Day 3 historical review | Example | Separate initial calculation from later eligible corrections. | Changing these dates changes eligibility. They set no general checkpoint. |
| Midnight boundary; 00:30 start; 30-minute gap | Proposal | Record an unapproved schedule discussed in study 06. | A 15-minute gap only changes the proposed start; neither guarantees complete input. |

B and C are booked Day 2 and valued Day 1. They raise current funds to 200.00 but are excluded from the Day 2 job. At the illustrative Day 3 review, two corrections produce `0.04 + 0.02 + 0.02 = 0.08`; both adjustments have Day 3 dates. Time zone and general clock schedule remain open.

**Source:** [example 08](../examples/08-daily-closing.md) and [study 06, open decisions](../research/06-daily-closing-research.md#decisions-still-open).

## Values Used in the Pending Interest Illustration

These are illustrative daily results, not replay calculations or a new rate.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| AED 1.50 original daily amount | Example | Compare recorded accrual with a revised target. | 0.75 makes each difference 1.25. |
| AED 2.00 revised daily target | Example | Produce a visible positive correction. | 1.00 makes each difference -0.50. |
| Three days: one paid, two unpaid | Example | Show that paid originals are excluded from payment but included in reconciliation. | A different count changes component coverage and totals. |
| D5, D20 and D25 accruals; D15 earlier payment; D30 correction | Example | Distinguish historical periods, payment and correction. | These are illustrative dates, not a monthly calendar. |

**Derived results:** `3 * (2.00 - 1.50) = 1.50` pending correction on D30. The next eligible payment includes `2 * 1.50 + 1.50 = 4.50`. Repeating after payment gives `2.00 - (1.50 + 0.50) = 0.00` per day. Paid adjustments still count in that comparison.

**Source:** [pending interest decision](AMBIGUITIES.md#interest-adjustments-wait-for-payment) and [study 10, payment components](../research/10-interest-capitalization-research.md#approved-payment-and-components).

## Values Used in the Overdraft Fee Snapshots

Both illustrative accounts open at AED 0.00 with no holds or capitalization. The required fee is AED 25.00 and the eligibility boundary is zero. Halving the fee changes the rule; half of zero remains zero.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| First snapshot: 100.00 credit | Example | Establish funds before the delayed debit. | 50.00 gives Day 1/2 bases of -60.00 and -40.00. |
| First snapshot: 20.00 credit | Example | Restore Day 2 to a positive base. | 10.00 leaves Day 2 at zero, still without a fee. |
| First snapshot: 110.00 debit | Example | Exceed the first credit but not both credits together. | 55.00 leaves bases of 45.00 and 65.00, both without fees. |
| Second snapshot: 10.00 debit | Example | Create the original deficit. | 5.00 leaves revised bases of 15.00 and -10.00, retaining the original fee. |
| Second snapshot: 20.00 late credit | Example | Remove the first deficit while retaining the earlier period's fee in Day 2. | 10.00 leaves bases of zero and -25.00; Day 1 still needs no fee. |
| Day 6 job; Day 5 cutoff | Example | Admit all listed bookings. | Different dates change eligibility; they set no replay checkpoint. |
| Ordinary fee for H: assessment, booking and value on H+1 | Choice | Follow the approved daily job and charge on its actual assessment day. | Half a day is not the chosen daily schedule. |

**Derived results:** the first snapshot has bases -10.00 and 10.00, giving targets 25.00 and zero. The second has bases 10.00 and -15.00, so Day 1's recorded 25.00 needs a refund. Late corrections assessed on Day 6 have both dates Day 6. Only reversal fee refunds use original charge value dates.

For E7, before E9 and excluding fees and capitalization: Day 2 principal is `1,200.00 - 950.00 - 620.00 = -370.00`; Day 3 adds 400.00 to give 30.00; Day 4 subtracts 185.00 and 180.00 to give -335.00. These are not final balances or a fee count.

**Source:** [study 07, calculation snapshots](../research/07-overdraft-fees-research.md#calculation-snapshots) and [the supplied event stream](../exercise-inputs/exercise-statement.md#event-stream).

## Values Used in the Reversal Illustration

The simulation compares method A, not adopted, with approved method B. Its dates and results are illustrative, not the E7/E9 replay.

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| AED 2,500.00 opening balance | Example | Earn exactly 1.00 daily before the debit. | 1,250.00 earns 0.50 and gives a 1,750.00 deficit after the debit. |
| AED 3,000.00 debit | Example | Create a 500.00 deficit. | 1,500.00 leaves 1,000.00 positive and no overdraft. |
| AED 3,000.00 reversal | Derived | Reverse the original principal once. | Half would reverse only part of it. |
| Zero initial holds and pending interest | Example | Isolate the financial movements. | Half remains zero. |
| Day 1 opening; Day 5 debit; Day 9 or 12 reversal; Day 10 payment; Day 15 consultation | Example | Compare correction before and after payment. | Changing the dates changes the comparison, not a project calendar. |

The ordinary job runs before financial events, using reference D-1; ordinary fees for H have both dates H+1. The immediate corrective review after reversal is illustrative. Day 15 consultation follows the job for Day 14 and pays nothing.

**Derived results:** four or seven fees give `4 * 25.00 = 100.00` or `7 * 25.00 = 175.00` refunds. At the required rate, each 25.00 fee changes unrounded daily interest by 0.01. Method B refunds retain each original charge's value date; interest corrections remain pending with current correction dates. The simulation preserves actual payments and includes them in later bases.

**Source:** [study 08, comparison](../research/08-reversals-research.md#one-fee-two-outcomes) and [complete simulation](../research/examples/08-reversal-15-day-simulation.md).

## Interest Settlement and Calendar Examples

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| Day 6 first business day; Day 5 previous month end | Choice | Map the supplied payment day to the approved monthly schedule. | Changing the dates breaks that mapping. |
| Dates 26, 27, 28, 29, 30, 01 in a 30-day month | Example | Illustrate the Day 1 through Day 6 mapping. | Date labels are not configurable amounts. No actual calendar is selected. |
| AED 0.10, 0.20 and -0.02 | Example | Show an eligible positive total: `0.28` credits. | Halving one component changes the total. |
| AED 0.10 and -0.30 | Example | Show a negative total: `-0.20` debits, even from zero funds. | Halving a component changes the debit. |
| Zero eligible total | Choice | Settle components once without moving funds. | Half remains zero; the recording protocol is still open. |
| AED 12.49 balance and 0.01 payment | Example | Reach 12.50 and cross a daily rounding boundary. | Halving no longer probes that boundary. |

Day 7 calculates Day 6 interest: `12.50 * 0.0004 = 0.005`, rounded to 0.01. Without the payment, `12.49 * 0.0004 = 0.004996` rounds to 0.00. This cannot change the earlier payment. Day 6 ordinary interest belongs to the new month and waits for the following month's first business day.

**Source:** [study 10, settlement, dates and calendar](../research/10-interest-capitalization-research.md#approved-signed-settlement).

## E10 Installment Values

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| BHD 10.000; three installments; both dates Day 5 | Scenario | Preserve E10's supplied credit and dates for ACC-002. | Changing these inputs changes E10. |
| BHD 0.001 minimum unit | Derived | Allocate whole stored units at BHD precision. | 0.0005 cannot be stored at three decimal places. |
| Remainder in installment 3 | Choice | Let the final installment complete the total reproducibly. | It is a position, not an amount. Positions 1 or 2 could also conserve the total. |
| Receipt on Day 6, after E9 and the Day 5 calculation | Choice | Apply the approved late-arrival scenario without changing E10's dates. | Changing the position changes the recorded calculation history. |

**Derived allocation:** `10,000 = 3 * 3,333 + 1` minimum units, giving `3.333 + 3.333 + 3.334 = 10.000`. HALF_UP does not choose the remainder position. Three 3.334 installments create 0.002 extra; three 3.333 installments leave 0.001 unallocated.

**Derived correction:** Day 5 interest changes from 0.000 to `10.000 * 0.0004 = 0.004`. Append +0.004 with both dates Day 6; it is excluded from Day 6 payment and remains pending. Final balances and installment event counts remain open.

**Source:** [study 11, allocation](../research/11-installments-research.md#approved-remainder-position) and [study 06, E10 receipt](../research/06-daily-closing-research.md#receipt-and-missing-inputs).

## Overdraft Fee Currency Values

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| AED 25.00 for ACC-001's account type | Rule, retained in configuration | Keep the exercise's fee amount in the account's currency. | 12.50 changes the required amount. |
| BHD 0.000 for ACC-002's account type | Choice and explicit exception | Simplify the exercise because conversion requirements are missing. | Half remains zero. The zero fee departs from the literal mandatory rule. |
| BHD 0.000 opening balance; E10 credit of 10.000 | Scenario | Explain ACC-002's principal, excluding fees and interest. | Zero remains zero; 5.000 changes the supplied credit. |
| BHD -1.000 assessment base | Example | Show that a zero fee does not prevent negative balances. | -0.500 remains negative after the zero fee. |

Neither fee is an exchange rate. Once E10 is recorded, principal is zero for Days 1 through 4 and 10.000 for Days 5 and 6; these are not final balances with interest. No foreign exchange, separate AED obligation or additional account is adopted.

**Source:** [study 12, approved exception and boundaries](../research/12-fee-currency-research.md#rule-and-approved-exception).

## Account Event Counter

| Value | Origin | Use and reason | Why not half? |
|---|---|---|---|
| First snapshot counter: 1 | Choice | Start each account's recorded snapshot sequence at a whole-number position. | 0.5 would introduce fractional positions. |
| Increment per later snapshot: 1 | Choice | Assign the next position from the calculation base's counter. | 0.5 would change that counting scheme. |
| Counter increment on a decline: 0 | Choice | Preserve the account version because funds and holds do not change. | Half remains zero. The decision and ID are still recorded. |
| Counters 10 and 11 | Example | Illustrate an account advancing while Yield calculates. | They are example versions, not configuration values. |

For a transaction that creates a snapshot, `candidate.event_counter = base.last_event_counter + 1`. Declines consume no position. A changed base requires recalculation; do not put a new counter on an old result.

**Source:** [snapshot decision](AMBIGUITIES.md#snapshot-recording-and-retries) and [architecture version example](../architecture.md#calculation-version-validation).

Only add further constants when supported by an approved decision or an identified scenario. [Open calculation decisions](AMBIGUITIES.md#pending-calculation-decisions) still prevent final replay totals.

## BANK-SPEC Replay Outcomes

These results use the explicit [SPEC checkpoint schedule](../implementation/bank-spec/SPEC.md#6-replay-schedule-and-independent-expectations), not a generalized banking calendar.

The financial intent correction changes failure recovery and accepted command metadata, with no change to these numerical rules or replay boundaries. Its [executed demo](evidence/bank-spec-financial-intents/demo.txt) matches the original twelve six-day reports and separate Day 7 continuation exactly.

| Quantity | Derivation and boundary |
|---|---|
| Initial E1/E2 balance | `1200.00 - 950.00 = 250.00`; supplied principal inputs. |
| E3 availability | `250.00 - 200.00 = 50.00`; holds do not change financial balance. |
| E4/E5/E6 financial balance | `250.00 + 400.00 - 185.00 - 180.00 = 285.00`. E5 releases unused 15.00 without credit. |
| E7 historical H2/H3/H4 principal | `-370.00`, `30.00`, `-335.00`; exclude E9, fees and capitalization. |
| E7 current balance | `285.00 - 620.00 = -335.00`; E8 would leave availability -425.00 and is declined. |
| Original H1 through H5 interest | `[0.10, 0.10, 0.26, 0.11, 0.00]`, totaling 0.57 after each day's rounding. |
| Day 6 fees | H2, H4 and H5 each require 25.00, totaling 75.00. H3 is positive. All three are booked and valued Day 6. |
| E7 interest adjustments | H2 `0.00 - 0.10 = -0.10`; H3 `0.01 - 0.26 = -0.25`; H4 `0.00 - 0.11 = -0.11`; total -0.46, booked Day 6. |
| Day 6 payment/close | Payment 0.57 excludes Day 6 adjustments. `-335.00 - 75.00 + 620.00 + 0.57 = 210.57`. |
| Day 7 reversal refunds | Three refunds of 25.00, booked Day 7 and valued Day 6, produce current 285.57. |
| Day 7 interest corrections | H2/H3/H4/H5 `[+0.10,+0.25,+0.11,+0.11]`; total +0.57; prior-month unpaid net `-0.46 + 0.57 = 0.11`. |
| Ordinary Day 6, calculated Day 7 | `210.57 * 0.0004 = 0.084228`, rounded to 0.08. Day 7 refunds are excluded by booking cutoff 6. New-month amount. |
| BHD late correction | Day 6 records +0.004 for H5, excluded from zero Day 6 settlement. New-month H6 adds 0.004 on Day 7, giving total pending 0.008. |
| Snapshot counts | ACC-001 counters 12 at Day 6 and 15 after three Day 7 refunds; ACC-002 counter 1. E3 creates a hold snapshot; E8 decline and zero settlement create none. |

The independent test oracle uses integer minor units and `0.0004 = 1/2500`, with positive HALF_UP computed as integer division after adding 1250. This is a test derivation of the supplied rate and rounding, not another production constant. The minimum three-installment total is three currency minor units, ensuring all three journal amounts are positive; halving it cannot produce three positive postings. E10 is much larger and unchanged.

Logical days start at 1 because the exercise names Day 1; initial state/counters/journal position use zero before recorded events. The next position increments by 1, not a fractional quantity. Day 5 month end, Day 6 first business day, and the separate Day 7 continuation are approved fixture boundaries. There is no chosen wall-clock time, annual divisor, retry count, backoff interval or expiration duration.
