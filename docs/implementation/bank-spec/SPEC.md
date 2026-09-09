# BANK-SPEC: implementation specification

Prepared on 2026-09-09 for a separate execution session. This document specifies proposed implementation choices; it does not claim that executable code exists or that tests have passed. Launching the companion [START.md](START.md) authorizes implementation of this package.

## 1. Objective and execution authority

Deliver a small, explainable Clojure account ledger application in memory, implementing the supplied exercise and adopted project decisions. The intended execution budget is six hours from launch. Success means working code, independently checked financial results, a passing normal test suite, and complete instructions for running and defending the solution.

* User codename: `BANK-SPEC`; branch: `codex/bank-spec`.
* Repository: `/Users/slmcassio/Developer/account-ledger-core`.
* Verified source baseline: `ba4201b51901b355c896cc94981bea14139c925e`, on local and remote `main` at preparation time.
* Intended worktree: `/Users/slmcassio/Developer/account-ledger-core-bank-spec`.
* Requested execution model: GPT-6 Astra, Ultra. This records the user's selection, not a guarantee of runtime availability or completion.
* Implement only after the user launches the execution prompt. Create or reuse an appropriate isolated worktree then. Preserve the original checkout and unrelated worktrees.
* Do not stage, commit, push, create a PR, merge, rebase, publish, change repository visibility, or bypass protections. No Touch ID interaction should be needed for this work.
* Proceed autonomously after launch. Resolve remaining implementation details using the smallest consistent design, recording decisions in root `agent-decisions.md`. Do not wait for routine approvals or reopen settled business policies.
* An actual permission or environment barrier does not authorize bypassing protections. Complete unaffected work and report exact evidence if no authorized alternative exists.
* Keep repository artifacts and code in English. Communicate with the user in Portuguese.

The user explicitly rejected speculative complexity. Each feature, API and test must trace to an exercise requirement, an adopted project decision, a choice in this specification, or a necessary verification of those contracts. Do not build a general banking platform or a generic recovery framework.

## 2. Source precedence and reading order

Read repository `AGENTS.md`, then:

1. `README.md`.
2. `docs/exercise-inputs/exercise-statement.md`.
3. `docs/exercise-inputs/business-rules-corrected.md`.
4. `docs/deliverables/AMBIGUITIES.md` and `docs/architecture.md`.
5. `docs/deliverables/NUMBERS.md`, `REJECTED.md`, `WORKLOG.md`, and `tests/README.md`.
6. The examples and research sections referenced by a specific task.

Explicit user instructions take precedence. The corrected rules and ambiguity decisions define adopted interpretations and exceptions to the preserved exercise statement. Research proposals do not automatically become policy. The source baseline already incorporates H+1 ordinary fee dates, E10 receipt, monthly scenario mapping, and signed interest settlement. Do not repeat the reconciliation work or use older examples as current expected results.

Sections 5 and 6 below make the remaining scenario and implementation choices concrete. They are new defaults proposed by this specification, not claims about prior user decisions. The launch prompt approves them. If fresher project documents contradict a default, preserve their adopted policy, record the conflict and smallest adjustment, and recompute affected expectations independently. Do not silently force the implementation to match obsolete numbers.

Read the complete [PLAN.md](PLAN.md) before dispatching implementation agents.

## 3. Scope and architecture

Use one Clojure/JVM project and one process. Use Clojure's standard library, `clojure.test`, `clojure.spec.alpha` and JDK decimal arithmetic. Pin the actual dependency versions used. Prefer an already available compatible runtime; do not alter machine configuration to obtain a preferred version.

Runtime state and event delivery remain in memory. Source files, test fixtures, reports and build tooling are not runtime ledger persistence. Exclude Kafka, HTTP, UI, databases, durable queues, actual bank integrations, distributed deployment, tax policy and a general holiday calendar. No infrastructure framework is required.

### Domain ownership

