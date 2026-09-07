# Hold settlement, release, and expiration

## Scope

This study covers settlement, release, and expiration of holds within the in-memory exercise.

## Settlement rule

Under the [approved interpretation](03-unmatched-settlements-research.md), SETTLEMENT reports a legitimate payment already settled externally. Append its actual debit with the supplied dates, even if the local hold is missing, released, or expired. This supports criterion 3.

## Approved policy: settlement and release

Separately apply the effect on any matching active reservation:

* **Partial, non-final settlement:** reduce it by the settled portion and retain the remainder.
* **Final settlement:** consume the settled portion and release the remainder.
* **Release without settlement:** free the specified reserved amount without a ledger debit or credit.

Preserve the original authorization and decision records. Append reservation changes and derive the active amount from that history, following the [exercise's](../exercise-statement.md) immutability rule.

**Approved assumption:** Auth-A's AED 185.00 settlement is final. End its AED 200.00 reservation and make the unused AED 15.00 available without a ledger credit. Neither the smaller amount nor the absence of later settlements establishes finality.

## Approved policy: expiration

Generate no automatic expiration during Days 1 through 6 because the scenario supplies no policy or deadline. This does not mean holds never expire. Beyond the replay, duration, time reference, and update rules remain undefined.

Auth-B has a hold only if approved. Later balance corrections follow the [approved authorization policy](04-authorization-decisions-research.md).

## Example

The [fictional example](../examples/07-hold-lifecycle.md) compares final and non-final settlements of AED 15.00 against a hold of AED 20.00. Releasing the remaining AED 5.00 changes availability, not the ledger balance.

## Sources and limits

* [Mambu clearing][mambu-transactions], "Transaction advices": supports explicitly non-final clearing. Its [general guide][mambu-general], "Clearing and settling authorization holds," instead directs partial-clearing users to support. The descriptions differ.
* [Mambu expiration][mambu-holds], "Authorization Holds Configuration": configurable seven-day default for card holds, distinct from direct account holds.
* [Stripe capture][capture], `final_capture`: distinguishes final capture from retaining funds. This is a merchant capture API.
* [Stripe Issuing][issuing], "Authorization updates": documents capture after expiration. [Validity windows][expiry] vary by payment circumstances.
* [Mastercard rules][mastercard], 9 June 2026, section 2.10.1.1, printed/PDF pages 57 to 58: distinguishes partial and final clearing and advises releasing unused funds on final clearing.
* [Visa guide][visa], "Authorization reversals," printed/PDF page 3: releases unused estimated reservations, distinct from financial refunds.

These product and network documents provide precedents within their transaction and regional scope. Their deadlines and record models are not adopted here; they do not determine Auth-A's finality.

[mambu-transactions]: https://docs.mambu.com/docs/card-transaction-processing/#transaction-advices
[mambu-general]: https://docs.mambu.com/docs/card-payments-and-authorization-holds/#clearing-and-settling-authorization-holds
[mambu-holds]: https://docs.mambu.com/docs/authorization-holds/#authorization-holds-configuration
[capture]: https://docs.stripe.com/api/payment_intents/capture#capture_payment_intent-final_capture
[issuing]: https://docs.stripe.com/issuing/purchases/authorizations#authorization-updates
[mastercard]: https://www.mastercard.com/content/dam/mccom/shared/business/support/rules-pdfs/transaction-processing-rules.pdf
[visa]: https://usa.visa.com/content/dam/VCOM/regional/na/us/support-legal/documents/authorization-and-reversal-processing-best-practices-for-merchants.pdf
[expiry]: https://docs.stripe.com/payments/place-a-hold-on-a-payment-method#authorization-validity-windows
