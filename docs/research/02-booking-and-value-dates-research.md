# Booking dates, value dates, and corrections

## Scope

This report covers legitimate transactions delivered after they occurred, such as a delayed transaction from Mastercard. It excludes corrections of system errors.

## Date definitions

* **`booking_date`:** the day the transaction is recorded in the accounting books, supplied as "Booked".
* **`value_date`:** the day from which the transaction affects the balance.

Neither date necessarily identifies when an event occurred or arrived. Preserve the supplied dates and event order. Append records without changing earlier records.

The [exercise](../exercise-statement.md) requires a fee's `value_date` to equal the "day assessed". The project interprets this as the current assessment day for an adjustment. Historical days identify the calculation periods.

## Approved adjustment method

1. Append the original transaction with its supplied dates.
2. Recalculate the affected fees and interest. For each component, calculate `adjustment = corrected amount - net amount already recorded`.
3. Append a separate adjustment linked to the transaction. Use the correction day for both `booking_date` and `value_date`.
4. Keep a breakdown by historical day and component.

The adjustment can charge or refund fees and increase or reduce interest. It does not repeat the original transaction amount. Corrections to unpaid interest change the future interest payment. Corrections to credited interest affect the account balance.

## Example

The [fictional example](../examples/04-backdated-adjustment.md) follows three transactions and their adjustment. The adjustment charges AED 75.00 for three negative closing days and reduces unpaid interest by AED 1.20. The final interest credit is AED 0.58.

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