* **Authorization** owns operational transactions, recorded decisions, holds, snapshots and its pending deliveries. All financial effects, including fees and interest payments, pass through it. It never reads Ledger balances.
* **Ledger** owns immutable, balanced journal entries and accounting queries. It has no authorization, fee or interest policy.
* **Yield and Fees** owns approved input history, assessments, daily interest components, adjustments and settlement receipts. It reconstructs bases from events and opening state, never from another module's balance.
* **Transaction** is a small technical entry adapter that submits only to Authorization.
* **System/replay** wires the modules, drives logical time and delivery, and obtains reports. Reporting does not belong to Transaction.

Each domain has pure business functions, a small application facade, and in-memory adapters. Modules expose public contracts and modify only their own state. Use protocols only for replaceable boundary operations that actually need them. A namespace or map of injected functions can be a port. No protocol is required for every function.

## 4. Business contracts that must hold

### Money and journal

* AED uses two fractional places, BHD three. Use `BigDecimal`, never binary floating point for money or rates.
* Daily interest is the exact product `max(base, 0) * 0.0004`, rounded once with HALF_UP to the currency scale. Do not round intermediate products or aggregated raw daily interest.
* Monetary adjustments subtract rounded monetary targets and previously recorded monetary amounts exactly. Pending interest is neither spendable nor interest bearing.
* Ledger entries are appended, never edited or deleted. Both sides of a movement are stored together and balance in the same currency.
* Queries and reports append nothing. Mutable indexes may point to immutable history but cannot replace or rewrite it.

### Authorization and delivery

* A new hold is approved only when remaining availability is at least zero. Holds do not change financial balance.
* A decline records its supplied ID and decision, without a hold, financial posting, snapshot or counter increment. Later funds or a reversal do not change that decision.
* An already recorded ID is ignored before inspecting or validating its payload, including changed or malformed content. Validate only enough envelope data to identify it first. Recheck uniqueness inside atomic recording.
* Each financial transaction or hold change creates a snapshot. The first counter per account is 1; subsequent snapshots increment by one. Initial state is version 0, not a recorded transaction.
* Identity checking, calculation-base checking and outcome recording are indivisible. If an uncommitted base changed, recompute against the latest base with the same ID. Never attach a newer counter to an old computed result.
* Confirmed settlements debit the actual amount even when a hold is absent or availability is negative. Report missing correspondence separately. Auth-A's settlement is final: debit 185.00, end its 200.00 hold, release the unused 15.00 without a credit.
* Support documented partial settlement and explicit release. Do not invent an automatic expiration schedule for the six-day replay.
* After Authorization records, delivery to Ledger and Yield is separate. A failed delivery retries the recorded movement with its original ID and dates, without another operational effect.
* Financial submissions from Yield validate `source_event_counter` against Authorization's current account counter atomically with recording. A mismatch records no payment or fee. A decline or another account's event does not invalidate the source account version.
* Duplicate financial submissions do not request recalculation or create another payment. A stale, unrecorded submission retains its ID for the next attempt.

### Fees, corrections and settlement

* Daily fee configuration is AED 25.00 for ACC-001's type and BHD 0.000 for ACC-002's type. Preserve the documented BHD exception explicitly.
* The job on D considers cumulative input bookings through D-1. Within those inputs, economic balances include value dates through the assessed day.
* For historical day H, exclude only H's own fee components and adjustments already included in its assessment base. Retain other periods' fees and refunds at their effective dates. A zero base incurs no fee.
* Ordinary fee assessment for H occurs on H+1, using H+1 for booking and value dates. Late corrections use the actual correction day for both dates.
* A reversal restores principal once with the supplied dates. Recalculate affected fees and interest. Reversal fee refunds are booked on the correction day and valued at the original fee charge's value date. Interest corrections retain current correction dates.
* Every correction is `corrected target - (original amount + all prior adjustments)`, including paid adjustments. Repeating an unchanged calculation adds no nonzero difference.
* Settle eligible unpaid interest components exactly once. Positive totals credit, negative totals debit even into a negative balance, and zero settles without a financial movement. Preserve prior payments and settled components for future comparisons.
* Day 6 is the first business day of a new month; Day 5 ends the previous month. The Day 6 payment covers eligible previous-month ordinary accruals and eligible adjustments. Adjustments booked on Day 6 are excluded. The job's newly computed ordinary Day 5 accrual is eligible.
* Day 6 interest is calculated on Day 7 and belongs to the new month. No real month, year, jurisdiction or holiday calendar is needed for this fixture.

