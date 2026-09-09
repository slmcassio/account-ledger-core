# Business Rules

**Project rounding decision:** use HALF_UP with two decimal places for AED and three for BHD. The explicit assumption is to favor the recipient of positive interest at exact ties, accepting the upward bias in those cases. See the [rounding decision and source limits](deliverables/AMBIGUITIES.md#rounding-mode).

BR06 records the approved project choice for confirmed settlements. The other rows summarize exercise requirements. Day 6 in BR10 is the endpoint of the supplied six-day scenario.

| ID | Business rule | Applicability | Required behavior |
|---|---|---|---|
| BR01 | **Monetary precision** | Storing or rounding monetary amounts. | Use **two decimal places for AED** and **three for BHD**, according to the amount's currency. |
| BR02 | **Closing ledger balance** | Calculating an account's closing balance for a given day. | Include all entries with a `value_date` **on or before the day being evaluated**. |
| BR03 | **Available balance** | Calculating the funds available for new authorizations. | Calculate **ledger balance minus the total of active holds**. |
| BR04 | **Effect of a hold** | Applying a hold resulting from an approved authorization. | Reduce the **available balance** without changing the **ledger balance**. |
| BR05 | **Authorization approval** | Receiving a new authorization request. | Approve only if the available balance, **after applying the new hold**, remains **at or above zero**. |
| BR06 | **Settlement with a missing local authorization** | Recording a legitimate, externally confirmed settlement whose authorization is absent from the local ledger. | **Append the debit and report the missing authorization.** Preserve the supplied reference and dates; do not create an authorization or hold. |
| BR07 | **Overdraft fee assessment** | A day's closing ledger balance is **below zero**. | Assess **AED 25.00**, once per day, per account. |
| BR08 | **Fee value date** | Recording an overdraft fee. | Set `value_date` to the **day assessed**. The interpretations for late adjustments and reversal refunds are recorded below. |
| BR09 | **Daily interest accrual** | Calculating daily interest on the closing ledger balance. | Apply **0.04% per day to positive balances only**. Zero or negative balances do not accrue interest. |
| BR10 | **Interest capitalization** | End of Day 6. | Capitalize accrued interest as **a single credit**. |
| BR11 | **Interest reconciliation** | Determining the total interest to capitalize. | Ensure that the **sum of rounded daily interest accruals equals the capitalized total exactly**. |
| BR12 | **Immutable history** | Recording and correcting events, including reversals. | Only append records to the ledger. **No existing event record may be modified or deleted**. |

## Approved Interpretation: Settlements

For this project, SETTLEMENT reports a legitimate payment that has already settled outside the ledger. This assumption explains BR06: the debit must reflect the payment even when the local authorization record is missing.

See the [decision and rationale](deliverables/AMBIGUITIES.md#settlements-with-a-missing-authorization) and the [rejection of criterion 4](deliverables/REJECTED.md#acceptance-criterion-4).

## Approved Interpretation: Late Transactions

This interpretation covers legitimate transactions delivered after they occurred. It excludes system error corrections.

* `booking_date` is the accounting recording day; `value_date` is the day the transaction starts affecting the balance. Preserve the supplied dates and event order.
* Append a separate adjustment for differences in affected fees and interest. Link it to the original transaction and retain a breakdown by historical day and type. Do not repeat the original transaction amount.
* Use the correction day for both dates of the adjustment. Interpret "day assessed" as the current assessment day; historical days remain calculation references. The reversal refund policy below is a separate, limited exception.
* Calculate each component as `corrected amount - (original amount + all prior adjustments)`, including interest adjustments already paid. Fee adjustments affect the ledger. Keep every interest difference pending until the next regular payment whose booking cutoff admits it, including corrections for previously paid periods. Pending interest is unavailable and earns no interest.
* Capitalize eligible unpaid daily accruals and adjustments once, recording which components the payment settles. Preserve earlier payments and exclude settled components from later payments, while retaining them for future difference calculations. See the [decision, rationale, and negative-total limit](deliverables/AMBIGUITIES.md#interest-adjustments-wait-for-payment).

See the [decision and rationale](deliverables/AMBIGUITIES.md#late-transaction-adjustments) and [worked example](examples/04-backdated-adjustment.md).

## Approved Interpretation: Reversals

Every correction in this exercise concerns a legitimate transaction; system errors are outside its simplified scope. Append the principal reversal once with the supplied dates, preserving all records and event order. Recalculate all affected fees and interest from the affected value day onward within the applicable input boundary, recording only the differences from originals plus all earlier adjustments, including paid ones.

Book fee refunds caused by reversal on the actual correction day, but value each at the original charge's value date. This limited exception to late transaction adjustment dating neutralizes the fee's historical effect without changing when the refund was recorded. Interest corrections keep both dates on the actual correction day and stay pending until the next eligible regular payment, including corrections of paid periods. Preserve actual payments and settle each component once; pending interest earns nothing.

E9 remains an AED 620.00 credit with booking Day 6 and value Day 2. E10 stays after E9 and E6's confirmed debit remains. Day 6 bookings cannot alter the Day 6 payment referencing Day 5. No authorization is automatically reevaluated. See the [decision and accepted limits](deliverables/AMBIGUITIES.md#reversal-compensation) and [comparison](research/08-reversals-research.md).

## Approved Interpretation: Authorization Responsibilities

The ledger supplies the current accounting balance. Authorization controls active holds and records decisions using the balance and holds known when each request is processed. A declined request creates neither a hold nor a financial debit.

Preserve the original authorization decision after a balance correction. Do not automatically reevaluate it. A later increase in funds does not activate a declined request. Evaluate a new explicit request against the updated balance and active holds. See the [decision and rationale](deliverables/AMBIGUITIES.md#authorization-and-ledger-responsibilities).

## Approved Interpretation: Hold Lifecycle

* Record the actual debit of a legitimate, externally settled payment independently of changes to a matching active hold. An absent, released, or expired hold does not prevent recording that debit.
* A partial, non-final settlement reduces the reservation by the settled portion and keeps the remainder active. A final settlement also releases the unused portion.
* A release without settlement frees the specified reserved amount without creating a ledger debit or credit.
* Preserve the original authorization and decision records. Append information explaining reservation changes and derive the remaining active amount from that history.
* Treat Auth-A's AED 185.00 settlement as final by explicit scenario assumption: end its AED 200.00 reservation and release the unused AED 15.00 without a ledger credit.
* Generate no automatic expiration during the supplied six-day replay. This is a bounded assumption, not a rule that holds never expire. Auth-B has a hold only if approved.

See the [settlement and release decision](deliverables/AMBIGUITIES.md#hold-settlement-and-release), [expiration decision](deliverables/AMBIGUITIES.md#hold-expiration-during-the-replay), and [worked example](examples/07-hold-lifecycle.md).

## Approved Interpretation: Active Calculations

Process financial events in the supplied order and update the running balance. Run one daily job in D for reference D-1, selecting input entries with cumulative `booking_date <= D-1`. Within that set, use value dates to calculate closing balances. Pending interest does not enter the ledger balance. The job's accrual and payment are outputs, not input transactions excluded by this cutoff.

Clock times, replay checkpoints, and handling missing eligible records remain unresolved. See the [decision and open details](deliverables/AMBIGUITIES.md#daily-calculation-timing). The [earlier worked example](examples/08-daily-closing.md) awaits alignment with the cutoff.

## Approved Interpretation: Overdraft Fee Assessment Base

For the job in D, select inputs with cumulative `booking_date <= D-1`. For each period being evaluated, calculate the ledger balance using those inputs and their applicable value dates. Exclude only that period's own fee components and related adjustments already included in this balance to obtain the fee assessment base. Keep other periods' fees and refunds according to their actual value dates. Holds and pending interest do not enter this base.

Assess AED 25.00 when the resulting base is negative, otherwise zero. Reconcile that target with the original fee plus all earlier fee adjustments and append only the difference on the actual correction day. The reported ledger balance still includes every eligible financial entry with an applicable value date; the exclusion changes fee eligibility, not history.

This approved interpretation of BR07 prevents a fee from sustaining itself. Final E7 fee counts and replay checkpoints remain open under study 06. Study 08 defines reversal compensation; study 10 defines payment dates and their effect on later bases. The negative BHD case remains for study 12. See the [decision and limits](deliverables/AMBIGUITIES.md#overdraft-fee-assessment-base).

## Approved Interpretation: Daily Interest Calculation

For each account and day, preserve the exact product `max(daily_base, 0) * 0.0004`, then round once with HALF_UP to that currency's precision. Do not round intermediate interest or carry fractions between days. Reconcile corrections against rounded daily targets, including all earlier adjustments, even paid ones. At payment, sum eligible unpaid daily accruals and adjustments exactly and settle each once; do not round their aggregated raw products or discard a difference.

This approved interpretation of BR09 and BR11 leaves the daily bases and payment details subject to the existing open decisions. It changes neither the booking cutoff nor adjustment dates and pending treatment. See the [decision and limits](deliverables/AMBIGUITIES.md#daily-interest-calculation) and [small examples](research/09-daily-interest-research.md).

## Approved Interpretation: Interest Payment

Book and value interest credits on the actual payment day. A Day 6 credit joins Day 6 bases calculated on Day 7; it changes neither the Day 5 input base nor the payment already calculated from it.

Pay monthly on the first business day for the previous month's ordinary accruals, plus eligible unpaid adjustments, including those for older paid periods. Keep the monthly accrual period separate from the cumulative booking cutoff. Current month accruals remain pending; the job's newly calculated ordinary accrual can participate in payment for its month, but corrections booked on payment day cannot.

This schedule is a project choice. The exercise's fixed Day 6 credit remains required; calendar mapping, the applicable business days and the resulting later payment date for Day 6 interest remain unresolved. Missing eligible inputs and negative totals remain open. See the [decisions and rationale](deliverables/AMBIGUITIES.md#interest-payment-schedule-and-capitalization).

## Open Questions

* **Other rounding questions:** Intermediate precision and stages outside daily interest are not established by study 09. Installment allocation remains for study 11.
* **Fee in another currency:** How should an overdraft fee denominated in AED apply to a BHD account?
* **Closing checkpoints:** Which clock times, business time zone, and replay checkpoints should apply, and how should missing eligible records be handled?
* **Hold expiration beyond the replay:** What duration or deadline, time reference, and update rules should a general expiration policy use?
