# Rejected Criteria and Approaches

Document each refused acceptance criterion with reasons, plus approaches actually abandoned during development.

## Historical Value Dates for Late Adjustments

Rejected assigning the corrected historical days as an adjustment's `value_date`. In the [reviewed example](../examples/04-backdated-adjustment.md), J uses Day 5 for both dates; Days 2, 3, and 4 only identify the calculation periods.

Under the approved project decision, the adjustment affects the balance from its recording day. Posting it against historical days would also change those days' ledger balances. The original delayed transaction retains its supplied historical value date.

## Acceptance Criterion 4

**Rejected:** refusing a settlement solely because its authorization ID is missing from the ledger, without recording the debit.

The [approved interpretation](AMBIGUITIES.md#settlements-with-a-missing-authorization) treats SETTLEMENT as a legitimate payment already settled outside the ledger. The debit must therefore be recorded, with the missing authorization reported separately. This rejection rests on that project assumption.

In the [example](../examples/05-unmatched-settlement.md), rejecting the settlement would leave AED 500.00 instead of AED 320.00, overstating the balance by AED 180.00.

## Inferring Finality from Missing Later Settlements

Rejected the original hold report's justification that Auth-A's settlement is final because the scenario supplies no later capture. The absence of another settlement does not establish whether the remaining reservation should stay active.

The [approved decision](AMBIGUITIES.md#hold-settlement-and-release) treats Auth-A as final through an explicit scenario assumption. Partial, non-final settlement remains a valid alternative behavior; it is not rejected as a general approach.

## Waiting for the Complete Input Before Calculating

Rejected deferring every calculation until the entire event stream has arrived. The user chose an active system that calculates during event processing. Such a system continues receiving transactions and has no final input event to wait for.

The [research](../research/06-daily-closing-research.md) keeps daily closing and later recalculation separate. A final report after the finite replay remains possible; the precise closing schedule is still proposed.

TODO: Record refused acceptance criteria and further abandoned approaches as they are reviewed.