## 5. Explicit implementation defaults

Record these as specification-derived choices in `agent-decisions.md` before module implementations diverge.

### D1. Runtime and source completeness

Use a deterministic in-memory dispatcher. Authorization records its outcome and pending outgoing messages together; recipients process those messages afterward. Acknowledge each recipient separately. One failed delivery remains pending and can be retried explicitly. No timers, exponential backoff, dead-letter queues or restart recovery are needed.

Normal replay drains pending delivery before a calculation or a report declared complete. Yield tracks a contiguous per-account event prefix, including hold-only snapshot events. It must not declare version N complete merely because it observed event N. This proves completeness of the recorded internal feed, not receipt of every future external event. E10 is a later external receipt.

Jobs run serially. Keep the atomic Authorization guarantees and one controlled version-change test. Do not build a second optimistic-concurrency framework for Yield's internal calculation records.

### D2. Minimal bookkeeping

Treat each customer book account as a liability whose positive customer balance is credits minus debits. Use a clearly named clearing counterpart in the same currency. Incoming customer money debits clearing and credits the customer liability; outgoing money does the reverse. Fees and interest may use that same counterpart, with their purpose preserved as metadata. This is a deliberately small exercise chart, not a production chart of accounts.

### D3. E10 representation

Process E10 as one supplied transaction and one Authorization snapshot, with the immutable installment breakdown `[3.333M 3.333M 3.334M]`. Ledger records one atomic journal containing three identified, balanced posting pairs, one per installment. This creates three customer credits totaling BHD 10.000 and no additional parent credit. Retain installment positions in the journal and report.

The original E10 ID protects the complete transaction from redelivery. This representation avoids a separate batch-commit protocol while implementing the required installment allocation. Do not describe it as three independent arrival events.

### D4. Zero settlement

A zero eligible total creates an immutable settlement receipt in Yield, linking the selected components once. It sends no financial command, creates no Ledger journal entry and does not advance Authorization's counter. Serialize component selection and receipt recording within Yield. Later eligible corrections remain separate unpaid components.

### D5. Identity and boundary validation

Use supplied transaction IDs unchanged. Their uniqueness is global within Authorization for this application; Ledger deduplicates financial deliveries by the same ID. Yield has separate namespaces for input IDs, calculation components and settlement receipts.

Use deterministic internal IDs derived from account, purpose, reference period and triggering transaction, with a reserved `system/` prefix. Preserve a proposed movement's ID across source-version retries. New financial corrections receive new IDs. The scenario's input IDs do not use that reserved prefix.

New commands require a known account, matching currency, supported operation, valid logical dates and valid required references. For external positive amounts, accept `BigDecimal` values exactly representable at the currency scale, normalize that scale without monetary rounding, and reject excess nonzero precision. Calculation outputs use the documented rounding policy. Do not silently invent missing amounts or currencies.

### D6. Small financial coordination

Submit nonzero fee differences one component at a time through Authorization. After each accepted movement, deliver its event to Yield before producing the next versioned proposal. Calculate that next proposal from the refreshed complete view; do not relabel an earlier proposal's source counter. Record only confirmed effects as recorded financial adjustments.

Interest components become settled only after an accepted or confirmed-duplicate financial payment, or the local zero receipt. An unrecorded stale payment settles nothing. Keep this coordination specific to the documented fee and interest flows.

## 6. Replay schedule and independent expectations

This schedule is an explicit BANK-SPEC choice. Use logical integer days and a separate receipt day. Preserve E1 through E10 order and their supplied booking and value dates.

