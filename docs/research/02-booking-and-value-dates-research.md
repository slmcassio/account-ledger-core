# Booking dates, value dates, and corrections

## Reference and scope

This report explains accounting dates and corrections. The [exercise](../exercise-statement.md) supplies the rules; the example uses fictional transactions.

**Scope:** Only legitimate transactions delivered after they occurred are considered here, for example an official transaction received from Mastercard at a later date. These events do not result from system errors. Corrections related to system errors are out of scope.

## Rule and open question

* **`booking_date`:** the day the transaction is recorded in the accounting books, supplied as "Booked".
* **`value_date`:** the day from which the transaction affects the balance.

These dates do not necessarily say when an event occurred or arrived. Existing records and the supplied processing order must remain unchanged.

The exercise requires a fee's `value_date` to equal the "day assessed". For the adopted adjustment, interpret this as the day the correction is assessed and recorded. The historical days identify what was recalculated.

## Analysis

**Adopted approach:**

1. Append the original transaction with its supplied dates.
2. Recalculate affected days and append a separate adjustment linked to that transaction. Use the correction day for both its `booking_date` and `value_date`. Keep the historical days in its fee and interest breakdown.

For each component: `adjustment = corrected amount - net amount already recorded`.

The adjustment can charge or refund fees and increase or reduce interest, without repeating the original transaction amount. It preserves previous records. Reversing and reposting calculations is an alternative with more records.

## Example

The [complete fictional example](../examples/04-backdated-adjustment.md) follows three transactions, their daily balances, a separate adjustment, and the final interest payment. It includes every event in that scenario.

The adjustment charges AED 75.00 for three negative days and reduces unpaid interest by AED 1.20. Interest is calculated daily but enters the account balance only at the end of Day 6. Reducing unpaid interest changes that future payment; correcting interest already credited would change the account balance.

## Sources and limits

* [ISO 20022][iso], sections 3.4.2.15.6 and 3.4.2.15.7, page 62: date definitions for messages, not ledger policy.
* [Mambu dates][dates], "Booking date input under accounting closure": later booking can retain an earlier value date.
* [Mambu backdating][backdating] and [adjustments][adjustments]: reverses and reposts subsequent transactions, recalculating interest. This product behavior does not mandate difference adjustments.
* [CBUAE][cbuae], sections 5.1.1.38 to 5.1.1.40: covered institutions must correct their errors and refund resulting deductions or costs. These error correction provisions are outside this discussion's scope and do not establish how legitimate delayed transactions should be adjusted.

[iso]: https://www.iso20022.org/sites/default/files/documents/messages/mdr_part_2/ISO20022_MDRPart2_BankToCustomerCashManagement_2018_2019_v1_0.pdf
[dates]: https://docs.mambu.com/docs/booking-date-vs-value-date/#booking-date-input-under-accounting-closure
[backdating]: https://docs.mambu.com/docs/deposits-withdrawals-and-transfers/#backdating-deposits-and-withdrawals
[adjustments]: https://docs.mambu.com/docs/adjusting-transactions/#adjusting-transactions-in-bulk
[cbuae]: https://rulebook.centralbank.ae/en/rulebook/article-5-business-conduct
