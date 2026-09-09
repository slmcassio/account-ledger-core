# Interest capitalization

**Source:** Research from `main` at `ec4e20cc9eb9f788e5195be574b384ec97108f32`, reconciled with the local architectural decisions.

**Integration status:** The source review adopted signed settlement and the Day 6 monthly mapping below. Those updates still await incorporation into this worktree's deliverables. [AMBIGUITIES](../deliverables/AMBIGUITIES.md#interest-payment-schedule-and-capitalization) records the locally accepted payment rules, and [Pending Calculation Decisions](../deliverables/AMBIGUITIES.md#pending-calculation-decisions) retains the unresolved local calendar and negative-total decisions.

## Approved payment and components

The [exercise](../exercise-inputs/exercise-statement.md#nonnegotiable-rules) literally requires one interest credit per account at the end of Day 6. Keep AED and BHD separate. The [daily job](06-daily-closing-research.md#agreed-operation) references Day 5.

Sum eligible unpaid [daily accruals](09-daily-interest-research.md) and signed interest adjustments exactly, including corrections of paid periods. Record which components are settled, settle each once and preserve earlier payments.

**Independent inputs:** unpaid eligible AED accruals 0.10 and 0.20, plus eligible correction -0.02 for a paid period. Pay `0.10 + 0.20 - 0.02 = 0.28`; exclude that period's earlier payment.

## Approved signed settlement

At the scheduled payment, settle the eligible unpaid total according to its sign:

* Positive: credit the account.
* Negative: debit the account, even if its balance becomes negative.
* Zero: mark the components as settled without a financial movement.

This project choice recovers excess interest at regular settlement without carrying an eligible negative total forward. The exercise's credit wording does not specify negative or zero totals. Negative daily balances still earn zero, not negative interest.

**Independent example:** eligible AED accrual 0.10 and correction -0.30 give -0.20. Debit 0.20 at payment; a zero account balance becomes -0.20.

## Why pending interest earns nothing

Pending amounts affect neither ledger nor available balances and earn no interest. Do not calculate hypothetical returns from earlier payment dates. This approved simplification preserves the exercise's 0.04% daily rate.

[CBUAE Consumer Protection Standards](https://www.centralbank.ae/media/5crd24gm/cp-standards-pdf.pdf#page=26), 2.1.2.4 and 2.3.2.2(d) and (e), require disclosure of payment frequency and compounding basis. They establish neither this method nor its applicability or compliance. Currency alone does not determine jurisdiction; payment frequency alone does not determine compounding.

## Approved capitalization date

Book and value the resulting credit or debit on its actual payment day, after calculation. A Day 6 movement enters Day 6 fee and interest bases evaluated on Day 7; it cannot change Day 5 interest. Payment order overrides neither input dates nor [ordinary fee dates](07-overdraft-fees-research.md#approved-assessment-dates).

**Independent example:** Day 6 balance AED 12.49 before an eligible payment of 0.01, with complete inputs and no fees, holds or other movements. Day 7 calculates `12.50 × 0.0004 = 0.005 → 0.01`. Without the credit, `12.49 × 0.0004 = 0.004996 → 0.00`. Neither result changes the payment already made.

## Approved monthly payment

Pay on the first business day of each month for the previous month. Current month accruals remain pending; eligible unpaid adjustments can concern older periods. Booking eligibility is separate: the job's new ordinary accrual participates if it belongs to the payment period; corrections booked on payment day do not.

**Approved scenario mapping:** Day 6 is the first business day of a new month, when regular monthly settlement occurs; Day 5 ends the previous month. For an illustrative 30-day month, Days 1 through 6 correspond to dates 26, 27, 28, 29, 30 and 01. Day 6 ordinary interest belongs to the new month, is calculated on Day 7 and awaits payment on the first business day of the following month.

This project assumption selects no specific month, year, jurisdiction or holiday calendar. The [15-day reversal simulation](examples/08-reversal-15-day-simulation.md#inputs-and-assumed-schedule) uses a separate illustrative schedule.

## Relation to the architecture

Financial settlement follows Authorization's [source counter validation](../deliverables/AMBIGUITIES.md#yield-calculation-and-payment) and [snapshot and ID rules](../deliverables/AMBIGUITIES.md#snapshot-recording-and-retries). This study specifies the payment policy, not the event representation or counter effects of a zero settlement. Daily arithmetic follows the [adopted daily calculation](../deliverables/AMBIGUITIES.md#daily-interest-calculation).

## Remaining questions

[Calculation checkpoints and final amounts](06-daily-closing-research.md#decisions-still-open) remain open; actual future payment dates require a business day calendar. A correction after payment waits for the next eligible monthly payment.