| Checkpoint | Work |
|---|---|
| Day 1 | Process E1, E2; drain delivery; capture the Day 1 report. |
| Day 2 | Calculate Day 1 with booking cutoff 1; process E3; drain and report. |
| Day 3 | Calculate Day 2 with cutoff 2; process E4; drain and report. |
| Day 4 | Calculate Day 3 with cutoff 3; process E5, E6; drain and report. |
| Day 5 | Calculate Day 4 with cutoff 4; process E7, E8; drain and report. E7 is not yet calculation-eligible. |
| Day 6, first | With cutoff 5, review E7's effects on Days 2 through 4 in order, then calculate ordinary Day 5. Post eligible fee differences through Authorization and drain delivery. |
| Day 6, receipts | Process E9, then E10. E9's principal effect is immediate but its booking is outside the day's calculation cutoff. Review E10's Day 5 interest using its eligible booking, appending its current-day correction. |
| Day 6, final | Settle eligible previous-month interest, drain delivery and capture the final six-day report. |
| Separate Day 7 continuation | Apply cutoff 6, review E9's effects on already assessed Days 2 through 5, then calculate ordinary Day 6. Keep these results explicitly separate from the six-day operational reports. |

The Day 7 continuation introduces no new external business event. It verifies the documented reversal compensation and Day 6 interest under D-1. Do not admit E9 into Day 6 calculations or bring Day 7 refunds into the Day 7 job's booking-cutoff-6 inputs.

### Frozen expectations under this schedule

| Fact | Expected result and independent rationale |
|---|---|
| ACC-001 principal after E1/E2 | `1200.00 - 950.00 = 250.00`. |
| After E3 | Financial balance 250.00, held 200.00, available 50.00. |
| After E4 | Financial balance 650.00, held 200.00, available 450.00. |
| After E5 | Financial balance 465.00, hold zero. No credit for the unused 15.00. |
| After E6 | Financial balance 285.00 and a missing-Auth-Z occurrence. |
| After E7 | Current financial balance -335.00. Historical principal for Days 2, 3, 4 is -370.00, 30.00, -335.00. |
| E8 | Declined. No hold and no new snapshot. It remains declined after E9. |
| Ordinary interest for Days 1 through 5 | AED `[0.10, 0.10, 0.26, 0.11, 0.00]`, calculated as those days originally close. |
| E7 fee effects on Day 6 | H2 25.00, H3 0.00, H4 25.00, H5 25.00: three charges totaling 75.00, all booked and valued on Day 6. |
| E7 interest corrections | H2 -0.10, H3 -0.25, H4 -0.11, totaling -0.46, booked on Day 6 and still pending at that day's payment. |
| ACC-001 Day 6 payment | `0.10 + 0.10 + 0.26 + 0.11 + 0.00 = 0.57`. E9 and Day 6 adjustments are excluded. |
| ACC-001 Day 6 operational close | `-335.00 - 75.00 + 620.00 + 0.57 = 210.57`. Pending interest adjustment -0.46. |
| ACC-002 Day 6 | Three postings total 10.000; Day 6 settlement is zero; E10's Day 5 correction is +0.004 pending. |
| Day 7 E9 fee refunds | +75.00 booked Day 7 and valued Day 6; current ACC-001 balance becomes 285.57. This is not its earlier Day 6 operational close. |
| Day 7 E9 interest corrections | H2 +0.10, H3 +0.25, H4 +0.11, H5 +0.11: +0.57. Prior-month unpaid adjustments net `-0.46 + 0.57 = 0.11`. |
| Ordinary Day 6 interest on Day 7 | ACC-001: `210.57 * 0.0004 = 0.084228`, rounded to 0.08. ACC-002: `10.000 * 0.0004 = 0.004`. Both belong to the new month. |
| Account counters | ACC-001 ends Day 6 at 12 and Day 7 at 15, with each of the three fees and three refunds submitted separately. ACC-002 stays at 1 after E10 and zero settlement. |

The Day 7 refunds have booking 7, so they cannot increase Day 6's base in the cutoff-6 job. A later historical query admitting those refunds is a different view. The continuation ends after Day 7; do not build an extended calendar simulation.

Recheck these numbers with arithmetic independent of the production calculation functions before treating their tests as an oracle. If a genuine conflict is found, document the rule and calculation, correct the expectation transparently, and preserve all already adopted policies. Never replace expected output with the implementation's output merely to make tests pass.

