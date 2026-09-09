# BANK-SPEC Verification

Verified on 09 September 2026 in America/Sao_Paulo. Individual command recording times and exact output are retained in [command evidence](evidence/bank-spec/commands.json). These are execution observations, not estimates of work duration.

## Workspace and scope

* Baseline and unchanged branch HEAD: `ba4201b51901b355c896cc94981bea14139c925e`.
* Branch: `codex/bank-spec`.
* Worktree: `/Users/slmcassio/Developer/account-ledger-core-bank-spec`.
* Primary checkout: `/Users/slmcassio/Developer/account-ledger-core`, still clean on `main` at the same baseline.
* Changes are unstaged and uncommitted. No push, PR, merge, rebase, publication, signing operation or Touch ID was performed.
* Runtime observed: Java 25 build 25+37-LTS-3491; Clojure CLI 1.12.5.1654. Dependencies pinned to Clojure 1.12.5, spec.alpha 0.5.238 and core.specs.alpha 0.4.74, already available locally.

[Preservation evidence](evidence/bank-spec/preservation.json) confirms the primary status/HEAD, empty index, unchanged original exercise statement and unchanged historical WORKLOG content.

## Executed commands

All normal commands run from the worktree root. The second full run started another JVM after the final code changes.

| Command | Tests | Assertions | Failures / errors | Exit | Evidence |
|---|---:|---:|---|---:|---|
| `clojure -M:test` | 78 | 669 | 0 / 0 | 0 | [full suite](evidence/bank-spec/normal.txt) |
| `clojure -M:test`, fresh JVM repeat | 78 | 669 | 0 / 0 | 0 | [repeat](evidence/bank-spec/normal-fresh-jvm.txt) |
| `clojure -M:test unit` | 30 | 272 | 0 / 0 | 0 | [unit](evidence/bank-spec/unit.txt) |
| `clojure -M:test integration` | 40 | 298 | 0 / 0 | 0 | [integration](evidence/bank-spec/integration.txt) |
| `clojure -M:test e2e` | 8 | 99 | 0 / 0 | 0 | [end to end](evidence/bank-spec/e2e.txt) |
| `clojure -M:demo` | n/a | n/a | Successful replay | 0 | [all reports](evidence/bank-spec/demo.txt) |
| `clojure -M:design-challenge` | 1 | 1 | **1 deliberate / 0** | **1** | [real failure](evidence/bank-spec/design-challenge.txt) |

