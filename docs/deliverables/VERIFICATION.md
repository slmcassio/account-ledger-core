# BANK-SPEC Verification

## Final verification before local commits

The user authorized focused local commits for the pending demo formatting and documentation updates. Fresh execution again passed **117 tests / 1,197 assertions**, zero failures/errors, exit 0: [normal output](evidence/bank-spec-final-commits/normal.txt). The separate [design challenge](evidence/bank-spec-final-commits/design-challenge.txt) produced exactly **one deliberate assertion failure**, zero errors, exit 1, for the documented known-ID payload-collision limitation.

The [demo](evidence/bank-spec-final-commits/demo.txt) exited 0 and exactly matched the earlier executed capture after inserting one blank line before each of its fourteen day/account headers. [Command timestamps](evidence/bank-spec-final-commits/commands.json) retain actual run boundaries in America/Sao_Paulo. No numerical or domain behavior changed. No separate formatter/linter task is configured; Git whitespace checks and local Markdown link/anchor checks cover the document changes.

Independent final review found no actionable finding or inappropriate evidence content. The four-page PDF still matches the SHA256 of the rendered and inspected consolidation artifact. [Final artifact checks](evidence/bank-spec-final-commits/artifact-checks.json) record link, whitespace, PDF and preservation checks. Local review reports remain unchanged and excluded. Earlier evidence below remains its original execution record.

## Architecture document consolidation

The user approved one concise [architecture document](../architecture.md) as the architecture source of the combined PDF. Removed `architecture-summary.md` after consolidating essential content and updating README, the Part 2 supplement and the current decision record. The detailed architecture's prose decreased from 2,488 to 505 words, counting headings and excluding Mermaid, HTML anchors and link destinations. Financial rules, formulas and detailed payload contracts remain in the linked API, CONTRACTS, NUMBERS and AMBIGUITIES documents.

The consolidated text preserves module ownership, pure/effectful boundaries, atomic identity/version recording, decline semantics, exact saved-command recovery, receipt reconciliation, pending Yield versus principal behavior, delivery completeness and temporal/report boundaries. The counter 10 to 11 example still requires receiving the missing event before recalculation. The original Mermaid C3 is byte-identical, including interaction labels 1, 2, 3, 10 and 11. The PDF prints those five relationships directly from the diagram labels. Historical WORKLOG anchors remain valid without rewriting earlier entries. An independent reviewer found no essential information loss and checked eight outgoing links and eighteen incoming anchor references.

The regenerated PDF has **four pages**, rendered and visually inspected. All **11,393 normalized body characters**, including the diagram's textual relationships, match the three current Markdown sources in order. Body text remains within page margins and separate from footers: [PDF/source checks](evidence/bank-spec-architecture-consolidation/pdf-checks.json), [extracted text](evidence/bank-spec-architecture-consolidation/pdf-text.txt). The prior Part 2 coverage map below remains applicable: architecture on page 1, tradeoffs and cuts on page 2, scale/lifecycle on page 3, value dates and the proposed control on page 4.

[Final links, whitespace and preservation checks](evidence/bank-spec-architecture-consolidation/artifact-checks.json) cover this documentation change. Source code, tests, original exercise, earlier evidence and local review files are unchanged from the start of consolidation. No Clojure tests were rerun for this documentation-only edit; the actual 117-test / 1,197-assertion run below remains the most recent normal suite result. No commit or remote operation was performed.

## Architecture deliverable audit and demo spacing

The combined architecture PDF now contains **four pages**, within the user's limit. The [supplemental Part 2 requirements](../exercise-inputs/architecture-requirements.md) transcribe the assessment screenshot supplied in this conversation. The original exercise statement remains unchanged, including its historical note that only the Part 2 title was available when copied.