### Six-day reporting

For each account and day, print operational closing financial balance, active holds, availability, fee assessments/adjustments, authorization states, interest paid/pending and occurrences. Include reference day and assessment/recording day for fees. Reports captured before later arrivals are immutable snapshots.

Also make historical accounting queries available with explicit economic day, booking cutoff and journal position. Label reconstructed history separately. Before calling a report complete, drain known pending internal delivery. Querying alone must never create a correction.

## 7. API contract

Use one public `api` namespace per domain. The signatures below are the shared contract; internal helpers are private unless genuinely reused. Constructors return opaque module handles, not public atoms. `!` marks state effects in this project.

| Namespace | Public functions |
|---|---|
| `account-ledger.authorization.api` | `(create config)`, `(submit! module command)`, `(snapshot module account-id)`, `(history module account-id)`; technical delivery ports `(pending-deliveries module)` and `(ack-delivery! module delivery-id)`. |
| `account-ledger.ledger.api` | `(create config)`, `(post! module committed-movement)`, `(balance module query)`, `(journal module account-id)`. |
| `account-ledger.yield-fees.api` | `(create config ports)`, `(receive! module approved-event)`, `(calculate! module request)`, `(settle! module request)`, `(report module account-id)`. |
| `account-ledger.transaction` | `(submit! authorization-module command)`. Calls only Authorization. |
| `account-ledger.system` | `(create config)`, `(submit! system command)`, `(drain! system)`, `(run-day! system request)`, `(settle! system request)`, `(report system query)`. Test/replay composition only. |
| `account-ledger.replay` | `(run)` returns immutable six-day reports and final state views; `(continue-day-seven system)` drives the separate continuation; `-main` prints the required demonstration. |

The coordinator freezes exact schemas before agents implement independently. The minimum shared fields are:

* Command: `:transaction/id`, `:transaction/type`, `:account/id`, `:booking-day`, `:value-day`, `:received-day`; positive `:money/amount` and `:money/currency` where applicable. Authorization and settlement use `:authorization/id`; final settlement uses `:settlement/final?`; reversal uses `:reversal/of`; E10 uses `:installment-count 3`.
* Supported external operations: `:credit`, `:debit`, `:authorization`, `:settlement`, `:release`, `:reversal`. System fee/interest movements add purpose, component links and `:source-event-counter`; only the financial path uses them.
* Committed event: original ID and dates, assigned `:event-counter`, financial effect when any, immutable hold/decision information, and correction/period links when applicable. It contains enough transaction information for Yield to reconstruct balances; it does not expose an operational balance as Yield's input.
* Snapshot: `:account/id`, `:financial-balance`, `:held-amount`, `:available-balance`, `:last-event-counter`. The value is immutable.
* Result: `:outcome` is `:recorded`, `:declined`, `:duplicate`, `:retry-required`, or `:invalid`; carry transaction identity, stable `:reason` when applicable, and occurrences separately. `:recorded` means locally recorded, not delivered everywhere. A financial success includes its immutable `:recorded/event`, with the committed amount, counter and component links. A duplicate includes `:original/result` from the earlier recording without inspecting the new payload. A caller confirming a duplicated payment uses that original receipt and its original component links, never a newly proposed selection. Return delivery state separately when relevant.
* Ledger query: `:account/id`, `:value-through-day`, `:booking-through-day`, optional `:as-of-journal-position`. The journal position describes Ledger's local append order, not an account counter or a receipt date. The balance result contains amount, currency and the effective query boundary.
* Calculation request: `:run-day`, `:reference-day`, `:booking-cutoff`, and `:mode` (`:daily` or `:historical`). Historical review identifies `:from-day`, `:through-day` and `:cause/transaction-id`. Daily mode uses reference D-1; the E10 historical review concerns only Day 5 with cutoff 5.
* Settlement request: stable `:settlement/id`, `:run-day`, `:period/end-day`, `:booking-cutoff`. Yield chooses eligible unpaid components and includes its complete source counter in financial proposals. The caller does not supply a guessed payment amount.

