# Annotated Failing Test

The executable [design challenge](../test-design-challenge/account_ledger/design_challenge.clj) records a credit ID, resubmits it with a different amount, and asserts `:payload-conflict`. Its inline annotation explains the deliberate limitation: Authorization ignores a recorded ID before reading its payload, relying on producer ID uniqueness. A changed payload therefore returns `:duplicate` and leaves the original money unchanged.

Run `clojure -M:design-challenge` from the repository root. The real assertion fails, with exit 1, one failure and zero errors. It is not an expected-failure wrapper and is excluded from the normal test path. `clojure -M:test` covers the approved behavior with passing duplicate regressions.

[Verification](../docs/deliverables/VERIFICATION.md) records exact commands, counts, requirements and the independently reviewed result. The [normal runner](../test/account_ledger/runner.clj) discovers unit, integration and end-to-end namespaces, rejects unknown groups, and refuses success with no tests.