The demo output contains exactly twelve six-day account reports and two separately labeled Day 7 reports; all report complete internal delivery. Its actual financial values agree with the independent oracle. The complete [documented API example](../api.md#composition-and-replay) also executed successfully and returned financial balance 250.10, paid interest 0.10 and zero pending, with complete delivery: [evidence](evidence/bank-spec/api-example.txt).

Test discovery found 11 namespaces and exactly 78 loaded test vars, matching 78 top-level test declarations: [discovery evidence](evidence/bank-spec/test-discovery.txt). An unknown group exited 2 ([output](evidence/bank-spec/invalid-group.txt)); running discovery from an empty temporary directory exited 2 for zero discovered namespaces ([output](evidence/bank-spec/zero-discovery.txt)). The latter also emitted Clojure's external-path deprecation warnings; those were retained. Load exceptions are caught by the runner and exit 2. The normal runner cannot turn failed assertions or execution errors into success.

## Requirement to test map

Tests below refer to actual `deftest` names. Module integration tests use real application adapters; the system tests and examples wire all three modules. Controlled port wrappers are used only to create the specified failure or version-change boundary.

| Requirement | Executable evidence |
|---|---|
| M1: exact money/scales/ties | [money tests](../../test/account_ledger/money_test.clj): `exact-inputs-and-scales`, `daily-rounding-boundaries`; [contract tests](../../test/account_ledger/contracts_test.clj): `command-boundaries`. |
| M2: round daily, preserve E10 | Money `three-installments-conserve-money`; Yield integration `positive-accrual-reaches-financial-port-and-settles-original-components`; Ledger installment tests; replay integer oracle. |
| A1: hold boundary and immutable decline | [Authorization unit](../../test/account_ledger/unit/authorization_test.clj): `hold-boundary-and-decline-recording`, `known-identity-precedes-every-payload-check`; example 06 and complete replay preserve declined B. |
| A2: final/partial/release/unmatched | Authorization `principal-hold-and-final-settlement`, `partial-settlement-and-explicit-release`, `release-can-free-a-partial-amount-without-posting-money`, `confirmed-settlement-debits-without-sufficient-funds-or-live-hold`; examples 05 and 07. |
| A3: duplicate before validation | Authorization `known-identity-precedes-every-payload-check`, `source-version-is-account-specific-and-unrecorded-attempts-retain-identity`; [Ledger integration](../../test/account_ledger/integration/ledger_test.clj) `duplicate-id-precedes-payload-validation`; Yield original-payment confirmation tests. |
| A4: atomic identity/base/outcome | [Authorization integration](../../test/account_ledger/integration/authorization_test.clj): `competing-holds-cannot-spend-the-same-availability`, `concurrent-reused-identity-records-only-one-entire-outcome`, `source-movement-between-calculation-and-submission-requires-new-calculation`, `competing-financial-proposals-cannot-both-record-from-one-source`; account-specific source unit regression includes decline/other account. |
| L1: balanced immutable temporal journal | [Ledger unit](../../test/account_ledger/unit/ledger_test.clj) covers all posting/domain functions; Ledger integration `historical-views-retain-economic-booking-and-arrival-boundaries`, `installment-arrival-has-one-global-position-and-no-extra-parent-credit`, `concurrent-redelivery-atomically-records-one-whole-entry`, `invalid-last-installment-cannot-leave-the-earlier-pairs-recorded`. |
| Y1: cutoff and complete prefix | [Yield unit](../../test/account_ledger/unit/yield_fees_test.clj) `contiguous-prefix-does-not-confuse-highest-observed-with-complete`, `closing-base-uses-both-dates-and-opening-state`; [Yield integration](../../test/account_ledger/integration/yield_fees_test.clj) `gap-blocks-calculation-until-hold-only-event-arrives`, `stale-payment-settles-nothing-and-retry-retains-identity`; example 08 records B during calculation. |
| Y2: fees/cross-period bases/corrections | Yield `fee-base-excludes-only-its-own-included-components`, `fee-target-is-negative-only-and-respects-explicit-zero-configuration`, `ordinary-fees-use-next-day-dates-and-refreshed-source-counters`, `historical-fees-refresh-and-recompute-each-next-proposal`, `reversal-refunds-use-original-charge-dates-and-preserve-other-period-fees`, stale-fee regression. |
| Y3: pending/sign/once/paid corrections | Yield `pending-interest-is-neither-spendable-nor-an-input-to-next-day-interest`, `negative-settlement-debits-zero-balance-and-counts-paid-adjustments`, `zero-settlement-links-zero-components-without-financial-effect`, `zero-cancellation-settles-positive-and-negative-components-once`, original-link/lost-response regressions. |
| Y4: principal/reversal dates/decisions | Authorization reversal tests; Yield reversal-refund test; [replay tests](../../test/account_ledger/e2e/replay_test.clj) `separate-day-seven-and-accounting-boundaries`; example 06 and Auth-B state at both replay boundaries. |
| X1: recorded delivery failure | [System integration](../../test/account_ledger/integration/system_test.clj) `failed-delivery-retries-recorded-movement` loses the Ledger acknowledgement after posting, then proves one operational effect/journal and eventual readiness. Yield also tests lost fee/payment responses. |
| X2: real E1 through E10 and reproducibility | Replay `complete-six-day-replay`, `independent-financial-oracle`, `separate-day-seven-and-accounting-boundaries`; compares every intermediate financial/hold/counter/pending state, fee dates/components, payment, E10 and a fresh run. |
| X3: five separate examples | [Examples tests](../../test/account_ledger/e2e/examples_test.clj): `example-04-backdated-adjustment`, `example-05-unmatched-settlement`, `example-06-authorization-decisions`, `example-07-final-partial-and-release`, `example-08-ordered-historical-views`. |
| D1: all eight criteria | [REJECTED conclusions and calculations](REJECTED.md#bank-spec-acceptance-criteria), backed by replay, Ledger temporal, hold/settlement, installment and daily-rounding tests above. |

Additional public boundary regressions reject forged event effects, generated command fields, mismatched precision/currency, invalid dates/references, unknown accounts and misuse of historical-interest options. `receiving-a-hold-cannot-invent-financial-interest` verifies the real application remains at 0.04 rather than incorrectly earning 0.08 from a forged hold effect.

## Domain function audit

* `money`: all three functions have direct precision, rounding, allocation and invalid-boundary tests.
* Authorization `initial-state` and `submit`: exercised through pure unit transitions and real APIs. Private approval/decline/settlement/release/reversal/recording helpers are covered by their distinct lifecycle outcomes, invalid inputs, snapshots and delivery assertions. Controlled races verify the adapter's indivisible operation.
* Ledger `initial-state`, `journal-entry`, `record-movement`, `account-journal` and `account-balance`: covered directly or through ordinary, invalid, installment, duplicate and temporal tests. Private normalization/pair/balance checks are exercised by malformed effects, amounts, currency and installment cases.
* Yield `input-view`, `total`, `closing-base`, `fee-base`, `fee-target`, `assessment`, `eligible-components` and `report`: covered by prefix, dated basis, cross-period fees, bounded interest, signed settlement, paid adjustments and immutable reports. Application coordination uses accepted, duplicate, stale, zero and delivery-failure paths.
* Composition/replay: real credit/payment, hold/final settlement, failed delivery, pure incomplete report, all external replay events and separately bounded continuation execute through the public modules. The Transaction adapter calls only Authorization.

No percentage substitutes for this audit. No test is skipped, no required namespace is hidden, and no expected financial result is generated by a production calculation function. The oracle derives monetary expectations in integer minor units; independent reviewers additionally recomputed the frozen values using Decimal arithmetic.

## Independent review and corrections

A read-only reviewer inspected all source and relevant normal/challenge tests, including the integrated numeric and temporal behavior. Its final fresh JVM run also observed 78 tests and 669 assertions, zero failures/errors, exit 0. Its independent Decimal calculation passed 12 assertions. An earlier independent module follow-up also passed 29 arithmetic assertions and audited discovery.

Concrete issues were reproduced before correction:

* Lost Authorization responses could erase confirmed fee metadata or let a recalculated zero settlement absorb the wrong components. Yield now recovers accepted fee assessments and original payment receipts from received committed events. Original dates, amounts and component links win over a new selection. The two new regressions first produced respectively five and four failed assertions with no runtime errors, then passed.
* Unrelated credit metadata could incorrectly consume a principal's reversal eligibility. Only actual reversal events now count; its regression first failed three assertions, then passed.
* Yield could accept a nonfinancial event carrying a forged financial effect. The shared committed-event boundary now enforces operation/effect/amount consistency; direct and real-module regressions first failed eight assertions, then passed.
* The narrow example 08 option could bypass ordinary fees if supplied on a daily request. Schema validation now requires both options together in historical mode; regressions first failed, then passed.

The stricter new-command boundary also initially rejected legitimate Ledger deliveries. The combined suite detected that integration regression. Ledger now validates the command projection and preserves/validates generated event metadata separately. The final reviewer reran its concrete reproductions after the fixes; no actionable finding remained. No balances, assertions or supplied exercise inputs were relaxed to obtain success.

## Financial boundaries and deliberate limitation

Day 6 operational close: ACC-001 AED 210.57, paid 0.57, pending -0.46, no holds, counter 12. Three fees total 75.00. ACC-002 BHD 10.000, paid zero, pending 0.004, counter 1. E10 has one journal with three balanced pairs.

Separate Day 7: ACC-001 AED 285.57, counter 15, after 75.00 refunds booked Day 7/value Day 6. Prior-month pending adjustments net 0.11; ordinary Day 6 earns 0.08 in the new month. Its total pending is 0.19. ACC-002 has 0.008 pending and unchanged financial balance/counter. The Day 7 booking-cutoff-6 job still uses AED 210.57 for Day 6's interest base, excluding its own newly booked refunds. Captured Day 6 reports remain unchanged.

The separate challenge intentionally demands a payload-conflict response for a reused ID. The actual approved duplicate policy does not inspect payload content. Its one real failed assertion reveals reliance on producer identity uniqueness; it is not skipped or inverted into a passing test.

## Actual limits

Runtime state is in memory and disappears on exit. Jobs are serial; there is no distributed scheduler, durable delivery, restart recovery, HTTP, database or UI. Internal-prefix completeness cannot establish that every future external event has arrived. The fixture selects no real calendar, tax policy or automatic hold expiration beyond the replay. BHD's zero fee is the explicit adopted exception. Example 08's bounded earlier views apply only to pending interest corrections and cannot submit partial-view fees. These limits are documented scope choices, not missing required replay behavior.

Final whitespace, local-link and preservation checks are recorded in [artifact checks](evidence/bank-spec/artifact-checks.txt). All implementation work remains available for inspection without any Git publication action.
