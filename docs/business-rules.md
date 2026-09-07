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
| BR08 | **Fee value date** | Recording an overdraft fee. | Set `value_date` to the **day assessed**. The interpretation for late adjustments is recorded below. |
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
* Use the correction day for both dates of the adjustment. Interpret "day assessed" as the current assessment day; historical days remain calculation references.
* Calculate each component as `corrected amount - net amount already recorded`. Correct unpaid interest separately from the account balance until capitalization; corrections to credited interest affect the balance.

See the [decision and rationale](deliverables/AMBIGUITIES.md#late-transaction-adjustments) and [worked example](examples/04-backdated-adjustment.md).

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

Calculate during event processing in an active system, preserving the supplied order. Financial entries update the running balance; daily closing and historical recalculation are separate operations. A closing result uses the records known at its calculation point and may need revision after later arrivals.

The operational schedule and replay checkpoints remain proposals. See the [decision and open details](deliverables/AMBIGUITIES.md#daily-calculation-timing) and [worked example](examples/08-daily-closing.md).

## Open Questions

* **Rounding precision and stages:** What intermediate precision should calculations retain, and are any stages needed beyond the required currency rounding and rounded daily accruals?
* **Fee in another currency:** How should an overdraft fee denominated in AED apply to a BHD account?
* **Closing checkpoints and reversals:** Which daily schedule, business time zone, and replay checkpoints should apply, and which fees and interest should a reversal correct?
* **Hold expiration beyond the replay:** What duration or deadline, time reference, and update rules should a general expiration policy use?
