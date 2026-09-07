# Authorization and ledger balances

## Reference and scope

This study separates financial entries, authorization holds, and authorization decisions. It uses the rules in the [exercise](../exercise-statement.md) and a complete fictional example.

## Rule and open question

The ledger balance reflects recorded financial entries. Available balance is `ledger balance - active holds`.

Approve a new hold only when `ledger balance - active holds - requested hold >= 0`. An approved hold reduces available balance without debiting the ledger. A declined request creates no hold.

The open question concerns a later financial correction: should the authorization system only use the updated balance for new requests, or also reevaluate earlier decisions?

## Analysis

**Agreed distinction:** Think of the ledger and authorization as separate responsibilities:

* The ledger records financial entries and supplies the current ledger balance.
* Authorization subtracts active holds from that balance, evaluates the new request, and records approval or decline. Approval creates a hold; decline does not.
* The decision history explains what was approved or declined using the information available at that point.

This is a conceptual separation within the exercise's in-memory scope. It does not require separate deployed systems.

At each request's position in the supplied event order, use the balance then supplied by the ledger and the holds then active. Include financial corrections already reflected in that balance. Later records are not yet known, and unpaid interest has not entered the ledger balance.

When a backdated entry or reversal changes the ledger balance, authorization sees a new balance. Updating that input and reconsidering an earlier decision are separate actions.

**Proposal still under review:** Preserve the original decision and do not automatically reevaluate it after a balance correction. A later increase in funds would not activate a declined request. A new explicit request would be evaluated against the updated balance and active holds.

The alternative is to append a corrective decision. It would need rules for when to reevaluate, which requests to reconsider, and how to handle intervening holds or settlements. Immutability prevents changing or deleting existing records; it does not prohibit an additional corrective decision or define its effects.

Criterion 5 is conditional: if Auth-B is approved, its hold reduces available balance without changing the ledger balance. The criterion does not require Auth-B to be approved.

## Example

The [complete fictional example](../examples/06-authorization-decisions.md) starts with a settled debit of AED 10.00, a settled credit of AED 50.00, an approved hold of AED 40.00, and a declined request for AED 10.00. The ledger balance is 40.00, active holds total 40.00, and available balance is 0.00.

A separate continuation illustrates the pending proposal: reversing the debit raises the ledger balance to 50.00. With the existing 40.00 hold, available balance becomes 10.00. The reversal updates the balance; it does not itself make another authorization decision.

## Sources and limits

[Stripe Issuing, Authorizations][stripe], the initial decision flow and "Authorization updates," illustrates checks when a request is processed and states that a declined request places no hold.

Stripe's Issuing balance definitions and authorization updates describe its product, not this ledger's accounting model. The source does not determine whether this project should reevaluate decisions after backdated entries or financial reversals.

[stripe]: https://docs.stripe.com/issuing/purchases/authorizations#authorization-updates
