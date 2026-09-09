# Settlements without a matching authorization

## The problem

In the [exercise](../exercise-statement.md), E6 is a settlement of AED 180.00 for ACC-001. It references Auth-Z, but the event stream contains no earlier authorization for Auth-Z.

Criterion 4 says to reject a settlement whose authorization cannot be found, but the exercise warns that some criteria are wrong.

The nonnegotiable rules leave this case undefined. Does SETTLEMENT request a payment or report one already settled?

## The alternatives

* **A payment request:** require a matching authorization before accepting the settlement. If the authorization is missing, report the problem and leave the balance unchanged. This follows criterion 4.
* **A payment already settled:** record the debit and report the missing authorization. The ledger must reflect the payment even though the local authorization record is missing.

## The approved decision

**Approved assumption: SETTLEMENT reports a legitimate payment already settled outside the ledger.**

When the authorization is missing:

1. Append the debit using the supplied `booking_date` and `value_date` in the supplied event order.
2. Keep the authorization ID supplied with the settlement and report that no matching authorization was found.
3. Do not create an authorization or hold to fill the gap.

For E6, debit AED 180.00 from ACC-001, with both dates on Day 4. Keep Auth-Z as the reference and report that its authorization was not found.

**The project rejects criterion 4 under this assumption.** Omitting a confirmed debit would overstate the ledger balance. The mandatory rules alone do not resolve this criterion.

This decision covers legitimate transactions. Corrections of system errors are outside its scope.

## Example

In the [fictional example](../examples/05-unmatched-settlement.md), a confirmed AED 180.00 settlement reduces the balance from AED 500.00 to AED 320.00. Omitting the debit would overstate the balance by AED 180.00.

## Sources and limits

[Stripe Issuing, Transactions, “Force capture”][stripe] describes payments recorded without a preceding authorization. Stripe records the transaction and deducts the amount from the Issuing balance. Purchases made offline on an airplane are one example.

Stripe shows that this behavior exists in a payment system. Its documentation does not establish that E6 is legitimate or require this exercise to accept E6. Our decision rests on the project assumption stated above.

[stripe]: https://docs.stripe.com/issuing/purchases/transactions#force-capture
