# Hold settlement, release, and expiration

## Scope

This report separates recording a settled payment from changing an authorization hold. It examines final settlement, partial settlement, release, and expiration within the exercise's in-memory scope.

## Rule and open question

The [exercise](../exercise-statement.md) defines available balance as ledger balance minus active holds. Only an approved authorization creates a hold. Existing event records must remain unchanged.

The [approved settlement interpretation](03-unmatched-settlements-research.md) treats SETTLEMENT as a legitimate payment already settled outside the ledger. Its debit must be recorded even when the local authorization is missing.

The scenario supplies Auth-A for AED 200.00 and a later settlement of AED 185.00. It does not identify that settlement as final or non-final. Neither the smaller amount nor the absence of another settlement establishes finality. The exercise also supplies no expiration policy or deadline.

## Analysis

Record the actual settlement debit once, using its supplied dates. Separately determine the effect on any matching active hold:

* **Partial, non-final settlement:** reduce the reservation by the settled portion and retain the remainder.
* **Final settlement:** consume the settled portion and release any remaining reservation.
* **Release without settlement:** free the specified reserved amount without a ledger debit or credit.

A settlement can be smaller than the hold and still be final. For Auth-A, a final settlement would debit AED 185.00 and end the AED 200.00 reservation. The unused AED 15.00 would become available without a ledger credit. A non-final settlement would debit the same amount and leave AED 15.00 reserved.

Ending a hold means ending its effect on available balance. Preserve the original authorization and decision records, append the information explaining the reservation change, and derive the remaining active amount from that history.

An expired or released hold does not prevent recording a legitimate payment already settled externally. Any problem matching the reservation is separate from the financial debit.

**Approved decision:** treat Auth-A's settlement as final. This is an explicit simplifying assumption for the scenario, not a conclusion established by the event stream.

**Approved expiration decision:** generate no automatic expiration during Days 1 through 6. This avoids choosing an unsupported deadline for the replay; it does not mean holds never expire. A general expiration policy would need an agreed duration or deadline, a time reference, and rules for relevant updates. No behavior beyond the replay window is determined.

Auth-B has a hold only if its request is approved. The absence of settlement alone does not establish an active reservation. The [approved authorization policy](04-authorization-decisions-research.md) preserves earlier decisions after balance corrections and requires a new explicit request for another evaluation.

Criterion 3 is supported by the approved meaning of SETTLEMENT: E5 reports a legitimate payment already settled externally, so its AED 185.00 debit must be recorded. This does not depend on an active local hold and does not determine finality.

## Example

The [complete fictional example](../examples/07-hold-lifecycle.md) starts with no funds or holds, adds a credit of AED 50.00, approves a hold of AED 20.00, and compares final and non-final settlements of AED 15.00. A separate continuation releases the remaining AED 5.00 without changing the ledger balance.

## Sources and limits

* [Mambu Transaction Processing][mambu-transactions], "Transaction advices," documents releasing the matched hold after successful processing and retaining the remainder when a clearing is explicitly non-final. This is a Cards API behavior. Mambu's [general guide][mambu-general], "Clearing and settling authorization holds," instead describes final clearing and directs partial-clearing users to support. These pages differ; support for a particular integration must be confirmed.
* [Mambu Authorization Holds][mambu-holds], "Authorization Holds Configuration" and "Hourly authorization holds cron job," documents a configurable seven-day default, category and individual overrides, and expiration that removes the hold's effect on availability. Card authorization holds are distinct from transaction holds placed directly on an account. These product settings do not establish a deadline for this exercise.
* [Stripe Capture API, `final_capture`][capture] distinguishes final capture from retaining funds for additional captures where supported. It describes a merchant requesting capture, not this exercise's receipt of an already settled payment.
* [Stripe Issuing authorizations][issuing], "Authorization updates," explicitly includes capture after expiration. The source distinguishes releasing a reservation from later financial processing; its balance definitions and record model are not adopted here.
* [Mastercard Transaction Processing Rules][mastercard], 9 June 2026, section 2.10.1.1, printed/PDF pages 57 to 58, distinguishes partial and final clearing and advises release of unused funds on final clearing. Sections 2.8 and 2.11 cover retention limits and authorization reversals. These network rules have transaction and regional scope; they do not determine this scenario's finality or expiration.
* [Visa authorization and reversal guide][visa], "Authorization reversals," printed/PDF page 3, discusses releasing unused amounts for estimated authorizations. This is a reservation reversal, distinct from reversing a recorded financial debit. Its network deadlines are not adopted here.
* [Stripe authorization validity windows][expiry] vary with payment circumstances. The exercise lacks the inputs needed to select a window.

[mambu-transactions]: https://docs.mambu.com/docs/card-transaction-processing/#transaction-advices
[mambu-general]: https://docs.mambu.com/docs/card-payments-and-authorization-holds/#clearing-and-settling-authorization-holds
[mambu-holds]: https://docs.mambu.com/docs/authorization-holds/#authorization-holds-configuration
[capture]: https://docs.stripe.com/api/payment_intents/capture#capture_payment_intent-final_capture
[issuing]: https://docs.stripe.com/issuing/purchases/authorizations#authorization-updates
[mastercard]: https://www.mastercard.com/content/dam/mccom/shared/business/support/rules-pdfs/transaction-processing-rules.pdf
[visa]: https://usa.visa.com/content/dam/VCOM/regional/na/us/support-legal/documents/authorization-and-reversal-processing-best-practices-for-merchants.pdf
[expiry]: https://docs.stripe.com/payments/place-a-hold-on-a-payment-method#authorization-validity-windows
