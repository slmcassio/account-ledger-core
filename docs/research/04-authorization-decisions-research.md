# Authorization and ledger balances

## Scope

This study separates financial entries, authorization holds, and authorization decisions within the exercise's in-memory scope.

## Authorization rule

The ledger balance reflects recorded financial entries. Available balance is `ledger balance - active holds`.

The [exercise](../exercise-statement.md) permits approval only when `ledger balance - active holds - requested hold >= 0`. An approved hold reduces available balance without debiting the ledger. A declined request creates no hold or financial debit.

Criterion 5 is conditional: if Auth-B is approved, its hold reduces available balance without changing the ledger balance. The criterion does not require approval.

## Approved responsibilities

* The ledger records financial entries and supplies the current ledger balance.
* Authorization subtracts active holds, evaluates the new request, and records approval or decline.
* The decision history explains what was approved or declined using the information available at that point.

These are conceptual responsibilities, not separate deployed systems.

Process each request in the supplied event order. Use the ledger balance and active holds known at that point, including financial corrections already recorded. Unpaid interest is not part of the ledger balance.

## Approved policy: later balance corrections

Updating the balance and reconsidering a decision are separate actions.

**Approved decision:** Preserve the original decision and do not automatically reevaluate it after a balance correction. A later increase in funds does not activate a declined request. Evaluate a new explicit request against the updated balance and active holds.

This policy keeps each decision tied to the information available when the request was processed. It is a project choice. Immutability alone does not determine whether to append a corrective decision.

## Example

The [fictional example](../examples/06-authorization-decisions.md) applies a settled debit of AED 10.00 and a settled credit of AED 50.00. A hold of AED 40.00 is approved. A later request for AED 10.00 is declined. The ledger balance and active holds are each AED 40.00, leaving AED 0.00 available.

The continuation reverses the debit and raises the ledger balance to AED 50.00. Active holds stay at AED 40.00. A new request can use the AED 10.00 now available.

## Sources and limits

[Stripe Issuing, Authorizations][stripe], the initial decision flow and "Authorization updates," illustrates checks when a request is processed and states that a declined request places no hold.

Stripe's Issuing balance definitions and authorization updates describe its product, not this ledger's accounting model. The source does not determine whether this project should reevaluate decisions after backdated entries or financial reversals.

[stripe]: https://docs.stripe.com/issuing/purchases/authorizations#authorization-updates
