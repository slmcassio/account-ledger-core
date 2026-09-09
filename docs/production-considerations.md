# Production Considerations

These are limits and recommendations, not implemented production capabilities.

## Growth at 100 times the volume

With a drain after each transaction, the first avoidable latency cost to address is delivery lookup: every drain scans retained `:delivery-order`, including acknowledged IDs. Acknowledgement removes the envelope from `:deliveries`; order and acknowledgement IDs remain. Repeated drains can therefore accumulate quadratic work. Heap capacity may become the first hard limit; no benchmark establishes a universal bottleneck ranking.

Unbounded state includes Authorization's identity results, decision/snapshot history and delivery indexes; Ledger's journal and identity set; and Yield's events, fee/interest components, receipts, intent history and identity indexes. Ledger queries scan history; Yield repeatedly scans events/components. At 100 times the retained records, a linear scan examines approximately 100 times as many records. This is an algorithmic estimate; Clojure collections share structure.

**Cheapest change:** maintain a separate ordered index of pending deliveries, removing acknowledged entries from that work index while preserving financial/audit history. Next, add per-account journal indexes and dated checkpoints, invalidated by relevant late entries and preserving booking, value and observation boundaries. Indexes reduce scans; durable storage and a governed archive policy are still needed to bound live memory. Deleting old identities or settled components can break duplicate protection or later corrections.

## Authorization endings and intermediate states

These are the current model's complete lifecycle paths. An already approved hold can end through full explicit release, final capture, or a capture that exhausts its remainder. Decline is an initial refusal. A reversal corrects money and does not itself terminate or restore a hold.

| Real-world situation | Required model behavior |
|---|---|
| A purchase request exceeds available funds | Record a permanent decline without creating a hold, financial posting or snapshot. Later funds do not reactivate it. |
| A merchant cancels the remaining order | Full explicit release ends the hold and restores availability without crediting money. |
| A hotel's final bill is below its reservation | Debit the final bill, release the unused remainder and end the hold without a release credit. |
| A restaurant's confirmed final bill, including a tip, consumes or exceeds the reservation | Debit the whole confirmed amount and exhaust the hold, including with a non-final flag. Do not suppress confirmed money because availability is insufficient. |
| A merchant sends a late capture after cancellation, decline or an earlier exhausting capture | Record the debit and `inactive-authorization`; create no new hold. A never-recorded reference produces `missing-authorization`. |
| A captured charge is fully reversed | Append the principal reversal once. Preserve any residual hold; do not recreate a consumed or released hold. |

For an order split across shipments, a partial cancellation releases only that part; a non-final capture below the remaining hold consumes only the captured amount. Both are intermediate states. Invalid releases leave the existing state unchanged, and duplicate transaction IDs repeat no effect.

**Expiry is not implemented:** if a hotel never captures or releases a reservation, its residual hold remains active. Before production, define the contractual deadline and append an audited expiry release, restoring availability without a financial credit. Do not infer a deadline from the replay window. A genuine late capture still requires recording and reconciliation.

## Value dates for a UAE licensed bank

A late booking can change an earlier economic balance and its fee/interest entitlement while preserving a previously captured report. Operations must reconcile postings, recalculated entitlements and affected statements; retain both dates and the observation boundary; and control changes affecting closed periods. A backdated economic effect is not evidence that funds were operationally available earlier.

For applicable consumer deposit products, CBUAE Standards 2.1.2.4-5 cover interest/payment and fee disclosures; 2.1.2.9 covers periodic statements with movements, interest, fees and closing balance. These duties make value-date corrections a customer communication and statement issue, not just arithmetic. They do not establish this exercise's cutoff, rounding or fee policy. [CBUAE, pages 9-10](https://www.centralbank.ae/media/5crd24gm/cp-standards-pdf.pdf#page=10).

Bank-caused errors are outside this implementation's correction scope. CBUAE 5.1.1.38-40 requires correction for affected consumers, immediate refunds of deductions/costs caused by an error, and notification within ten complete business days of identifying it. The exercise's regular pending-interest schedule must not be assumed sufficient for that redress. [CBUAE, page 45](https://www.centralbank.ae/media/5crd24gm/cp-standards-pdf.pdf#page=46).

**One control before live use:** an independently approved backdated-adjustment gate. Record the authenticated initiator and approver, original reference, cause classification, both dates and a preview of affected balances, fees and interest; reconcile postings and statements before release. Route bank-caused errors to the applicable redress process and flag closed-period changes. This is a proposed implementation of control, not a claim that the cited clauses prescribe that exact workflow. Applicability depends on the institution, customer and product; durable recovery and a contractual calendar remain prerequisites.
