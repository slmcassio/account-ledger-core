# Business Rules

**Project rounding decision:** use HALF_UP with two decimal places for AED and three for BHD. The explicit assumption is to favor the recipient of positive interest at exact ties, accepting the upward bias in those cases. See the [rounding decision and source limits](deliverables/AMBIGUITIES.md#rounding-mode).

The table below records exercise requirements; the rounding mode above is a project choice.

| ID | Business rule | Applicability | Required behavior |
|---|---|---|---|
| BR01 | **Monetary precision** | Storing or rounding monetary amounts. | Use **two decimal places for AED** and **three for BHD**, according to the amount's currency. |
| BR02 | **Closing ledger balance** | Calculating an account's closing balance for a given day. | Include all entries with a `value_date` **on or before the day being evaluated**. |
| BR03 | **Available balance** | Calculating the funds available for new authorizations. | Calculate **ledger balance minus the total of active holds**. |
| BR04 | **Effect of a hold** | Applying a hold resulting from an approved authorization. | Reduce the **available balance** without changing the **ledger balance**. |
| BR05 | **Authorization approval** | Receiving a new authorization request. | Approve only if the available balance, **after applying the new hold**, remains **at or above zero**. |
| BR06 | **Settlement without an existing authorization** | Receiving a settlement that references an authorization ID that does not exist. | **Reject the settlement and prevent funds from leaving** the account. |
| BR07 | **Overdraft fee assessment** | A day's closing ledger balance is **below zero**. | Assess **AED 25.00**, once per day, per account. |
| BR08 | **Fee value date** | Recording an overdraft fee. | Set `value_date` to the **day assessed**. The interpretation for late adjustments is recorded below. |
| BR09 | **Daily interest accrual** | Calculating daily interest on the closing ledger balance. | Apply **0.04% per day to positive balances only**. Zero or negative balances do not accrue interest. |
| BR10 | **Interest capitalization** | End of Day 6. | Capitalize accrued interest as **a single credit**. |
| BR11 | **Interest reconciliation** | Determining the total interest to capitalize. | Ensure that the **sum of rounded daily interest accruals equals the capitalized total exactly**. |
| BR12 | **Immutable history** | Recording and correcting events, including reversals. | Only append records to the ledger. **No existing event record may be modified or deleted**. |

## Approved Interpretation: Late Transactions

This covers legitimate transactions delivered after they occurred, such as an official transaction from Mastercard received later. System error corrections are out of scope for this interpretation.

* `booking_date` is the accounting recording day; `value_date` is the day the transaction starts affecting the balance. Preserve the supplied dates and event order.
* Append a separate adjustment for differences in affected fees and interest. Link it to the original transaction and retain a breakdown by historical day and type. Do not repeat the original transaction amount.
* Use the correction day for both dates of the adjustment. Interpret "day assessed" as the current assessment day; historical days remain calculation references.
* Calculate each component as `corrected amount - net amount already recorded`. Correct unpaid interest separately from the account balance until capitalization; corrections to credited interest affect the balance.

See the [decision and rationale](deliverables/AMBIGUITIES.md#late-transaction-adjustments) and [worked example](examples/04-backdated-adjustment.md).

## Open Questions

* **Rounding precision and stages:** What intermediate precision should calculations retain, and at which stages should rounding occur?
* **Fee in another currency:** How should an overdraft fee denominated in AED apply to a BHD account?
* **Closing checkpoints and reversals:** When should daily calculations run, and which fees and interest should a reversal correct?
* **Holds:** When should the remaining hold be released after a settlement below the held amount, and do holds expire?