| Required topic | Coverage in the final PDF |
|---|---|
| Architecture and tradeoffs | Page 1: ownership, ports, pure logic, effect ordering and temporal boundaries. Page 2: concrete choices, benefits and costs. |
| Append-only growth at 100 times volume | Page 3: the repeated delivery scan under the stated workload, unbounded state in each module, an ordered pending index as the cheapest first change, and the remaining memory limits. This is algorithmic reasoning, not a benchmark result. |
| Every authorization ending, real-world scenario and mandated behavior | Page 3: refusal, release, final/fully consuming capture, inactive/missing references and reversal effects; partial operations remain intermediate. Automatic expiry is explicitly unimplemented, with a proposed audited release and no invented deadline. |
| Value-dated entries in a UAE licensed bank and one control | Page 4: economic, recording and observation boundaries; reconciliation, statements, closed periods and consumer error correction; an independently approved adjustment gate. Verified official CBUAE citations are scoped to applicable products and do not prescribe the exercise's numerical policies. |
| Every documented cut, reason and deferred production risk | Page 2: eleven explicit cut/reason/risk rows, plus concurrency and payload-conflict limits in the choice table. No replay narrative or repeated rule catalogue. |

The audit corrected inaccurate attribution of broker exclusion and numerical constants, expanded the cut/risk mapping and concrete lifecycle scenarios, and distinguished legitimate late inputs from bank-caused error remediation. The requested wording is now “One application”; the implementation-history paragraph remains removed. The performance discussion identifies its workload assumptions and does not claim measured throughput or a universal bottleneck ranking.

Current Markdown descriptions are aligned with the implementation: reports read live module state without an atomic cross-module snapshot guarantee; the serial-job, local atomic-recording and source-validation choices are adopted rather than open proposals. A pending Yield command blocks new Yield fees/payments for that account, while principal credits/debits remain accepted. README, detailed architecture, API, contracts, current ambiguity decisions, corrected business rules, example 08 and agent decisions reflect these distinctions. Historical research, earlier WORKLOG entries and original verification records remain historical evidence, not rewritten current claims.

Independent reviewers checked coverage, implementation consistency and links. Their final checks found no remaining substantive discrepancy within the supplied exercise scope. One checked 230 local link occurrences and 155 anchors across the twelve source Markdown files before this evidence entry. A second compared 107 extracted source paragraphs, headings and table cells with the PDF. The lead additionally compared all **10,530 normalized body characters in order** against the three Markdown sources, rendered and inspected all four pages, and checked page margins and footer separation: [PDF checks](evidence/bank-spec-document-audit/pdf-checks.json), [extracted text](evidence/bank-spec-document-audit/pdf-text.txt). Normalization removes presentation syntax, whitespace, bullet glyphs, PDF footers and page numbers, not substantive wording.

The demo readability change was completed by an agent in parallel: one blank line before each `Day`/account header. The actual before/after runs have exactly **14 added blank lines**; every other line remains identical. No calculation or state-transition code changed, and no formatting-only test was added.

| Executed check | Actual result | Evidence |
|---|---|---|
| `clojure -M:test` | **117 tests / 1,197 assertions**, zero failures/errors, exit 0 | [normal suite](evidence/bank-spec-document-audit/normal.txt) |
| `clojure -M:demo`, before and after | Exit 0 both; all 14 report headers preceded by a blank line; exact expected formatting transformation | [before](evidence/bank-spec-document-audit/demo-before.txt), [after](evidence/bank-spec-document-audit/demo.txt), [comparison](evidence/bank-spec-document-audit/demo-spacing.json) |
| `clojure -M:design-challenge`, separate | **1 test / 1 assertion, 1 deliberate failure**, zero errors, exit 1: known ID returns duplicate rather than detecting changed content | [challenge](evidence/bank-spec-document-audit/design-challenge.txt) |

[Command timestamps](evidence/bank-spec-document-audit/commands.json), [final artifact checks](evidence/bank-spec-document-audit/artifact-checks.json) and [preservation evidence](evidence/bank-spec-document-audit/preservation.json) record the actual checks. The original exercise, historical worklog, primary checkout and all four local review reports are preserved. This follow-up makes no new commit or remote change. The earlier refactor and mutation results below remain their original execution records.

