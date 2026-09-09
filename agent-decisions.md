# BANK-SPEC execution decisions

Source: [approved specification](docs/implementation/bank-spec/SPEC.md) and [plan](docs/implementation/bank-spec/PLAN.md), launched 09 September 2026 at baseline `ba4201b51901b355c896cc94981bea14139c925e`. The implementation was subsequently rebased and committed with user authorization as recorded in [WORKLOG](docs/deliverables/WORKLOG.md). The financial intent corrections and regression follow-up below form one user-authorized commit on `codex/bank-spec` after `ad24b3fcdb878bf6dc41c7cb1106afaf80ad16d2`; Git history records its final identity.

## Financial intents before effects

The user explicitly required local calculation recording before any outbound effect and correction of both reported duplication defects. This section supersedes the earlier confirmation-recovery claims where they were incomplete.

* Save the entire financial command before submission: stable transaction ID, component links, amount, dates, source counter and fee assessment when applicable. Keep an append-only proposal history and at most one unresolved command per account. Existing serial jobs make a general retry framework unnecessary.
* An exception or unknown result preserves the exact pending command. Retrying the same settlement resends that saved command; calculation resumes its pending fee first. Another financial command, including a new zero settlement, waits. Pure accruals can continue without changing the earlier intent.
* A definitive invalid or stale-source rejection records no money, so clear pending state and allow a fresh proposal. Preserve the rejected calculation in history and retain its unrecorded transaction ID. Never attach a fresh counter to an unknown old payment.
* Confirm the original assessment or settlement receipt atomically with clearing its pending entry. Both the original financial result and delivered committed events can provide that confirmation. Event delivery recognizes the original settlement independently of the next caller's ID. Intent recording itself does not mark interest paid or a fee charged.
* Require coherent fee assessment metadata and interest receipt metadata at the shared boundary before Authorization changes money. Normal producers already supplied these fields; accepted incomplete commands could otherwise bypass Yield's history. Copy the confirmed command's mandatory source counter into its fee record, avoiding a redundant mandatory nested field.
* The effect-order audit found Authorization already saves outcome, snapshot and envelopes before dispatch, and Ledger records the balanced journal before acknowledgement. No changes to those module lifecycles were necessary. Every cross-module call still occurs outside the local state lock.

