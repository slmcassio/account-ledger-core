# Production Considerations

These are limits and recommendations, not implemented production capabilities.

## Growth at 100 times the volume

The first avoidable bottleneck is delivery lookup: each drain scans retained `:delivery-order`, including acknowledged IDs. Acknowledgement removes the envelope from `:deliveries`; order and acknowledgement IDs remain. Draining after every transaction can therefore accumulate quadratic work.

Unbounded state includes Authorization's identity results, decision/snapshot history and delivery indexes; Ledger's journal and identity set; and Yield's events, fee/interest components, receipts, intent history and identity indexes. Ledger queries scan history; Yield repeatedly scans events/components. At 100× retained records, a linear scan examines approximately 100× records. This is an algorithmic estimate, not a benchmark; Clojure collections share structure.

**Cheapest change:** maintain a separate ordered index of pending deliveries, removing acknowledged entries from that work index while preserving financial/audit history. Next, add per-account journal indexes and dated checkpoints that retain both booking and value boundaries. These improve scans but do not bound history. Durable storage and a governed archive policy would still be required; deleting old identities or settled components can break duplicate protection or corrections.

## Authorization endings and intermediate states

These behaviors are adopted project rules. Reservation changes affect availability; confirmed settlements move money.

| Situation | Model behavior |
|---|---|
| Purchase authorization lacks available funds | Record a permanent decline, with no hold, posting or new snapshot. Later funds do not reactivate it. |
| Merchant cancels the remaining purchase | Full explicit release ends the hold; restore availability without crediting money. |
| Final capture is below the reservation | Debit the capture, release the unused remainder and end the hold without a release credit. |
| Capture equals or exceeds the remaining hold | Debit the entire confirmed amount and exhaust the hold, including with a non-final flag. Insufficient availability does not suppress confirmed money. |
| Capture arrives after decline, release or exhaustion | Record the debit and `inactive-authorization`; create no new hold. An unknown reference instead produces `missing-authorization`. |
| A captured payment is reversed | Append the principal reversal. Do not recreate the consumed/released hold; preserve any residual hold. Corrections follow their own rules. |

Partial release and non-final capture below the remaining hold are intermediate states: free or consume only the specified amount, retaining the remainder. With no further event, an approved residual hold stays active. No expiry policy or timer is implemented; a production timeout needs a contractual deadline and an audited release. Invalid releases leave state unchanged; duplicate transaction IDs repeat no effect.

## Value dates for a UAE licensed bank

A late booking can change an earlier economic balance and its fee/interest entitlement while preserving the previously observed operational report. Booking date, value date and observation boundary must remain distinguishable in reconciliation and customer explanations.

For consumer deposit products, CBUAE Standards clauses 2.1.2.4–5 require disclosure of interest/payment circumstances and fees; clause 2.1.2.9 requires periodic statements including movements, interest, fees and closing balance. Applicability depends on the institution/customer/product. These provisions do not establish this exercise's cutoff, rounding or fee policy. [CBUAE Consumer Protection Standards, pages 9–10](https://www.centralbank.ae/media/5crd24gm/cp-standards-pdf.pdf#page=10).

**Control before live use:** require independently approved backdated adjustments with an immutable cause/reference, both dates and a preview of affected fees/interest; reconcile resulting postings and statements before release. Closed-period changes follow the bank's approved process. This is a recommended control, not a claim that the cited regulation prescribes this exact workflow. Durable recovery and a contractual calendar remain separate prerequisites.