## Module structure refactor

Refactored from `8530deb` into per-module `logic`, `ports`, `db` and `model` directories, with mirrored tests. Public operations retain their argument and result maps under the new `ports.api-server` namespaces. All **98 original test names remain discovered exactly once**, and every previous assertion was retained. Pure logic contains no API or storage calls; an architecture regression checks local dependencies and known effectful operations. The static guard is not a general proof of purity for arbitrary Java interop or dynamically constructed code.

| Executed check | Actual result | Evidence |
|---|---|---|
| Complete normal suite, three fresh JVMs | **117 tests / 1,197 assertions**, zero failures/errors each, exit 0 | [run 1](evidence/bank-spec-module-structure/normal-1.txt), [run 2](evidence/bank-spec-module-structure/normal-2.txt), [run 3](evidence/bank-spec-module-structure/normal-3.txt) |
| Unit group | 51 tests / 550 assertions, zero failures/errors | [unit](evidence/bank-spec-module-structure/unit.txt) |
| Integration group | 58 tests / 543 assertions, zero failures/errors | [integration](evidence/bank-spec-module-structure/integration.txt) |
| End-to-end group | 8 tests / 104 assertions, zero failures/errors | [end-to-end](evidence/bank-spec-module-structure/e2e.txt) |
| Full immutable replay comparison | All six-day reports, summary and separate Day 7 continuation equal the pre-refactor capture; every original test retained | [comparison](evidence/bank-spec-module-structure/preservation-replay.txt) |
| Demo | Exact original output after removing only the historical recording header | [demo](evidence/bank-spec-module-structure/demo.txt) |
| Separate design challenge | **1 test / 1 assertion, 1 deliberate failure, zero errors, exit 1** | [challenge](evidence/bank-spec-module-structure/design-challenge.txt) |

The replay comparison is an additional refactor preservation check, not the financial oracle. The independent integer oracle and manually specified business outcomes remain in the normal suite. The group discovery check confirms that unit/integration/e2e partition all discovered namespaces.

The four controlled review mutations still fail against the current full suite: negative-interest Ledger rejection produces **11** assertion failures; ignored confirmed duplicate responses **13**; bypassed BHD availability **14**; fabricated occurrences **7**. Each has zero errors and exits 1. [Command arguments, timestamps and summaries](evidence/bank-spec-module-structure/commands.json) record every execution. The [current mutation probe](evidence/bank-spec-module-structure/review-mutations.clj) uses the new namespaces:

```sh
clojure -Sdeps '{:paths ["src" "test"]}' -M docs/deliverables/evidence/bank-spec-module-structure/review-mutations.clj negative-ledger
```

Replace the last argument with `duplicate-response`, `bhd-availability` or `fabricated-occurrences`. These probes intentionally fail and remain separate from the normal suite and the exercise challenge.

Independent review found one refactor regression before delivery: registering new model specs under public payload keywords incidentally narrowed Clojure `s/keys` validation when a model namespace was loaded. Model-local specs and explicit field predicates remove that interference. Three regressions preserve public metadata while model tests still reject malformed internal records. The isolated red run had 8 assertion failures and no errors; the final model group had 7 tests / 76 assertions, zero failures/errors. The independent reviewer reproduced the original public command before and after loading all three corrected models and observed `:recorded` both times, with no remaining actionable finding.

The API example, document links, three-page PDF rendering and preservation checks are recorded in [artifact checks](evidence/bank-spec-module-structure/artifact-checks.txt). The primary checkout is clean on `main` at `8530deb` at final verification; it was not modified by this refactor. The original exercise, earlier WORKLOG entries and all four local review reports are preserved. `docs/reviews` remains excluded from the requested commit.

## Regression protection follow-up

The user's follow-up required closure of all five concerns in the test-quality review and a signed commit including WORKLOG. This verification covers the combined financial-intent fixes and the completed regression protection. `docs/reviews` remains unchanged and excluded from the commit.

