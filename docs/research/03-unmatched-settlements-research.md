# Settlements without a matching authorization

## The problem

In the [exercise](../exercise-statement.md), E6 is a settlement of AED 180.00 for ACC-001. It references Auth-Z, but the event stream contains no earlier authorization for Auth-Z.

Acceptance criterion 4 says to reject a settlement when its authorization cannot be found. Our [business rules](../business-rules.md) originally repeated this instruction in the row named BR06. The exercise warns that some acceptance criteria are wrong, so the rejection instruction needs to be examined.

The nonnegotiable rules do not specify how to handle this case. The key question is what SETTLEMENT represents: a request to make a payment, or notice of a payment that has already settled.

## The alternatives

* **A payment request:** require a matching authorization before accepting the settlement. If the authorization is missing, report the problem and leave the balance unchanged. This follows criterion 4.
* **A payment already settled:** record the debit and report the missing authorization. The ledger must reflect the payment even though the local authorization record is missing.

## The approved decision

We adopt the second interpretation. **For this project, SETTLEMENT reports a legitimate payment that has already settled outside the ledger.** This is an explicit project assumption because the exercise does not define SETTLEMENT that precisely.

When the authorization is missing:

1. Append the debit using the supplied `booking_date` and `value_date`. Preserve the event order and existing records.
2. Keep the authorization ID supplied with the settlement and report that no matching authorization was found.
3. Do not invent an authorization or a hold to fill the gap. Reporting the missing authorization does not cancel the debit.

For E6, debit AED 180.00 from ACC-001, with both dates on Day 4. Keep Auth-Z as the reference and report that its authorization was not found.

**We reject criterion 4 under this assumption:** the payment has already settled, so omitting its debit would leave the ledger balance too high. This conclusion follows from the meaning we chose for SETTLEMENT.

This decision covers legitimate transactions. Corrections of system errors are outside its scope.

## Example

The [worked example](../examples/05-unmatched-settlement.md) lists every event and shows the resulting balances in tables. It also shows what the balance would be if the settlement were rejected.

## Sources and limits

[Stripe Issuing, Transactions, “Force capture”][stripe] describes payments recorded without a preceding authorization. Stripe records the transaction and deducts the amount from the Issuing balance. Purchases made offline on an airplane are one example.

Stripe shows that this behavior exists in a payment system. Its documentation does not establish that E6 is legitimate or require this exercise to accept E6. Our decision rests on the project assumption stated above.

[stripe]: https://docs.stripe.com/issuing/purchases/transactions#force-capture
