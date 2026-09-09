# BANK-SPEC implementation plan

> For agentic workers: execute this plan with built-in subagents and the applicable subagent-development workflow. User instructions in the launch prompt override skill defaults that would request intermediate approval, commit changes or expand scope. Checkboxes track verified work, not intentions.

**Goal:** Deliver the complete, tested, explainable six-hour exercise implementation.

**Architecture:** One in-memory Clojure application with Authorization, Ledger and Yield and Fees. Pure rules sit behind small application facades and in-memory adapters. A deterministic dispatcher and replay runner provide the integration.

**Tech stack:** Clojure/JVM, BigDecimal, clojure.test, clojure.spec.alpha, minimal pinned dependencies.

**Spec:** [SPEC.md](SPEC.md). Read it completely before coding.

## Global constraints

* Codename `BANK-SPEC`; branch `codex/bank-spec`; target worktree `/Users/slmcassio/Developer/account-ledger-core-bank-spec`.
* Source baseline `ba4201b51901b355c896cc94981bea14139c925e`; verify current state at execution time.
* Work only within required runtime isolation. Preserve the original checkout and unrelated changes.
* Do not stage, commit, push, create PRs, merge, rebase, publish or bypass protections.
* No Kafka, HTTP, runtime persistence, database, UI, generic retry framework or expanded banking scope.
* No intermediate user questions after launch. Record remaining choices in `agent-decisions.md`.
* English artifacts, Portuguese user communication. Actual America/Sao_Paulo timestamps in WORKLOG.
* Required tests must be real and pass; the one annotated challenge runs separately and really fails.

## Ownership and dependencies

The coordinator owns shared contracts, money, composition, replay, fixture/oracle, cross-module tests and all deliverable documentation. Each module owner implements its own application, domain, adapter and tests. Every dispatch must state exact paths, the shared contracts, expected output and that other agents are working concurrently. Do not revert their changes.

Use up to three module agents alongside the coordinator when runtime capacity allows. Use the configured execution model and supported native agents; do not change saved machine settings or create user-owned tasks. After implementation, reuse available capacity for a read-only correctness review.

When the runtime permits agents to share the isolated BANK-SPEC worktree, restrict every writer to its owned paths there. If a required worker role receives a separate managed worktree, provide the current contract files and integrate its changes as patches, without commits. A completed agent report is not evidence of integration: the coordinator tests the combined BANK-SPEC worktree. Never accidentally write implementation into the primary checkout.

Dependencies: T0 enables T1, T2 and T3 in parallel. T4 begins as soon as those owners expose their first working paths, without waiting for all module work. T5 follows the full integration. T6 maintains documentation throughout and performs final verification after the last fix.

### File map

* Coordinator: `deps.edn`; `src/account_ledger/{money,contracts,system,transaction,replay}.clj`; `test/account_ledger/{runner,fixtures,money_test,contracts_test}.clj`; `test/account_ledger/e2e/`; `test/account_ledger/integration/system_test.clj`; `test-design-challenge/`; documentation.
* Authorization owner: `src/account_ledger/authorization/{api,domain,memory}.clj`; `test/account_ledger/unit/authorization_test.clj`; `test/account_ledger/integration/authorization_test.clj`.
* Ledger owner: `src/account_ledger/ledger/{api,domain,memory}.clj`; `test/account_ledger/unit/ledger_test.clj`; `test/account_ledger/integration/ledger_test.clj`.
* Yield owner: `src/account_ledger/yield_fees/{api,domain,memory}.clj`; `test/account_ledger/unit/yield_fees_test.clj`; `test/account_ledger/integration/yield_fees_test.clj`.

Split a file further only when its responsibility becomes unclear. These are ownership boundaries, not an instruction to add empty layers. All module APIs match SPEC section 7.

## T0. Executable foundation and contract freeze

**Owner:** coordinator. **Target:** first 30 to 40 minutes. **Dependencies:** none.

* [x] Inspect Git status, source revision, instructions and existing worktrees. Establish the BANK-SPEC worktree and verify its Git common directory links to the intended repository. Do not overwrite a conflicting worktree.
* [x] Copy this package to `docs/implementation/bank-spec/` inside the execution worktree so the implementation remains traceable to its specification.
* [x] Verify the existing Java/Clojure runtime, pin the versions actually used, and provide a test runner that fails on test failures, load errors and zero-test discovery. Keep the challenge path out of default tests.
* [x] Implement `money/amount [currency decimal]`, `money/daily-interest [currency base]`, and `money/allocate-three [currency total]` against these concrete assertions. `amount` validates and normalizes exact representability; calculation rounding remains a separate operation.