The final normal command `clojure -M:test` passed **98 tests / 1,033 assertions**, with **zero failures and errors**, exit **0**: [output](evidence/bank-spec-regression-gaps/normal.txt). Each review mutation below ran the same complete suite in its own JVM and now causes assertion failures. These are controlled verification probes outside normal test discovery, with no changes to production source files.

| Review mutation | Assertions failed | Errors | Exit | Evidence |
|---|---:|---:|---:|---|
| Ledger rejects negative-interest payments | 11 | 0 | 1 | [negative Ledger delivery](evidence/bank-spec-regression-gaps/negative-ledger.txt) |
| Yield ignores confirmed duplicate financial responses | 12 | 0 | 1 | [duplicate response](evidence/bank-spec-regression-gaps/duplicate-response.txt) |
| BHD authorization bypasses available funds | 14 | 0 | 1 | [BHD availability](evidence/bank-spec-regression-gaps/bhd-availability.txt) |
| Every report fabricates occurrence content | 7 | 0 | 1 | [occurrence content](evidence/bank-spec-regression-gaps/fabricated-occurrences.txt) |

[Command arguments, timestamps and results](evidence/bank-spec-regression-gaps/commands.json) and the [exact mutation probe](evidence/bank-spec-regression-gaps/review-mutations.clj) record the historical checks at commit `8530deb`. That archived probe uses the original namespaces. On that commit, from the worktree root, run:

```sh
clojure -Sdeps '{:paths ["src" "test"]}' -M docs/deliverables/evidence/bank-spec-regression-gaps/review-mutations.clj negative-ledger
```

Replace the final argument with `duplicate-response`, `bhd-availability` or `fabricated-occurrences` for the other probes. Each intentionally modified run exits 1; the normal implementation exits 0. These probes do not replace or hide the exercise's separate design challenge.

Permanent regression coverage:

* P1: `a-different-settlement-after-response-loss-cannot-pay-the-same-interest-again` uses the public System API and all three modules. A payment confirmed before response loss is followed directly by a different settlement, without an intervening accrual or explicit drain. It asserts one 0.04 payment, operational/accounting balance 100.04, unique original component links, no pending delivery and immutable original receipt on retry.
* Delivery: `negative-settlement-debits-zero-balance-and-counts-paid-adjustments` asserts the one balanced 0.20 debit, both module balances at -0.20, completed delivery and recorded/completed historical follow-up. The earlier lost-negative-response regression remains.
* Duplicate response: `a-direct-confirmed-duplicate-uses-the-original-financial-receipt` asserts the original receipt, one payment/counter advance, both balances, cleared pending state and completed delivery.
* BHD: `hold-boundary-and-decline-recording` now preserves the AED case and covers BHD approval at exact availability, refusal 0.001 above it, and refusal of a 0.001 hold from zero funds. Refusal preserves funds, holds, counter and delivery state while retaining the decline in history.
* Occurrences: the full replay asserts none through Day 3 for ACC-001, exactly the missing `Auth-Z` occurrence from Day 4 through the separately captured Day 7, and none for ACC-002. Example 05 asserts the exact missing `Auth-Missing` occurrence.

Before strengthening the relevant tests, the BHD mutation passed its focused Authorization tests (14 / 184) and the occurrence mutation passed the end-to-end tests (8 / 99). Both now fail. The earlier financial-intent corrections already exposed the negative-delivery and duplicate-response mutations; this follow-up strengthens their delivery, journal and result assertions as specified. No further production behavior change was needed beyond the financial-intent correction below.

An independent reviewer inspected all five completed regression areas and found no remaining actionable issue within this scope. The independent numerical oracle remains part of the passing suite. `clojure -M:demo` exited 0 with output exactly equal to the original twelve six-day reports and two Day 7 reports ([output](evidence/bank-spec-regression-gaps/demo.txt)). `clojure -M:design-challenge` again produced exactly **one deliberate failed assertion, zero errors, exit 1** ([output](evidence/bank-spec-regression-gaps/design-challenge.txt)). [Artifact checks](evidence/bank-spec-regression-gaps/artifact-checks.txt) retain the document and preservation checks. This is targeted regression verification, not an exhaustive mutation-coverage claim.

