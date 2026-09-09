# Architecture

BANK-SPEC is one Clojure application running in memory. Three modules own separate immutable histories and communicate through explicit function ports. The design prioritizes explainable financial behavior and deterministic verification. The [detailed architecture](architecture.md) retains implementation and calculation notes.

## Responsibilities and boundaries

* **Authorization** owns available funds, holds, operational snapshots and decisions. It records identity, account version and outcome atomically before making an event available for delivery.
* **Ledger** owns balanced journal entries and accounting balances. Each accepted financial movement posts once by transaction ID. Booking date, value date and journal position make historical queries reproducible.
* **Yield and Fees** owns dated assessments, interest components, financial intents and settlement receipts. It calculates from approved events, saves the complete intended command, then requests its financial effect through Authorization.

Each domain has `logic`, `ports`, `db` and `model` directories, mirrored in tests. Logic contains pure functions only. `ports/api_server.clj` coordinates public operations; `ports/api_client.clj` owns outgoing calls. `db/memory.clj` owns local atomic state replacement. `model/models.clj` describes internal data without changing public validation. Ledger has no outgoing dependency. Shared arithmetic and validation also remain pure.

## Recording, delivery and recovery

A technical entry point submits transactions to Authorization. The deterministic dispatcher sends saved envelopes to Yield and, for financial movements, Ledger. It acknowledges each destination independently. Neither Authorization nor Yield reads Ledger balances.

Local locks protect identity, version and state changes; no cross-module call runs under those locks. Delivery can lag, so operational balances and accounting balances have different observation boundaries. Reports expose known pending work and never trigger recovery as a side effect of reading.

Yield saves the exact payment or fee command before submission. A lost response leaves that command pending for an identical retry. A confirmed result, confirmed duplicate or delivered event reconciles its original assessment or component receipt before later settlement selection. A different settlement cannot pay the same component again. A definite rejection permits recalculation; uncertainty does not. This recovery works only while the process and its memory remain alive.

## Time and immutable corrections

Economic value date, recording date and observed event/journal position remain distinct. Late inputs append linked differences; they never rewrite earlier payments, decisions or captured reports. Pending interest is separate from posted money until settlement. Exact decimal arithmetic and explicit currency rounding implement the adopted numerical policy.

The executable checks cover module boundaries, failure recovery and the supplied replay against an independent numerical oracle. [Tradeoffs](trade-offs.md) explains the deliberate limitations; [production considerations](production-considerations.md) identifies the controls and structural changes needed before live use.
