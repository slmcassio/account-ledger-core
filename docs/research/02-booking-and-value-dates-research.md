# Booking dates, value dates, and corrections

## Scope

This report covers legitimate transactions delivered after they occurred, such as a delayed transaction from Mastercard. It excludes corrections of system errors.

## Date definitions

* **`booking_date`:** the day the transaction is recorded in the accounting books, supplied as "Booked".
* **`value_date`:** the day from which the transaction affects the balance.

Neither date necessarily identifies when an event occurred or arrived. Preserve the supplied dates and event order. Append records without changing earlier records.

The [exercise](../exercise-inputs/exercise-statement.md) requires a fee's `value_date` to equal the "day assessed". For late transaction adjustments, the project uses the current assessment day; historical days identify calculation periods. [Reversal fee refunds](08-reversals-research.md#correcting-fees-and-interest-after-a-reversal) are a narrow exception: current booking, original fee value date, under the [accepted decision](../deliverables/AMBIGUITIES.md#reversal-compensation).

## Approved adjustment method

1. Append the original transaction with its supplied dates.
2. Recalculate the affected fees and interest. For each component, calculate `adjustment = corrected amount - net amount already recorded`.
3. Append only a nonzero adjustment, separately linked to the transaction, using the correction day for both dates except for reversal fee refunds above.
4. Keep a breakdown by historical day and component.

The net amount includes the original result and **all prior adjustments, paid or unpaid**. Never repeat the principal. Fee differences debit or credit the ledger. Interest differences, including corrections of paid periods, [remain pending until eligible payment](10-interest-capitalization-research.md#approved-payment-and-components); that study defines their balance treatment, rationale and payment limits under the [component settlement decision](../deliverables/AMBIGUITIES.md#interest-adjustments-wait-for-payment).

## Example

The [legacy example 04](../examples/04-backdated-adjustment.md), outside research, follows three transactions and their adjustment: AED 75.00 for three negative closing days and an AED 1.20 reduction in unpaid interest. Its AED 0.58 final credit follows its retained earlier schedule, not the [approved booking cutoff](06-daily-closing-research.md#agreed-operation).

## Sources and limits

* [ISO 20022][iso], camt.053.001.08, sections 3.4.2.15.6 and 3.4.2.15.7, page 62: booking refers to posting on the account servicer's books. Value date concerns when assets become or cease to be available. These are message definitions, not a ledger adjustment policy.
* [Mambu dates][dates], "Booking date input under accounting closure": later booking can retain an earlier value date.
* [Mambu backdating][backdating] and [adjustments][adjustments]: Mambu reverses and reposts later transactions and recalculates interest. This product behavior does not establish the project's adjustment method or dates.
* [CBUAE Consumer Protection Standards][cbuae], sections 5.1.1.38 to 5.1.1.40, printed page 45 (PDF page 46): covered institutions must correct their errors and refund resulting deductions or costs. These provisions do not establish a policy for legitimate delayed transactions.

[iso]: https://www.iso20022.org/sites/default/files/documents/messages/mdr_part_2/ISO20022_MDRPart2_BankToCustomerCashManagement_2018_2019_v1_0.pdf
[dates]: https://docs.mambu.com/docs/booking-date-vs-value-date/#booking-date-input-under-accounting-closure
[backdating]: https://docs.mambu.com/docs/deposits-withdrawals-and-transfers/#backdating-deposits-and-withdrawals
[adjustments]: https://docs.mambu.com/docs/adjusting-transactions/#adjusting-transactions-in-bulk
[cbuae]: https://www.centralbank.ae/media/5crd24gm/cp-standards-pdf.pdf#page=46