## Financial intent correction

Initial correction verified on 09 September 2026 after commit `ad24b3fcdb878bf6dc41c7cb1106afaf80ad16d2`, in `/Users/slmcassio/Developer/account-ledger-core-bank-spec` on `codex/bank-spec`. At that check the changes were unstaged and no commit or remote operation had been performed. The original checkout was clean on `main` at `6980b14e0eaf8f02bba1e7486eca4dbd0119d4df`. The original exercise and local `docs/reviews` reports were preserved. The subsequent regression follow-up and its commit authorization are documented above.

| Executed command | Tests | Assertions | Failures / errors | Exit | Evidence |
|---|---:|---:|---|---:|---|
| `clojure -M:test` | 97 | 964 | 0 / 0 | 0 | [normal suite](evidence/bank-spec-financial-intents/normal.txt) |
| `clojure -M:design-challenge` | 1 | 1 | **1 deliberate / 0** | **1** | [separate challenge](evidence/bank-spec-financial-intents/design-challenge.txt) |
| `clojure -M:demo` | n/a | n/a | Exact original output | 0 | [replay](evidence/bank-spec-financial-intents/demo.txt) |

[Command timestamps](evidence/bank-spec-financial-intents/commands.json) retain actual execution and recording times in America/Sao_Paulo. The demo output equals the original captured output exactly, excluding its recording header: twelve reports for Days 1 through 6 and two separately labeled Day 7 reports. The independent integer oracle still passes as part of the normal suite; expected results were not derived from the production calculation.

The two reviewed duplication defects are corrected:

* Yield saves the entire command and calculation links before submission. The unknown outcome retains the exact pending map. A later settlement cannot select the same components while that operation is unresolved. Delivered confirmation records the original receipt independently of a subsequent caller's settlement ID.
* The shared boundary rejects a missing or inconsistent fee assessment before recording money. Its confirmed source counter is copied from the accepted command. The analogous incomplete-interest contract also rejects commands without coherent receipt ID, period and dates.

[Financial intent integration tests](../../test/account_ledger/yield_fees/ports/api_client_test.clj) cover saved state visible inside the outgoing port, loss before and after Authorization recording, immutable retries after new accruals and changed request dates, confirmed duplicate results, different settlement IDs, zero settlement blocked by an unknown payment, definitive invalid/stale rejection, negative interest and reversal refunds. [System integration tests](../../test/account_ledger/integration/system_test.clj) verify that malformed financial commands change no snapshot, journal, delivery or calculation report, and that accepted records prevent another charge/payment. Existing [Yield tests](../../test/account_ledger/yield_fees/ports/api_server_test.clj) additionally check an unknown fee is retried before replacing its calculation, even when a later input changes the target to zero.

Regression evidence before the relevant fixes:

* The independent financial intent test author's original six tests executed 113 assertions with 38 failures and zero errors against the committed implementation.
* The fee contract unit run executed 34 tests / 336 assertions with 50 failures and zero errors before its validation change, then passed. The subsequent interest contract unit run executed 37 tests / 385 assertions with 32 failures and zero errors before its change, then passed.
* The composed regression for omitted nested fee source metadata exposed five failures and two null-source errors; copying the confirmed command source fixed it. The composed incomplete-interest regression demonstrated 100.08 paid from a 100.00 principal with only 0.04 accrued, producing 16 failures and zero errors before contract correction.
* One older lost-response test expected zero pending after a correction because receipt recognition was delayed until settlement retry. It now expects -0.04 immediately after delivered confirmation, preserving the original 0.04 payment and the separate unpaid correction.