```clojure
(is (= 0.01M (money/daily-interest :AED 12.50M)))
(is (= 0.00M (money/daily-interest :AED 12.49M)))
(is (= 0.001M (money/daily-interest :BHD 1.250M)))
(is (= 0.00M (money/daily-interest :AED -1.00M)))
(is (= [3.333M 3.333M 3.334M]
       (money/allocate-three :BHD 10.000M)))
```

* [x] Freeze command, committed-event, result, snapshot, calculation, settlement and query schemas from SPEC section 7 in `contracts.clj` and document their field meanings. Also freeze `pending-deliveries`, `ack-delivery!`, the `drain!` result, and Yield's `:submit-financial!`/`:flush-deliveries!` ports exactly as specified there. Preserve duplicate checking before full payload validation.
* [x] Write the supplied E1..E10 fixture without changing dates/order and independent expectations from SPEC section 6. Add logical-day control, not wall-clock sleeps.
* [x] Record the specification defaults, journal mapping, E10 representation, zero receipt and six-day/Day-7 boundary in `agent-decisions.md`.
* [x] Run `clojure -M:test unit` for the implemented foundation and confirm nonzero executed tests. Dispatch module tasks with these files and contracts. Record any corrected API detail once and notify all owners.

**Gate G0:** The project starts, real money tests pass, and all owners use the same contracts. No fake module success is required to reach this gate.

## T1. Authorization and confirmed delivery

**Owner:** Authorization agent. **Files:** Authorization ownership set above. **Consumes:** money helpers and shared contracts. **Produces:** `create`, `submit!`, `snapshot`, `history`, `pending-deliveries` and `ack-delivery!`, with SPEC section 7's exact technical port signatures and results.

* [x] First implement credit, hold and final settlement with unit and adapter tests. Expose this working path early so T4 can connect actual modules.
* [x] Verify an E1/E2/E3 sequence has financial balance 250.00, held amount 200.00, availability 50.00 and account counter 3. Then verify E4/E5 ends Auth-A with balance 465.00 and no hold.
* [x] Implement decline recording without a new snapshot; check the exact zero-availability boundary and one-minor-unit refusal. Demonstrate that later credit does not change an already recorded decline.
* [x] Add confirmed unmatched settlement, partial settlement, explicit release, reversal principal and E10's one-snapshot installment breakdown. Preserve supplied IDs/dates and no parent overcredit.
* [x] Make identity/base/outcome recording indivisible. Keep side effects out of retryable pure state-update functions. Test a controlled race with a barrier or equivalent deterministic coordination; use no arbitrary sleeps.
* [x] Check duplicate IDs before payload validation, including changed payload and a declined request. Duplicates cause no new snapshot or pending delivery.
* [x] Implement source-version validation for fee and interest commands. An unrecorded stale proposal returns the documented result and applies nothing. A recalculated proposal keeps its ID; a recorded payment duplicate never requests recalculation.
* [x] Keep pending deliveries with the recorded outcome. Expose original committed events for redelivery and acknowledge destinations separately, without repeating operational effects.
* [x] Run the module's unit and integration namespaces with the coordinator's runner. Report executed test counts, unresolved issues and exact owned file changes.

**Gate G1:** A1 through A4 have meaningful passing tests, and T4 can submit actual financial movements and inspect immutable snapshots.

## T2. Ledger and explicit accounting views

**Owner:** Ledger agent. **Files:** Ledger ownership set above. **Consumes:** committed-movement schema, money helpers, SPEC journal mapping. **Produces:** `create`, `post!`, `balance`, `journal`.

