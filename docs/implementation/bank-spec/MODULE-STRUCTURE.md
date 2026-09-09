# BANK-SPEC module structure refactor

## Approved scope

The user requested `logic`, `ports`, `db` and `model` directories in each domain module, separate `api-client` and `api-server` files, matching test organization, no API calls from pure logic, preserved behavior, consistent test results and a signed commit with WORKLOG. This is a structural refactor from commit `8530deb5f6a71e811c50e8fded58e79b9fbafa33`, continuing the existing isolated BANK-SPEC worktree.

## Design and ownership

* Authorization worker owns only Authorization source and its relocated tests. Ledger worker owns only Ledger source and its relocated tests. Yield worker owns only Yield source and its relocated tests. Workers use isolated checkouts; the coordinator integrates their files and owns combined correctness.
* `logic/core.clj` contains pure business rules and persistent state transitions. Yield can use a separate `logic/transitions.clj` for payment preparation and confirmation. Logic receives plain data and returns plain data; it never reads storage or calls another module.
* `ports/api_server.clj` retains each module's public operations and argument/result shapes. It coordinates state reads, pure decisions, atomic recording and outgoing calls. `ports/api_client.clj` owns outgoing calls to injected recipients. Authorization uses it to deliver its saved envelopes. Yield uses it to submit financial commands and flush deliveries. Ledger has no outgoing application dependency; its client namespace states that explicitly without adding a fake dependency.
* `db/memory.clj` owns the opaque mutable cell and local atomic transitions. Every callback passed to a transaction is pure. No cross-module call runs while a local state lock is held.
* `model/models.clj` declares the module's internal records and state schemas. Model definitions describe existing state without introducing stricter runtime command rules. Pure construction and transitions stay in logic.
* Shared arithmetic, identifiers and boundary validation move into `shared/logic`; shared contract schemas move into `shared/model`. Shared code has no storage or outgoing ports. The root System, Transaction and replay files remain the small application composition and fixture entry points.
* Tests mirror domain module directories. Existing behavior tests retain their names and assertions. Actual storage/atomicity tests belong in `db`, boundary integration tests in `ports`, pure rule tests in `logic`, and schema tests in `model`. System integration and full exercise replay remain separate application tests. The runner preserves unit/integration/e2e commands while discovering the new locations.

## Execution and verification plan

1. Capture the complete immutable replay reports and all 98 existing test names before edits. Establish a passing 98-test, 1,033-assertion baseline.
2. Refactor three modules independently and integrate a working combined application as their files arrive. Move shared code and update composition imports centrally. Preserve original map keys, financial IDs, dates, exceptions, duplicate behavior and lock boundaries.
3. Enforce the requested dependency boundary: logic may depend on pure logic/model namespaces, never ports, storage, application composition or effectful operations. Verify internal state schemas through real constructed states and transitions.
4. Run every original regression plus new meaningful model/boundary checks. Compare the full immutable replay data to the pre-refactor capture, not only selected totals. Verify all prior test names remain discovered.
5. Run the complete suite repeatedly in fresh JVMs and run each supported test group. Repeat all four recorded review mutations using the new namespaces and retain their real failures outside the normal suite. Run the deliberate challenge separately and require its single explained failure.
6. Review the integrated code, update current architecture/API/verification documentation and WORKLOG, check local links and preserve the original checkout, exercise statement and review report hashes. Commit the scoped result with the configured signer and verify its signature. Do not push.

## Progress

* Baseline suite executed: 98 tests / 1,033 assertions, zero failures/errors. Original test identities and full replay captures are retained for comparison during this refactor.

* All three modules integrated through the actual dispatcher; all 98 original tests and their assertions retained. Independent review confirmed pure boundaries, atomic local writes before effects, and original financial recovery behavior.
* Resolved the model spec load-order regression with namespace-local definitions and executable regressions. No stricter storage creation rule was retained.
* Final normal suite passed three times: 117 tests / 1,197 assertions, zero failures/errors. Unit 51/550, integration 58/543 and end-to-end 8/104 passed. Full immutable replay and Day 7 match the pre-refactor capture. Four review mutations fail with 11/13/14/7 assertions and zero errors. The separate challenge retains exactly one explained failure.
* README, API, detailed architecture, concise assessment documents and current verification evidence updated. The combined PDF has three visually verified pages, within the user's four-page maximum. WORKLOG records the completed work; the authorized signed commit follows final staging checks.
