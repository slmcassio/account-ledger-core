# Tradeoffs

These implemented choices favor a small, explainable model. Production limits below describe work still required, not delivered capabilities.

| Choice | Benefit | Cost or boundary |
|---|---|---|
| One application, three modules, explicit ports | Clear financial ownership without distributed infrastructure. | Shared failure and memory limits; ports provide no security isolation. |
| Pure rules, local atomic writes, serial jobs | Testable decisions; identity, version and recording stay together. | Commands serialize locally; concurrent Yield jobs are unsupported. |
| Append-only history and corrections | Reproduce earlier views and explain later changes. | Unbounded history and repeated scans. |
| Save financial intent before submission | Retry the original command; reconcile its original components. | New Yield fees or payments wait; principal continues. No restart recovery. |
| Recorded ID wins without payload comparison | Retries repeat no effect. | A reused ID can hide different content; the separate challenge demonstrates this limitation. |
| Reconstruct dated balances from approved events | Explain late corrections without consuming another module's balance. | Scanning costs; a complete internal prefix cannot prove all external inputs arrived. |
| Exact decimals and daily currency rounding | Reconcile posted amounts and rounded components exactly. | Currency scales, daily rate and AED fee come from the exercise; HALF_UP and the BHD fee exception are adopted choices. |

## What was cut and why

The first two rows are explicit exercise exclusions. The remaining rows are BANK-SPEC choices or limits of the supplied scenario. Earlier choices also exclude parallel jobs and payload-conflict detection.

| Cut or simplification | Why retained | Production risk deferred |
|---|---|---|
| Durable storage and database | Required in-memory scope. | State, pending intents and audit records vanish on restart. |
| HTTP/web layer and UI | Required core-only scope. | No customer interface, authenticated access or authorization of external callers. |
| Broker, distribution, timers, backoff and rejected-message queue | Deterministic delivery and explicit retry keep coordination small. | No independent failover or automatic recovery; failures need operator action. |
| Payment-network integration and external completeness proof | Inputs are assumed legitimate; only known internal delivery is checked. | Missing or disputed external money needs authentication, reconciliation and exception handling. |
| Automatic hold expiry | No contractual lifetime supplied. | Uncollected holds can remain indefinitely; production needs audited expiry releases. |
| Real business calendar | Integer days make the fixture reproducible. | Holiday, time-zone and cutoff mistakes can change financial eligibility. |
| General installment schedule | One supplied three-part credit is one atomic journal. | No independent future installment posting or collection lifecycle. |
| Production chart of accounts | Same-currency clearing balances this small ledger. | Fee revenue, interest expense and settlement controls need dedicated accounting mappings. |
| FX and yield taxes | No conversion or tax policies adopted. | Cross-currency valuation and required withholding/reporting are unimplemented. |
| Bank-caused error remediation | Corrections cover legitimate late transactions and reversals only. | Customer redress can require treatment different from regular pending-interest payment. |
| General historical-view engine | One explicit interest-only bound serves the supplied correction example. | Arbitrary partial views and partial-view fees are unsupported. |