Use schemas and explicit boundary validation. Do not depend on test-only instrumentation to validate live API inputs. Expected business outcomes are data; technical failures and violated invariants remain visible errors. Pure queries have no hidden delivery, recalculation or state changes.

### Technical ports frozen before delegation

* `authorization/pending-deliveries [module]` is a read-only query returning an ordered vector of immutable envelopes: `{:delivery/id id :destination destination :event committed-event}`. Each ID identifies one transaction/destination pair. Destinations are `:ledger` and `:yield`. Hold-only snapshot events go only to Yield; declines create neither kind of delivery.
* `authorization/ack-delivery! [module delivery-id]` acknowledges that exact envelope idempotently and returns `{:delivery/id id :acknowledged? true}`. It changes delivery tracking only, never business records, balances, snapshots or account counters. Unknown delivery IDs return `{:delivery/id id :acknowledged? false :reason :unknown-delivery}`.
* `system/drain! [system]` routes pending envelopes through `ledger/post!` or `yield-fees/receive!`. Acknowledge only successful or confirmed-duplicate receipt. Preserve a failed envelope for explicit retry. Return `{:pending-count n :errors [...]}`; every error identifies its delivery ID and reason. This command returns on a delivery failure instead of spinning forever.
* The coordinator supplies Yield's `ports` map with `:submit-financial!`, a one-argument function from a financial command to the Authorization result, and `:flush-deliveries!`, a zero-argument function with the same result shape as `system/drain!`. These are boundary functions, not access to Authorization state.
* After an accepted fee or payment, Yield calls the flush port before preparing another versioned financial proposal. A nonzero pending count or delivery error prevents claiming a complete input view or complete report. Normal replay resolves known pending delivery at its next explicit drain; it does not invent timers or retry policies.
* Ledger `post!` and Yield `receive!` acknowledge receipt using `{:outcome :recorded :transaction/id id}` or `{:outcome :duplicate :transaction/id id}`. Out-of-order input can be received and buffered by Yield while its complete prefix remains unchanged.

Do not hold a module's state lock while invoking another module. Reentrant delivery to Yield must be possible after its financial submission. Freeze these port contracts alongside the data schemas in G0.

## 8. Required tests and evidence

Use `clojure.test`. Unit tests cover every domain function through meaningful examples and boundaries; private helpers may be exercised through callers. Integration tests use the real module application layer and in-memory adapters. End-to-end tests use all three real modules. No percentage alone establishes completion.

| ID | Required coverage |
|---|---|
| M1 | Currency scales, exact decimal handling, positive/zero/negative interest bases and HALF_UP ties; AED 12.49 versus 12.50; BHD 1.250. |
| M2 | Daily rounding before sum: two AED 465.00 days pay 0.38, not 0.37; E10 preserves exactly 10.000. |
| A1 | Hold at exact availability, refusal one minor unit above, decline history/counter, no automatic reevaluation after added funds. |
| A2 | Final/partial settlement, explicit release, unmatched confirmed settlement and actual debit despite unavailable funds. |
| A3 | Duplicate before payload validation; changed payload, duplicate decline and duplicate financial payment preserve original effects and counters. |
| A4 | Atomic identity/base/outcome; controlled competing requests cannot overspend a hold; stale source rejects financial recording, fresh recalculation with the same ID succeeds. A decline and another account's event do not stale this account. |
| L1 | Balanced atomic journals in one currency; append-only history; duplicate delivery; temporal queries and installment posting breakdown. |
| Y1 | D-1 cutoff; complete internal prefix; a future booking can change source version without changing the eligible amount. |
| Y2 | Negative-only fees, zero BHD configuration, exclusion of only H's own included components, retention of other days' fees/refunds, incremental nonduplicating corrections. |
| Y3 | Pending interest is not available or earning interest; signed settlement including zero; each eligible component settles once; paid adjustments remain in later difference calculations. |
| Y4 | Reversal principal once; separate fee-refund and interest-adjustment dates; Day 7 continuation; earlier authorization decisions preserved. |
| X1 | One delivery failure after Authorization recording, followed by redelivery, produces one operational effect and one journal. Do not create a chaos-testing platform. |
| X2 | Full E1..E10 replay, six-day report and independent Section 6 oracle; zero initial BHD payment and delayed +0.004 correction; rerunning from fresh state reproduces the same report. |
| X3 | Executable fixtures for current examples 04, 05, 06, 07 and 08, preserving their stated assumptions. These are separate scenarios, not alternate main-replay schedules. |
| D1 | Each of the eight acceptance criteria has a documented conclusion and a test or explicit arithmetic supporting that conclusion. Refuse criterion 2's exactly-one-fee claim under this schedule. Qualify criterion 6 by view and time, rather than inventing blanket restoration. |