* [x] First expose posting and balance queries for ordinary credit/debit. A movement with ID E1 creates one balanced entry and a customer balance of 1200.00.
* [x] Store both journal sides together. Validate positive posting amounts, equal debit/credit totals and same currency. Avoid confusing customer CREDIT/DEBIT with universal accounting debit/credit meaning.
* [x] Deduplicate by original movement ID and preserve all recorded entries. Reposting E1 must not change the journal or balance.
* [x] Implement value-date and booking-cutoff filters plus an optional local journal position. Verify the E7 historical Day 2 principal result -370.00 before fees and E9, and preserve a query reproducing the earlier view.
* [x] Implement E10 as one atomic entry with three separately identified posting pairs and exactly 10.000 of customer credits. Do not add a fourth parent credit.
* [x] Prove holds, declines and releases cannot create a financial journal. Verify queries leave the history unchanged.
* [x] Run unit and adapter integration tests and provide T4 a passing real posting/query path early.

**Gate G2:** L1 passes, balances identify their temporal boundaries, and journal totals reconcile to each approved financial movement.

## T3. Yield and Fees, from simple accrual to corrections

**Owner:** Yield agent. **Files:** Yield ownership set above. **Consumes:** approved-event schema, money helpers, injected `:submit-financial!` and `:flush-deliveries!` functions with SPEC section 7's signatures/results. **Produces:** `create`, `receive!`, `calculate!`, `settle!`, `report`.

* [x] First implement event ingestion, a complete source prefix, one positive daily accrual and a financial payment through Authorization's port. Expose this path to T4 before implementing all corrections.
* [x] Reconstruct balances from opening state and events. Respect both dates and exclude holds/pending interest. A known higher counter with a gap is not complete input.
* [x] Implement fee bases and H+1 dates. Review periods chronologically and count prior adjustments even when their booking dates exclude them from an assessment base. Preserve other periods' dated fee effects.
* [x] Implement daily interest and incremental corrections against rounded targets. Cover two AED 465.00 days yielding 0.38 and an AED base changing 12.49 to 12.50 requiring +0.01.
* [x] Reconcile financial corrections only with confirmed Authorization movements. Refresh and recalculate the next versioned proposal after each accepted fee; do not reuse a stale source number across successive movements.
* [x] Implement component selection and positive/negative/zero settlement. Link each component once, include paid adjustments in future comparisons, and keep current-day adjustments outside that day's payment.
* [x] Verify an eligible 0.10 ordinary component plus a -0.30 correction settles as -0.20, including from a zero account balance. This fixture tests the adopted signed policy; it is not a new product feature.
* [x] Implement late receipt and reversal corrections under the chosen schedule. E10's +0.004 stays pending; E9's compensation waits for cutoff 6 on Day 7.
* [x] Run unit and integration tests for Y1 through Y4. Report component identities, settlement links and remaining integration concerns rather than only reporting totals.

**Gate G3:** Yield uses actual approved events, produces reproducible components, and never settles a component on an unrecorded stale payment.

## T4. Integrate early and finish the required replay

**Owner:** coordinator. **Files:** system, transaction, replay, fixtures and integration/e2e tests. **Dependencies:** G0 and the first paths from T1..T3, not their completion.

* [x] Wire actual module handles and adapters. Transaction submits only to Authorization. The separate system driver drains delivery and queries reports.
* [x] By approximately 90 minutes, demonstrate a credit reaching Authorization, Ledger and Yield, followed by a simple interest calculation/payment through Authorization. Use actual modules and assert both balance and journal effects.
* [x] Add hold/final-settlement integration immediately as its owner exposes it. Detect schema and ownership mismatches now; refreeze contracts centrally if a small correction is necessary.
* [x] Implement the exact checkpoint sequence from SPEC section 6. Capture immutable daily reports before later events. E10 remains after E9 despite its Day 5 booking.
* [x] Implement `replay/run` to return `{:system system :daily-reports reports :summary summary}`. Give summary the explicit keys used below and amounts in their currency scales. `continue-day-seven` receives that system and returns separate continuation results.

```clojure
(let [{:keys [summary daily-reports]} (replay/run)]
  (is (= 12 (count daily-reports)))
  (is (= 210.57M (get-in summary ["ACC-001" :financial-balance])))
  (is (= 0.57M (get-in summary ["ACC-001" :interest-paid])))
  (is (= -0.46M (get-in summary ["ACC-001" :pending-interest])))
  (is (= 10.000M (get-in summary ["ACC-002" :financial-balance])))
  (is (= 0.004M (get-in summary ["ACC-002" :pending-interest]))))
```