An independent effect-order audit found Authorization already records outcomes, snapshots and delivery envelopes before dispatch; Ledger records the balanced journal before acknowledgement. An independent final reviewer ran 52 integration tests / 480 assertions with zero failures/errors and checked the final code and API documentation. Its source-metadata and interest-contract findings were corrected with the regressions above; its final documentation note about equal interest booking/value days was also applied. No remaining finding was identified within the serial, unique-ID scope. [Artifact checks](evidence/bank-spec-financial-intents/artifact-checks.txt) record whitespace, local links and preservation checks.

This initial correction added 19 tests and 295 assertions to the previous 78 / 669 suite. BHD authorization boundaries and occurrence-report details were outside that initial step; the regression follow-up above subsequently closed both gaps.

Intent history and pending commands are in-memory state. They do not survive process exit. Jobs remain serial, identity uniqueness remains the producer's responsibility, and the existing annotated challenge still exposes the intentional duplicate-payload limitation. No financial constants, capitalization rules or replay boundaries changed.

## Original implementation verification record

The sections below preserve the initial implementation evidence and describe that earlier workspace state. Current correction results and the superseding review findings are above and in [WORKLOG](WORKLOG.md).

Originally verified on 09 September 2026 in America/Sao_Paulo. Individual command recording times and exact output are retained in [command evidence](evidence/bank-spec/commands.json). These are execution observations, not estimates of work duration.

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
| M1: exact money/scales/ties | [money tests](../../test/account_ledger/shared/logic/money_test.clj): `exact-inputs-and-scales`, `daily-rounding-boundaries`; [contract tests](../../test/account_ledger/shared/logic/contracts_test.clj): `command-boundaries`. |
| M2: round daily, preserve E10 | Money `three-installments-conserve-money`; Yield integration `positive-accrual-reaches-financial-port-and-settles-original-components`; Ledger installment tests; replay integer oracle. |
| A1: hold boundary and immutable decline | [Authorization unit](../../test/account_ledger/authorization/logic/core_test.clj): `hold-boundary-and-decline-recording`, `known-identity-precedes-every-payload-check`; example 06 and complete replay preserve declined B. |
| A2: final/partial/release/unmatched | Authorization `principal-hold-and-final-settlement`, `partial-settlement-and-explicit-release`, `release-can-free-a-partial-amount-without-posting-money`, `confirmed-settlement-debits-without-sufficient-funds-or-live-hold`; examples 05 and 07. |
| A3: duplicate before validation | Authorization `known-identity-precedes-every-payload-check`, `source-version-is-account-specific-and-unrecorded-attempts-retain-identity`; [Ledger integration](../../test/account_ledger/ledger/ports/api_server_test.clj) `duplicate-id-precedes-payload-validation`; Yield original-payment confirmation tests. |
| A4: atomic identity/base/outcome | [Authorization integration](../../test/account_ledger/authorization/ports/api_server_test.clj): `competing-holds-cannot-spend-the-same-availability`, `concurrent-reused-identity-records-only-one-entire-outcome`, `source-movement-between-calculation-and-submission-requires-new-calculation`, `competing-financial-proposals-cannot-both-record-from-one-source`; account-specific source unit regression includes decline/other account. |
| L1: balanced immutable temporal journal | [Ledger unit](../../test/account_ledger/ledger/logic/core_test.clj) covers all posting/domain functions; Ledger integration `historical-views-retain-economic-booking-and-arrival-boundaries`, `installment-arrival-has-one-global-position-and-no-extra-parent-credit`, `concurrent-redelivery-atomically-records-one-whole-entry`, `invalid-last-installment-cannot-leave-the-earlier-pairs-recorded`. |
| Y1: cutoff and complete prefix | [Yield unit](../../test/account_ledger/yield_fees/logic/core_test.clj) `contiguous-prefix-does-not-confuse-highest-observed-with-complete`, `closing-base-uses-both-dates-and-opening-state`; [Yield integration](../../test/account_ledger/yield_fees/ports/api_server_test.clj) `gap-blocks-calculation-until-hold-only-event-arrives`, `stale-payment-settles-nothing-and-retry-retains-identity`; example 08 records B during calculation. |
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