Verification: regressions inspect saved reports from inside the submission port, lose responses before and after recording, exercise different settlement IDs, changed retry dates, definite rejections, fee refunds and incomplete accepted contracts. The initial corrected suite passed 97 tests and 964 assertions; [verification](docs/deliverables/VERIFICATION.md#financial-intent-correction) preserves command evidence and scope limits.

## Closing the review's regression gaps

The user's next instruction included all four demonstrated surviving mutations and the missing settlement recovery scenario, then authorized a signed commit with WORKLOG. Retain the earlier production fixes, strengthen the exact financial/delivery/report outcomes, and prove that all four original mutations fail against the complete suite. No business rule changes or general mutation-testing framework are needed.

The final suite passes 98 tests and 1,033 assertions. Negative Ledger delivery, ignored duplicate responses, BHD availability bypass and fabricated occurrences now produce respectively 11, 12, 14 and 7 assertion failures, with no execution errors. A direct System regression preserves one payment after response loss and a different settlement ID. [Verification and reproducible probes](docs/deliverables/VERIFICATION.md#regression-protection-follow-up) record the evidence. Prior review documents remain untouched and outside the commit.

## Specification defaults accepted at launch

* D1: Deterministic in-memory delivery with a pending envelope per recipient. Yield tracks a contiguous per-account prefix including holds. Serial jobs, explicit drain, no timers or restart recovery. This proves known internal delivery only. Verification: X1, Y1.
* D2: Customer liabilities use credits minus debits and a same-currency clearing counterpart. This is the smallest balanced chart for the exercise. Verification: L1.
* D3: E10 is one transaction and one snapshot containing three installment amounts. One atomic journal stores three balanced pairs and no parent overcredit. Verification: M2, L1, X2.
* D4: Zero interest settlement records a local immutable receipt and component links, with no payment, journal or Authorization counter. Verification: Y3, X2.
* D5: Supplied transaction IDs are globally unique within Authorization; internal deterministic IDs use `system/`. Validate new payloads exactly at the boundary after duplicate lookup. Verification: A3, A4 and the separate annotated challenge.
* D6: Submit fee components separately, drain, and recompute from the refreshed complete view. Settle interest links only after financial confirmation or a zero receipt. Verification: A4, Y2, Y3.

## Implementation refinements

* Use the installed Clojure CLI 1.12.5.1654, Clojure 1.12.5 and Java 25. Pin spec.alpha 0.5.238 and core.specs.alpha 0.4.74 from the existing cache. No machine configuration changes. Verification: executable foundation and final runtime evidence.
* Use positive integer logical days; receipt is independent from booking/value dates. Day 5 is month end, Day 6 begins the new month. No actual calendar is inferred. Verification: X2.
* All jobs target one account. Composition iterates the two scenario accounts in sorted order. This keeps source version and payment identity unambiguous. Verification: e2e replay.
* Reversals derive their positive amount by reference, reject mismatching supplied amounts, and negate the original signed principal effect once. No invented external amount. Verification: A2/Y4.
* Exact map shapes and ports are frozen in [CONTRACTS](docs/implementation/bank-spec/CONTRACTS.md). Constructors use validated account types to obtain the fee. Shared changes require coordinator notification.

## Milestones

* G0 foundation: verified clean primary checkout and baseline, created the requested linked worktree, copied the approved package, pinned available runtime, executed the first money tests (3 tests, 19 assertions passing). Domain contracts are frozen for delegation. Foundation checks completed: contract tests and independent integer oracle pass. The separate numerical review also passed 29 Decimal assertions.

The copied PLAN checkboxes record verified work. Final executed evidence will be maintained in `docs/deliverables/VERIFICATION.md`.

## Boundary and review refinements

* New commands reject computed event fields. Authorization alone creates the signed effect, counter, installment breakdown and hold outcomes. Ledger validates a command projection and then its full committed event. This prevents a valid operational record with a forged installment breakdown that cannot be posted. Verification: `computed-event-fields-cannot-be-injected-by-a-new-command` plus full module integration.
* Three installments require at least three minor units so every posting remains positive. The allocation helper and command boundary reject a smaller total. E10 is unchanged. Verification: `three-installments-conserve-money` and `financial-path-requires-version-and-links`.
* Ledger queries fill an omitted journal position with the current global append position, and reject a requested future position. This makes the returned boundary reproducible. Verification: `new-query-and-configuration-boundaries-are-validated` and temporal-view tests.
* Authorization uses one short local lock around a pure transition and state replacement. It never calls another module while holding it. This is smaller than an optimistic retry loop while making identity, calculation and recording indivisible. Verification: controlled competing hold, identity and financial-source requests.
* Authorization lifecycle IDs are unique per account. A new explicit request requires a new authorization ID; release above the remaining hold is invalid; a second reversal of the same principal is invalid. Original decisions remain in history. Verification: lifecycle and reversal unit tests.
* Example 08 requires two ordered +0.02 historical adjustments even though both Day 2 credits are already known at the Day 3 review. A complete unbounded review would instead append one +0.04 difference. The bounded fixture therefore uses explicit `:input-through-event-counter` values 2 then 3, permitted only with `:interest-only? true`. Receipt dates remain unchanged; the earlier historical view cannot submit a fee. Ordinary replay jobs use all eligible inputs and never set this option. Monotonic review boundaries prevent an old view reversing a newer correction. Verification: `example-08-ordered-historical-views` and Yield boundary tests. This refinement serves that documented example only.
* Daily replay of an unchanged assessment appends nothing. A changed daily target requires an explicit historical review with a cause. Older calculation cutoffs or input bounds cannot regress an assessed view. Verification: Yield historical-review regressions.

## Integrated milestones and review

* G1/G2: Authorization and Ledger implementations and module tests complete; later formal findings have dedicated regressions.
* G4: Actual end-to-end replay and examples 04 through 08 passed (8 tests, 99 assertions) and the demo printed twelve reports plus the separate Day 7 continuation. Captured reports are reproducible from fresh state.
* Review found and corrected an ordinary-fee bypass through an unconstrained interest-only option and a non-reversal metadata field consuming another movement's reversal eligibility. New-command versus committed-event boundaries and signed effects were tightened with failing regressions first.
* Financial confirmation recovery is verified: received accepted fee/payment events must preserve original monetary records and settlement links after a lost response, even when a retry's recalculated total becomes zero. Correction and scoped re-review completed. The normal suite passed 78 tests and 669 assertions twice in fresh JVMs; the reviewer independently observed the same result with no remaining actionable finding.

* Confirmation recovery uses only received accepted events: fee metadata is indexed idempotently on receipt, and the stable original interest payment is checked before any new/zero settlement selection. Completing an interrupted first daily job can link its fee correction to a newly eligible principal cause after the original fee's source version; if none exists, it returns an explicit unresolved outcome. This preserves confirmed money without a generic retry framework. Verification: both lost-response regressions and independent scoped review.
* G3/G5/G6: All required module behavior, contract scenarios, deliberate challenge, independent review, CLI commands and API example are verified. [VERIFICATION](docs/deliverables/VERIFICATION.md) contains the requirement/function audit and actual command evidence. No required implementation remains pending.

## Module structure and public validation

The user requested pure `logic`, incoming/outgoing `ports`, `db/memory`, internal `model/models` and matching test directories for each domain. Public namespaces move to `ports.api-server`; function arguments, results and financial behavior remain unchanged. No compatibility forwarding files or generic adapter framework are added. Ledger has no outgoing dependency, so its client namespace documents that fact without an invented operation.

The shared area separates pure arithmetic, validation, identifiers and reporting from contract schemas. Mutable access stays in the memory adapters, and outgoing calls stay in clients. Pure callbacks run atomically before external effects. Every previous test is preserved; extra tests protect storage atomicity, models, public metadata and the dependency boundary.

Review exposed a Clojure spec interaction: globally registering extra payload keywords in internal models changes open `s/keys` validation elsewhere. New model specs therefore use their own namespaces, with explicit predicates for qualified fields. Model declarations do not add runtime validation to storage creation. This keeps internal documentation from changing accepted commands based on namespace load order.

At the user's subsequent request, the repo also includes a concise architecture summary, tradeoffs and production considerations, with one combined PDF limited to four pages. The detailed architecture remains a separate engineering reference. Production recommendations and the cited CBUAE consumer disclosure/statement requirements are explicitly separated from implemented behavior and the exercise's adopted numerical rules.