* [x] Check intermediate states and each fee/interest component as well as the final sums. Independently recompute the oracle without production calculation functions.
* [x] Add the separate Day 7 continuation: current ACC-001 285.57 after refunds, prior-month net pending adjustments 0.11, and new-month ordinary Day 6 accrual 0.08. Do not rewrite the earlier Day 6 report.
* [x] Run `clojure -M:test e2e` and `clojure -M:demo`; resolve integration problems with the owning agents while preserving their boundaries.

**Gate G4:** All external exercise events pass through all appropriate real modules, all six-day reports exist, and the independent oracle passes. Target this before the final two hours.

## T5. Contract regressions and the honest design challenge

**Owner:** coordinator with module owners. **Files:** integration/e2e tests, example fixtures, `test-design-challenge/`.

* [x] Prove X1 by failing one delivery after Authorization recording and retrying the recorded message. Assert one operational effect, one journal and eventual delivery readiness.
* [x] Prove a source-version mismatch applies no payment, then a refreshed calculation with the same ID succeeds. Verify a decline and another account's event do not invalidate this account. This is the documented contract, not a generic retry system.
* [x] Execute examples 04 through 08 as independent fixtures. In particular, current example 04 pays 1.72, closes at 226.72 and leaves -1.20 pending; example 08 has original 0.04 plus two separate 0.02 adjustments. Preserve their explicit scenario boundaries.
* [x] Trace all eight acceptance criteria. Reject incorrect claims with calculations and clarify evaluation boundaries. Do not treat the old research counterfactual as the current replay oracle.
* [x] Create the separate annotated challenge asserting a payload conflict on a known ID. Keep the actual duplicate policy covered by a passing normal test. Verify the challenge's failure is the intended assertion, with zero runtime errors.
* [x] Audit every domain function against the coverage matrix; add only missing meaningful examples or boundary tests. Check that test discovery includes all intended namespaces.

**Gate G5:** M1..D1 from the SPEC are covered, all normal tests pass, and the isolated challenge genuinely fails exactly as documented.

## T6. Review, documentation and final evidence

**Owner:** coordinator; independent reviewer is read-only. **Files:** README, `agent-decisions.md`, `docs/api.md`, existing deliverables, `docs/deliverables/VERIFICATION.md`, necessary related examples and test documentation.

* [x] Maintain concise WORKLOG entries at completed milestones using real America/Sao_Paulo recording times. Do not rewrite historical entries or claim work not yet verified.
* [x] Document actual API signatures, schemas, result meanings, delivery boundaries and examples. Align AMBIGUITIES, NUMBERS, REJECTED and architecture with the adopted specification choices and any explained refinements.
* [x] Request one independent review of the integrated code, relevant tests and numerical reasoning. Ask specifically about duplicated money, cutoff violations, mutation of history, component settlement, unrecorded attempts, false test success and scope creep.
* [x] Fix substantiated findings using the responsible owner. Rerun affected tests, then the full suite after the final code changes. Do not prolong review with hypothetical features or style-only rewrites.
* [x] Execute the README's normal commands from the worktree root. Capture real test counts, errors, exit codes and demo results. Repeat the complete suite once in a fresh JVM. Run the deliberate challenge separately.
* [x] Check whitespace, changed local Markdown links and final diff. Verify the primary checkout was not modified by this execution. Record final branch/worktree and uncommitted state.
* [x] Fill VERIFICATION with requirement-to-test mapping, executed commands and results, review outcome, expected deliberate failure and real limitations. No placeholder can substitute for executable coverage.
* [x] Deliver a self-contained Portuguese handoff with worktree link, commands, actual results, financial boundaries and key decisions. State honestly if any required work remains.

**Gate G6:** Every completion criterion in SPEC section 9 has observed evidence.

## Time management and continuity

Use the six-hour budget as an execution target, not as fabricated worklog duration. G0 should be early; the real integrated path should exist around 90 minutes; the complete replay should exist by approximately hour four. Reserve the last hour for demonstrated defects, final verification and documentation.

If a milestone slips, prioritize the first missing required integration, reuse the existing agents, and remove optional polish. Do not cut business behavior, skip required tests, relax assertions or mark unfinished work complete. Do not wait for the user to choose routine details.

Keep this checklist and `agent-decisions.md` current at meaningful milestones so context compaction can resume from actual state. After a restart or context loss, inspect the worktree, last verified gate and current test results before continuing. Do not create a new implementation or repeat already completed work blindly.
