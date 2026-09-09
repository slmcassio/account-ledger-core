# Account Ledger Core

One Clojure/JVM application in memory, with Authorization, Ledger, and Yield and Fees. It implements the supplied exercise with the [adopted rules](docs/exercise-inputs/business-rules-corrected.md) and the [BANK-SPEC schedule](docs/implementation/bank-spec/SPEC.md). No HTTP, database, UI or runtime persistence is required.

## Running the Suite

Run from the repository root with Java and the Clojure CLI. Verified environment: Java 25 and Clojure CLI 1.12.5.1654. `deps.edn` pins Clojure 1.12.5, spec.alpha 0.5.238 and core.specs.alpha 0.4.74. Dependency download is needed only if they are absent from the local Maven cache.

```sh
clojure -M:test
clojure -M:test unit
clojure -M:test integration
clojure -M:test e2e
clojure -M:demo
```

The default suite discovers every normal test namespace. Failures, errors, invalid groups and zero discovered tests produce a nonzero exit. Unit tests exercise pure rules, model shapes and architecture boundaries; integration tests exercise ports, storage and composed modules with controlled delivery/concurrency; end-to-end tests execute E1 through E10 and the five separate documented examples. The numerical oracle uses integer minor units independently of production calculation functions.

The required deliberate design challenge runs separately:

```sh
clojure -M:design-challenge
```

It must exit 1 with exactly one assertion failure and no runtime errors. It demands a payload-conflict response for a known transaction ID, exposing that the adopted duplicate policy trusts producer identity uniqueness and does not inspect changed content. The normal suite verifies the approved duplicate response. See [test explanation](tests/README.md) and [executed verification evidence](docs/deliverables/VERIFICATION.md).

## Module structure

Authorization, Ledger, and Yield and Fees each use the same layout. For example:

```text
src/account_ledger/authorization/       test/account_ledger/authorization/
  logic/core.clj                         logic/core_test.clj
  ports/api_server.clj                   ports/api_server_test.clj
  ports/api_client.clj                   ports/api_client_test.clj
  db/memory.clj                          db/memory_test.clj
  model/models.clj                       model/models_test.clj
```

`logic` contains pure calculations and state transitions over immutable data. It calls no API and reads no storage. `ports/api_server.clj` exposes the module's operations; `ports/api_client.clj` calls its injected recipients. `db/memory.clj` owns the opaque mutable state and atomic updates. `model/models.clj` declares the internal map shapes. Yield also separates its pure payment transitions into `logic/transitions.clj`. Ledger has no outgoing application dependency, so its client namespace documents that fact without an unused operation.

Shared arithmetic, identifiers, validation and report projection live in `shared/logic`; shared contract schemas live in `shared/model`. Application composition and replay remain in `system.clj`, `transaction.clj` and `replay.clj`. See the [architecture boundaries](docs/architecture.md#executable-boundaries-and-tradeoffs) and [test organization](tests/README.md#normal-test-organization).

## Reading the Output

The demo prints twelve immutable operational reports, one for each account/day. Financial balance reflects recorded money; active holds affect availability only. Fees show their reference, booking and value days; positive fee amounts are charges and negative amounts refunds. Interest paid has entered funds, while pending components have not. Occurrences include the missing Auth-Z reference; it does not suppress the confirmed debit. Counter values identify account snapshots, not elapsed days or Ledger journal positions.

At the Day 6 operational close, ACC-001 has AED **210.57**, paid interest **0.57**, no holds, and pending interest **-0.46**. Three fees total 75.00; Auth-B remains declined. ACC-002 has BHD **10.000**, zero paid interest, and **0.004** pending. E10 is one transaction containing installments 3.333, 3.333 and 3.334.

The separately labeled Day 7 continuation returns 75.00 in fees, producing AED **285.57**. Its pending interest is **0.19**: 0.11 in prior-month adjustments and 0.08 for ordinary Day 6 interest in the new month. BHD pending interest becomes **0.008**, with no further financial movement. These are later results, not replacements for Day 6's captured report. Day 7 refunds have booking 7 and cannot enter the Day 7 job's booking-cutoff-6 base.

Use [API documentation](docs/api.md) for historical accounting queries with explicit economic day, booking cutoff and local journal position. Queries never trigger delivery or corrections. [Architecture](docs/architecture.md), [implementation decisions](agent-decisions.md) and [numbers](docs/deliverables/NUMBERS.md) explain the small design for a live defense.

Yield saves each fee or interest command locally before submitting it. An uncertain response retains the exact command and blocks another financial operation for that account until resolved. Confirmation records the original assessment or paid component links, including when it arrives by event delivery. Reports expose `:financial-intents` and `:pending-financial-commands`; unresolved commands make the report incomplete. These records remain in memory and do not survive process exit.

## Documentation

### Exercise Reference

* [Exercise statement](docs/exercise-inputs/exercise-statement.md)

### Required Deliverables

* [Constants and numerical decisions](docs/deliverables/NUMBERS.md)
* [Ambiguities and decisions](docs/deliverables/AMBIGUITIES.md)
* [Rejected criteria and approaches](docs/deliverables/REJECTED.md)
* [Worklog](docs/deliverables/WORKLOG.md)
* [Annotated failing test requirement](tests/README.md)
* [Part 2: Architecture summary](docs/architecture-summary.md)
* [Detailed architecture reference](docs/architecture.md)
* [Tradeoffs](docs/trade-offs.md)
* [Production considerations](docs/production-considerations.md)
* [Architecture, tradeoffs and production PDF](docs/deliverables/architecture-tradeoffs-production.pdf)

### Working Notes

Research includes proposals and source review. [AMBIGUITIES](docs/deliverables/AMBIGUITIES.md) records adopted decisions and remaining questions.

* [Corrected business rules](docs/exercise-inputs/business-rules-corrected.md)
* [01: Monetary rounding](docs/research/01-rounding-research.md)
* [02: Booking dates, value dates, and corrections](docs/research/02-booking-and-value-dates-research.md)
* [03: Settlements without a matching authorization](docs/research/03-unmatched-settlements-research.md)
* [04: Authorization and ledger balances](docs/research/04-authorization-decisions-research.md)
* [05: Hold lifecycle](docs/research/05-hold-lifecycle-research.md)
* [06: Daily closing and calculation timing](docs/research/06-daily-closing-research.md)
* [07: Daily overdraft fee assessment](docs/research/07-overdraft-fees-research.md)
* [08: Principal reversals and financial corrections](docs/research/08-reversals-research.md)
* [09: Daily interest precision and rounding](docs/research/09-daily-interest-research.md)
* [10: Interest capitalization](docs/research/10-interest-capitalization-research.md)
* [11: Installment allocation](docs/research/11-installments-research.md)
* [12: Overdraft fee currency](docs/research/12-fee-currency-research.md)
* [13: Acceptance criteria assessment](docs/research/13-acceptance-criteria-research.md)
