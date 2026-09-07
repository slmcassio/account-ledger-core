# Business Rules

| ID | Business rule | Applicability | Required behavior |
|---|---|---|---|
| BR01 | **Monetary precision** | Storing or rounding monetary amounts. | Use **two decimal places for AED** and **three for BHD**, according to the amount's currency. |
| BR02 | **Closing ledger balance** | Calculating an account's closing balance for a given day. | Include all entries with a `value_date` **on or before the day being evaluated**. |
| BR03 | **Available balance** | Calculating the funds available for new authorizations. | Calculate **ledger balance minus the total of active holds**. |
| BR04 | **Effect of a hold** | Applying a hold resulting from an approved authorization. | Reduce the **available balance** without changing the **ledger balance**. |
| BR05 | **Authorization approval** | Receiving a new authorization request. | Approve only if the available balance, **after applying the new hold**, remains **at or above zero**. |
| BR06 | **Settlement without an existing authorization** | Receiving a settlement that references an authorization ID that does not exist. | **Reject the settlement and prevent funds from leaving** the account. |
| BR07 | **Overdraft fee assessment** | A day's closing ledger balance is **below zero**. | Assess **AED 25.00**, once per day, per account. |
| BR08 | **Fee value date** | Recording an overdraft fee. | Set `value_date` to the **day to which the fee assessment applies**. |
| BR09 | **Daily interest accrual** | Calculating daily interest on the closing ledger balance. | Apply **0.04% per day to positive balances only**. Zero or negative balances do not accrue interest. |
| BR10 | **Interest capitalization** | End of Day 6. | Capitalize accrued interest as **a single credit**. |
| BR11 | **Interest reconciliation** | Determining the total interest to capitalize. | Ensure that the **sum of rounded daily interest accruals equals the capitalized total exactly**. |
| BR12 | **Immutable history** | Recording and correcting events, including reversals. | Only append records to the ledger. **No existing event record may be modified or deleted**. |

## Open Questions

- **Rounding:** Which rounding mode should be used?
- **Fee in another currency:** How should an AED-denominated overdraft fee apply to a BHD account?
- **Backdated entries and reversals:** How should previously assessed fees and accrued interest be adjusted?
- **Holds:** When should the remaining hold be released after a settlement below the held amount, and do holds expire?
