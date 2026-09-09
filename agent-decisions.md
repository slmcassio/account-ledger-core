# BANK-SPEC execution decisions

Source: [approved specification](docs/implementation/bank-spec/SPEC.md) and [plan](docs/implementation/bank-spec/PLAN.md), launched 09 September 2026. The baseline is `ba4201b51901b355c896cc94981bea14139c925e`. All work remains unstaged and uncommitted in `codex/bank-spec`.

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
