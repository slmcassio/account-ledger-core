# Rejected Criteria and Approaches

Document each refused acceptance criterion with reasons, plus approaches actually abandoned during development.

## Historical Value Dates for Late Adjustments

For legitimate late transaction adjustments, rejected assigning the corrected historical days as the adjustment's `value_date`. This does not reject the separately approved [reversal fee refund exception](AMBIGUITIES.md#reversal-compensation). In the [reviewed example](../examples/04-backdated-adjustment.md), J uses Day 5 for both dates; Days 2, 3, and 4 only identify the calculation periods.

Under the approved project decision, the fee component affects the balance from its recording day. Posting that debit or credit against historical days would also change those days' ledger balances. Interest differences remain pending until the [next eligible regular payment](AMBIGUITIES.md#interest-adjustments-wait-for-payment); their dates do not create a historical ledger credit. The original delayed transaction retains its supplied historical value date.

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

## Principal Only and Current Dates for Reversal Fee Refunds

For reversals, the user selected recalculation of all affected fees and interest rather than returning only principal. Derived bases can change, so leaving every previously calculated amount untouched was not selected.

Method A in [study 08](../research/08-reversals-research.md) books and values fee refunds on the correction day. It was not adopted because those fees would remain in earlier balances. Approved method B keeps current booking but offsets each fee at its original charge's value date. The late transaction adjustment policy above remains unchanged outside this exception.

Neither decision backdates interest payments: all interest differences retain current correction dates and wait for an eligible regular payment. This record does not establish criterion 6's blanket restoration or final replay totals; the numerical dependencies on studies 09 and 10 remain accepted as open.

TODO: Record refused acceptance criteria and further abandoned approaches as they are reviewed.