Report which tests cover each ID in `docs/deliverables/VERIFICATION.md`. Audit all domain functions for coverage; close meaningful gaps. Do not add tests that merely restate an implementation expression or assert that a private function exists.

### Required annotated failing test

Keep exactly one deliberate design challenge in a separate runner. Register a transaction ID, resubmit it with a different amount, and assert that the API reports a payload conflict. This expectation fails because the adopted policy ignores known IDs without inspecting the payload. Annotate inline that this exposes reliance on producer identity uniqueness and the inability to detect content collisions.

The normal suite includes a passing regression for the actual approved duplicate behavior. The challenge runner executes its real failing assertion and returns a nonzero exit code. Do not skip it, invert its assertion, convert it to a passing expected-failure wrapper or include it in the normal runner. Verify the failure is exactly the annotated assertion, not a loading or runtime error.

## 9. Commands and completion criteria

Provide these actual commands, runnable from the worktree root:

```sh
clojure -M:test
clojure -M:test unit
clojure -M:test integration
clojure -M:test e2e
clojure -M:demo
clojure -M:design-challenge
```

The default test command runs all normal groups and exits nonzero on any failure or error. It must not report success after discovering zero tests. Group arguments select the named group. The demo runs the six-day replay; display the Day 7 continuation separately if included in its output.

Completion requires all of the following:

* Full normal suite passes after the final code change, with actual counts and exit status captured. Repeat from a fresh JVM once to confirm reproducibility, not indefinitely.
* Demo exits successfully, prints every required account/day, and matches the independently checked expected amounts and states.
* The separately invoked design challenge yields exactly one explained assertion failure and no execution errors.
* No stubs, skipped required scenarios, placeholder tests, unimplemented public functions or ignored relevant failures remain.
* An independent read-only review checks financial correctness, state ownership, idempotency, dates and whether tests can detect defects. Address substantiated findings and rerun affected checks.
* README contains verified prerequisites, commands, expected output interpretation and the deliberate failure explanation. `docs/api.md` documents the public contracts and examples.
* Update AMBIGUITIES, NUMBERS, REJECTED, architecture, tests/README and related examples only as needed to describe the delivered behavior. Preserve exercise input and historical worklog entries.
* `agent-decisions.md` records choice, rationale, applicable source and associated test. WORKLOG uses actual recording timestamps in America/Sao_Paulo and does not invent execution duration.
* VERIFICATION records baseline/worktree, exact commands, observed results, traceability, review findings and any material limitation. Never claim an unexecuted check passed.
* Review diff, whitespace, local links and final worktree status. Preserve the original checkout. Leave all changes uncommitted and unstaged.

At the six-hour target, correctness and mandatory completion take priority over optional polish. Do not declare success merely because the time budget elapsed. Continue meaningful corrective work when possible; report any actual incomplete requirement explicitly. Finishing early is acceptable once all required evidence exists.

The final handoff must identify the worktree and branch, how to run tests/demo, observed test results including the deliberate failure, final financial figures with their time boundaries, key decisions and any real blockers. The user should not need to reconstruct the result from agent messages.

## 10. Preparation review

This package was checked before implementation. Independent read-only reviews covered the replay arithmetic and the task/API contracts. The missing technical delivery ports found in review were specified in section 7. A separate Decimal calculation check passed 27 assertions for the monetary expectations, and local Markdown links, code fences and whitespace were checked. These are specification checks, not tests of a ledger implementation. The executor must still produce all evidence in section 9.
