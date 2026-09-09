# Corrected Business Rules

This document combines exercise requirements with approved project interpretations and exceptions.

**Project rounding decision:** use HALF_UP with two decimal places for AED and three for BHD. The explicit assumption is to favor the recipient of positive interest at exact ties, accepting the upward bias in those cases. See the [rounding decision and source limits](../deliverables/AMBIGUITIES.md#rounding-mode).

The [daily interest interpretation](#approved-interpretation-daily-interest-calculation) defines its exact multiplication, single rounding and reconciliation stages.

BR06 records the approved project choice for confirmed settlements. The other rows summarize exercise requirements. Day 6 in BR10 is the endpoint of the supplied six-day scenario.

| ID | Business rule | Applicability | Required behavior |
|---|---|---|---|
| BR01 | **Monetary precision** | Storing or rounding monetary amounts. | Use **two decimal places for AED** and **three for BHD**, according to the amount's currency. |
| BR02 | **Closing ledger balance** | Calculating an account's closing balance for a given day. | Include all entries with a `value_date` **on or before the day being evaluated**. |
| BR03 | **Available balance** | Calculating the funds available for new authorizations. | Calculate **ledger balance minus the total of active holds**. |
| BR04 | **Effect of a hold** | Applying a hold resulting from an approved authorization. | Reduce the **available balance** without changing the **ledger balance**. |
| BR05 | **Authorization approval** | Receiving a new authorization request. | Approve only if the available balance, **after applying the new hold**, remains **at or above zero**. |
| BR06 | **Settlement with a missing local authorization** | Recording a legitimate, externally confirmed settlement whose authorization is absent from the local ledger. | **Append the debit and report the missing authorization.** Preserve the supplied reference and dates; do not create an authorization or hold. |
| BR07 | **Overdraft fee assessment** | A day's closing ledger balance is **below zero**. | Literal exercise rule: assess **AED 25.00**, once per day, per account. The approved currency exception is recorded below. |
| BR08 | **Fee value date** | Recording an overdraft fee. | Set `value_date` to the **day assessed**. The interpretations for late adjustments and reversal refunds are recorded below. |
| BR09 | **Daily interest accrual** | Calculating daily interest on the closing ledger balance. | Apply **0.04% per day to positive balances only**. Zero or negative balances do not accrue interest. |
| BR10 | **Interest capitalization** | End of Day 6. | Capitalize accrued interest as **a single credit**. |
| BR11 | **Interest reconciliation** | Determining the total interest to capitalize. | Ensure that the **sum of rounded daily interest accruals equals the capitalized total exactly**. |
| BR12 | **Immutable history** | Recording and correcting events, including reversals. | Only append records to the ledger. **No existing event record may be modified or deleted**. |

## Approved Interpretation: Settlements

For this project, SETTLEMENT reports a legitimate payment that has already settled outside the ledger. This assumption explains BR06: the debit must reflect the payment even when the local authorization record is missing.

See the [decision and rationale](../deliverables/AMBIGUITIES.md#settlements-with-a-missing-authorization) and the [rejection of criterion 4](../deliverables/REJECTED.md#acceptance-criterion-4).

## Approved Interpretation: Late Transactions

This interpretation covers legitimate transactions delivered after they occurred. It excludes system error corrections.

* `booking_date` is the accounting recording day; `value_date` is the day the transaction starts affecting the balance. Preserve the supplied dates and event order.
* Append a separate adjustment for differences in affected fees and interest. Link it to the original transaction and retain a breakdown by historical day and type. Do not repeat the original transaction amount.
* Use the correction day for both dates of the adjustment. Interpret "day assessed" as the current assessment day; historical days remain calculation references. The reversal refund policy below is a separate, limited exception.
* Calculate each component as `corrected amount - (original amount + all prior adjustments)`, including interest adjustments already paid. Fee adjustments affect the ledger. Keep every interest difference pending until the next regular payment whose booking cutoff admits it, including corrections for previously paid periods. Pending interest is unavailable and earns no interest.
* Capitalize eligible unpaid daily accruals and adjustments once, recording which components the payment settles. Preserve earlier payments and exclude settled components from later payments, while retaining them for future difference calculations. See the [decision, rationale, and negative-total limit](../deliverables/AMBIGUITIES.md#interest-adjustments-wait-for-payment).

See the [decision and rationale](../deliverables/AMBIGUITIES.md#late-transaction-adjustments) and [worked example](../examples/04-backdated-adjustment.md).

## Approved Interpretation: Reversals

Under [approved method B](../deliverables/AMBIGUITIES.md#reversal-compensation), reverse principal once with its supplied dates and recalculate all affected fees and daily interest from the affected value day onward, including later periods whose bases change within the calculation boundary. Append only linked differences from original amounts plus all prior adjustments, including paid ones. This does not automatically refund every fee. Corrections concern legitimate transactions; system errors are excluded by explicit simplification.

Book each reversal fee refund on the correction day and value it at the original charge's value date, which need not be H or the principal's value date. Funds become available when recorded; only reconstructed balances change historically. This limited exception does not change interest correction dates or their pending payment treatment, including for paid periods.

Preserve records, supplied event order and earlier authorization decisions. The [full decision](../deliverables/AMBIGUITIES.md#reversal-compensation) explains the rationale and limits; the [study](../research/08-reversals-research.md) compares the alternatives.

## Approved Interpretation: Authorization Responsibilities

Authorization guards available balance using its latest operational snapshot, which includes financial state and active holds. It sends financial data to Ledger and never consumes Ledger data. BR03 compares the same set of financial events; a Ledger report may temporarily lag that set during delivery. This replaces the earlier Ledger-supplied balance approach.

Transaction communicates only with Authorization. After recording the transaction and snapshot, Authorization forwards approved transactions to Yield and Fees. The required test suite or script handles replay and daily report inspection outside the Transaction module.

Transactions supply their own IDs. Skip a recorded ID, including a declined request's ID, before repeating calculations or inspecting its amount or content, even if the content differs. It creates no new snapshot or counter increment. Financial events and hold changes create snapshots; their per-account counters start at 1 and advance by one. Calculate from the latest saved snapshot and assign its counter plus one to each new snapshot candidate.

A declined authorization records its decision and ID without changing funds, holds, snapshot or counter. It does not invalidate Yield's source version. Under the [snapshot and retry decision](../deliverables/AMBIGUITIES.md#snapshot-recording-and-retries), check ID uniqueness and the unchanged calculation base indivisibly with recording the outcome: decision only for a decline, transaction plus snapshot otherwise. A changed base requires reevaluating the uncommitted request with the same ID. A recorded decline remains unchanged on redelivery, even after funds increase.

Authorization records first and sends financial data to Ledger afterward; Ledger uses that ID to prevent duplicate journal entries. This [separate delivery](../deliverables/AMBIGUITIES.md#ledger-delivery) may be asynchronous.

Uncommitted attempts publish no Ledger posting. All financial effects, including fees and capitalization, enter through this path. Confirmed payments retain their debit even if authorization is missing or availability becomes negative; the new-hold approval check does not apply to them. Unpaid interest remains outside available balance until capitalization.

Preserve the original authorization decision after a balance correction. Do not automatically reevaluate it. A later increase in funds does not activate a declined request. Evaluate a new explicit request against the updated balance and active holds. See the [decision and rationale](../deliverables/AMBIGUITIES.md#authorization-and-ledger-responsibilities).

## Approved Interpretation: Hold Lifecycle

* Record the actual debit of a legitimate, externally settled payment independently of changes to a matching active hold. An absent, released, or expired hold does not prevent recording that debit.
* A partial, non-final settlement reduces the reservation by the settled portion and keeps the remainder active. A final settlement also releases the unused portion.
* A release without settlement frees the specified reserved amount without creating a ledger debit or credit.
* Preserve the original authorization and decision records. Append information explaining reservation changes and derive the remaining active amount from that history.
* Treat Auth-A's AED 185.00 settlement as final by explicit scenario assumption: end its AED 200.00 reservation and release the unused AED 15.00 without a ledger credit.
* Generate no automatic expiration during the supplied six-day replay. This is a bounded assumption, not a rule that holds never expire. Auth-B has a hold only if approved.

See the [settlement and release decision](../deliverables/AMBIGUITIES.md#hold-settlement-and-release), [expiration decision](../deliverables/AMBIGUITIES.md#hold-expiration-during-the-replay), and [worked example](../examples/07-hold-lifecycle.md).

## Approved Interpretation: Daily Interest Calculation

For each account and day, preserve the exact product `max(daily_base, 0) * 0.0004`, then round once with HALF_UP to that currency's precision. Do not round intermediate interest or carry fractions between days. Reconcile corrections against rounded daily targets, including all earlier adjustments, even paid ones. At payment, sum eligible unpaid daily accruals and adjustments exactly and settle each once; do not round their aggregated raw products or discard a difference.

This approved interpretation of BR09 and BR11 leaves the daily bases and payment details subject to the existing open decisions. It changes neither the booking cutoff nor adjustment dates and pending treatment. See the [decision and limits](../deliverables/AMBIGUITIES.md#daily-interest-calculation) and [small examples](../research/09-daily-interest-research.md).

## Approved Interpretation: Active Calculations

Process financial events in the supplied order and update the running balance. An intraday deficit alone triggers no daily fee. Run one daily job in D for reference D-1, selecting cumulative input bookings with `booking_date <= D-1`. Within that set, use `value_date <= calculated day` for closing balances. Pending interest does not enter the balance. The job's accrual and payment are outputs, not inputs excluded by this cutoff.

Keep one interest credit per account at the end of Day 6, with AED and BHD calculated separately. This payment uses reference Day 5 and both dates are Day 6. E9 and adjustments booked on Day 6 do not change its amount, even if already processed. The credit enters Day 6 bases evaluated on Day 7. Day 6 interest is calculated on Day 7 for later payment.

The [approved monthly schedule](../deliverables/AMBIGUITIES.md#interest-payment-schedule-and-capitalization) pays on the first business day for the previous month's ordinary accruals plus eligible unpaid adjustments, including corrections of older paid periods. Keep this period separate from the booking cutoff; current month ordinary accruals remain pending. Record payments with both dates on the actual payment day, after their calculations. Follow the component settlement rules above.

[Study 10](../research/10-interest-capitalization-research.md) explains this choice. The business day calendar, mapping of scenario days to months, relationship to the fixed Day 6 credit, and later payment date for Day 6 interest remain unresolved. Final amounts and negative total settlement also remain [pending](../deliverables/AMBIGUITIES.md#pending-calculation-decisions).

Yield reconstructs interest bases from approved transactions forwarded by Authorization after recording and from opening state. It receives no input directly from Transaction and consumes no Ledger or Authorization balance. Its payment carries the target account's source-view counter, distinct from the new payment counter. Authorization skips recorded IDs before inspecting amount or source version. Otherwise it requires the source version to equal its current snapshot version as part of conditional payment and snapshot recording, assigning the payment that counter plus one. A mismatch applies no payment and requests recalculation using approved transactions for the latest account counter, retaining the unrecorded payment's supplied ID. See the [decision and input completeness limits](../deliverables/AMBIGUITIES.md#yield-calculation-and-payment). The required interest rate, currency rounding, and exact-sum reconciliation still apply.

Tax charges on yield and changes to those taxes are outside scope.

The current source account counter remains mandatory at payment even when a new booking is excluded and the eligible amount is unchanged. Validation of cutoff-relevant calculation records remains a separate proposal under the [timing decision](../deliverables/AMBIGUITIES.md#daily-calculation-timing). The [earlier worked example](../examples/08-daily-closing.md) awaits alignment with the cutoff.

## Approved Interpretation: Overdraft Fee Assessment Base

For the job in D, select inputs cumulatively with `booking_date <= D-1`. For historical day H, calculate the balance using those inputs with `value_date <= H`. Exclude only H's own fee components and related adjustments already included in that balance. Keep other periods' charges and refunds at their actual value dates. Holds and pending interest do not enter this base.

When that base is negative, assess the [configured fee in the account's currency](#approved-exception-overdraft-fee-currency), otherwise zero. This interpretation of BR07 prevents a fee from sustaining itself. The reported ledger balance retains all financial entries passing both filters; only the fee assessment base changes.

Calculate `corrected fee - (original fee + all earlier adjustments)`, including adjustments excluded from the historical balance. Append only a nonzero difference with a linked historical breakdown. Positive differences debit; negative differences refund. Legitimate late transaction adjustments keep both dates on the actual correction day. Reversal fee refunds instead use current booking and the original charge's value date under the limited exception above. Other periods' refunds remain in the filtered base from that value date. Fee submissions require the same current `source_event_counter` validation as interest payments.

See the [assessment decision and limits](../deliverables/AMBIGUITIES.md#overdraft-fee-assessment-base) and [study 07](../research/07-overdraft-fees-research.md). Its isolated examples define no final balances or total fee count.

## Approved Exception: Overdraft Fee Currency

Configure the daily overdraft fee by account type in its own currency: AED 25.00 for the type corresponding to ACC-001 and BHD 0.000 for the type corresponding to ACC-002. The zero BHD fee is an explicit exception to BR07's literal requirement, chosen for simplicity because conversion requirements are missing. No exchange rate or additional type is inferred.

A zero fee does not prohibit negative balances or allow an unfunded authorization. BR05 still requires nonnegative available balance after a new hold, in the account's currency; BR06 still records legitimate confirmed debits. See the [decision and limits](../deliverables/AMBIGUITIES.md#overdraft-fee-currency) and [study 12](../research/12-fee-currency-research.md).

## Approved Interpretation: E10 Installments

E10 credits ACC-002 with BHD 10.000 in three installments, all booked and valued on Day 5. Preserve E10 after E9. Allocate BHD 3.333, 3.333 and 3.334, totaling 10.000. Assigning the remainder to installment 3 is an approved convention independent of HALF_UP.

If installments are individual financial credits, link them to E10 without also crediting the parent. Representation, IDs, event count and counter effects remain unspecified. No installment calendar, interest between installments or general allocation algorithm is introduced.

See the [decision and rationale](../deliverables/AMBIGUITIES.md#e10-installment-allocation), [numerical derivation](../deliverables/NUMBERS.md#e10-installment-values), and [study 11](../research/11-installments-research.md).

## Open Questions

Calculation questions and their study dependencies are listed in [AMBIGUITIES](../deliverables/AMBIGUITIES.md#pending-calculation-decisions).

* **Hold expiration beyond the replay:** What duration or deadline, time reference, and update rules should a general expiration policy use?
