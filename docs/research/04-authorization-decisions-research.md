# Authorization and ledger balances

## Scope

This study separates financial entries, authorization holds, and authorization decisions within the exercise's in-memory scope.

## Authorization rule

The ledger balance reflects recorded financial entries. Available balance is `ledger balance - active holds`.

The [exercise](../exercise-inputs/exercise-statement.md) permits approval only when `ledger balance - active holds - requested hold >= 0`. An approved hold reduces available balance without debiting the ledger. A declined request creates no hold or financial debit.

Criterion 5 is conditional: if Auth-B is approved, its hold reduces available balance without changing the ledger balance. The criterion does not require approval.

## Approved responsibilities

* Authorization guards available balance through operational snapshots containing financial state and active holds. Every request uses the latest snapshot. An approved hold creates a new snapshot; a decline records only its decision and supplied ID.
* All financial effects pass through Authorization's snapshot path. Authorization sends committed financial data to Ledger and never consumes Ledger data. Confirmed payments are recorded even with missing authorization or negative availability; only new hold requests face the approval check.
* Ledger records financial entries and supplies accounting reports to other consumers. The balance equation above is arithmetic, not a query from Authorization to Ledger.
* The decision history explains what was approved or declined using the information available at that point.

This approved snapshot ownership replaces the earlier approach in which Ledger supplied Authorization's balance. The domains remain modules within one in-memory system.

The Transaction entry module communicates only with Authorization. Authorization forwards approved transactions to Yield and Fees after recording the transaction and snapshot. The test suite or script replays the event order and inspects daily outputs separately from that entry module.

Process requests in the supplied event order. Use the latest operational snapshot, including financial corrections already recorded. Unpaid interest affects neither ledger nor available balance until capitalization. A decline preserves funds, holds, the current snapshot and its counter, and creates no financial posting.

Transactions arrive with their own IDs. Skip recorded IDs, including declined requests, before repeating calculations or inspecting amount or content, even if content differs. Financial events and hold changes create snapshots. Each account's first snapshot counter is 1; each later snapshot uses its calculation base's counter plus one. Declines do not consume a counter position or invalidate Yield's source version.

ID uniqueness checking, unchanged-base validation, and recording the outcome are indivisible: save only the decision for a decline, or the transaction and new snapshot otherwise. If the base advances before recording, reread it and reevaluate the uncommitted request with its supplied ID under the [snapshot and retry decision](../deliverables/AMBIGUITIES.md#snapshot-recording-and-retries). A recorded decline remains unchanged on redelivery after a later balance correction.

Authorization records first and sends financial data to Ledger afterward. Ledger prevents duplicate journal entries using the same ID. This [separate delivery](../deliverables/AMBIGUITIES.md#ledger-delivery) may be asynchronous, so accounting reports can temporarily lag the operational snapshot. Delivery and reporting readiness details remain open. Yield reconstructs interest bases from Authorization's approved transactions and submits payments under the [source-view validation decision](../deliverables/AMBIGUITIES.md#yield-calculation-and-payment), without querying either module's balance.

## Approved policy: later balance corrections

Updating the balance and reconsidering a decision are separate actions.

**Approved decision:** Preserve the original decision without automatic reevaluation after a balance correction. More funds do not activate a declined request. Evaluate a new explicit request against the updated balance and active holds.

This policy keeps each decision tied to the information available when the request was processed. It is a project choice. Immutability alone does not determine whether to append a corrective decision.

## Example

The [fictional example](../examples/06-authorization-decisions.md) applies a settled debit of AED 10.00 and a settled credit of AED 50.00. A hold of AED 40.00 is approved. A later request for AED 10.00 is declined. The ledger balance and active holds are each AED 40.00, leaving AED 0.00 available.

The continuation reverses the debit and raises the ledger balance to AED 50.00. Active holds stay at AED 40.00. A new request can use the AED 10.00 now available.

## Sources and limits

[Stripe Issuing, Authorizations][stripe], the initial decision flow and "Authorization updates," illustrates checks when a request is processed and states that a declined request places no hold.

Stripe's Issuing balance definitions and authorization updates describe its product, not this ledger's accounting model. The source does not determine this project's snapshot ownership, concurrency policy, or whether decisions should be reevaluated after backdated entries or financial reversals.

[stripe]: https://docs.stripe.com/issuing/purchases/authorizations#authorization-updates
