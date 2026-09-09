# Annotated Failing Test

The executable [design challenge](../test-design-challenge/account_ledger/design_challenge.clj) records a credit ID, resubmits it with a different amount, and asserts `:payload-conflict`. Its inline annotation explains the deliberate limitation: Authorization ignores a recorded ID before reading its payload, relying on producer ID uniqueness. A changed payload therefore returns `:duplicate` and leaves the original money unchanged.

Run `clojure -M:design-challenge` from the repository root. The real assertion fails, with exit 1, one failure and zero errors. It is not an expected-failure wrapper and is excluded from the normal test path. `clojure -M:test` covers the approved behavior with passing duplicate regressions.

[Verification](../docs/deliverables/VERIFICATION.md) records exact commands, counts, requirements and the independently reviewed result. The [normal runner](../test/account_ledger/runner.clj) discovers unit, integration and end-to-end namespaces, rejects unknown groups, and refuses success with no tests.

## Normal test organization

Tests mirror each domain module under `test/account_ledger/<module>/`:

* `logic` tests pure calculations and transitions using immutable input/output data.
* `ports` tests public operations, real module integration and outgoing calls to injected recipients.
* `db` tests opaque storage, atomic transitions and immutable snapshots.
* `model` tests internal state shapes using actual constructed or recorded data.

Shared pure rules have matching tests under `shared/logic`; contract validation tests also exercise the shared schemas. Application integration remains under `integration`; exercise replay and the independent numerical oracle remain under `e2e`. `architecture` prevents logic from depending on APIs, storage or effectful operations. Ledger's client namespace has no outgoing operation, so there is no artificial client behavior to test.

The existing runner commands retain their meanings with these locations:

| Command | Selected tests |
|---|---|
| `clojure -M:test` | Every normal test namespace. |
| `clojure -M:test unit` | Pure logic, model schemas and architecture boundaries. |
| `clojure -M:test integration` | Module ports, memory adapters and application integration. |
| `clojure -M:test e2e` | The exercise replay, documented examples and independent numerical checks. |

The behavior regressions include saved financial intents, lost responses followed by a different settlement ID, negative-interest delivery, confirmed duplicate responses, BHD availability boundaries and actual occurrence content. Their expected values remain independent of production calculations. Verification counts and executed mutation results are recorded in [VERIFICATION](../docs/deliverables/VERIFICATION.md).

## Refactor verification

The current [verification record](../docs/deliverables/VERIFICATION.md#module-structure-refactor) includes three fresh normal runs, every test group, all 98 original test identities, full replay preservation and the four controlled review mutations using the new namespaces. Mutation probes are explicit verification commands outside normal discovery; each must fail with assertions, not execution errors. Internal model tests also ensure that loading schemas does not narrow unrelated public command metadata.
