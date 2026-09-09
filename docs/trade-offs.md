# Tradeoffs

The design favors a small, explainable financial model. The following choices are implemented; [production considerations](production-considerations.md) identifies additional work, not delivered capabilities.

| Choice | Benefit | Cost or boundary |
|---|---|---|
| One process, three modules, explicit ports | Separates financial ownership without distributed infrastructure. | Shared failure domain and memory limit. Ports do not provide security or process isolation. |
| Pure logic and local atomic transitions | Tests can exercise decisions without adapters; each module records identity and state together. | Local locks serialize changes. Yield jobs require serial execution. |
| Append-only records and corrections | Preserves previous decisions and explains changed historical balances. | History and indexes grow; dated queries and recalculations scan retained records. |
| Save financial intent before submission | Unknown outcomes preserve the original command; confirmation prevents duplicate payment. | A pending command blocks another financial operation for that account. Recovery does not survive process exit. |
| Recorded identity wins without comparing payload | Retries return the original outcome without another effect. | A reused ID can hide different content. The separate failing challenge demonstrates this accepted limitation. |
| Reconstruct balances using booking and value dates | Makes late corrections reproducible. | More scanning; internal counter completeness does not prove all external inputs arrived. |
| Exact decimal amounts and daily currency rounding | Implements the adopted numerical rules with explainable reconciliation. | HALF_UP, rates and fee configuration remain product choices, not consequences of the currency code. |

## What was cut and why

Durable storage, HTTP, UI and a broker were excluded by the exercise. Automatic expiry, a real business calendar, FX, taxes and payment-network integration lack required rules in this scope. These exclusions keep the implementation defensible but leave crash recovery, external reconciliation and contractual timing unresolved for production.

Unsafe submission-before-recording and incomplete financial metadata were corrected, with regressions retained. Scope exclusions were not systems built and discarded. [AMBIGUITIES](deliverables/AMBIGUITIES.md) and [REJECTED](deliverables/REJECTED.md) preserve the adopted decisions and actual rejected approaches.
