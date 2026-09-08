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

Neither decision backdates interest payments: all interest differences retain current correction dates and wait for an eligible regular payment. This record does not establish criterion 6's blanket restoration or final replay totals; daily calculation stages are now defined in study 09, while the unresolved bases and study 10 payment dependencies remain open.

## Acceptance Criterion 8

**Rejected:** discarding a remainder when rounded daily accruals differ from the capitalized total. The exercise requires those amounts to agree exactly.

For two eligible unpaid days at AED 465.00, each day's raw interest is `465.00 * 0.0004 = 0.186000`, rounded to 0.19. Their payment is `0.19 + 0.19 = 0.38`. Rounding the raw sum `0.372000` to 0.37 and discarding 0.01 violates that requirement. Corrections follow the same reconciliation: pay the eligible unpaid monetary components exactly once.

## Extra Daily Interest Rounding and Fraction Carry

The [approved daily calculation](AMBIGUITIES.md#daily-interest-calculation) rejects intermediate rounding and carrying fractions between days. Each exact product is rounded once to its currency precision, keeping its daily result independent of other days.

Rounding after aggregating raw interest does not replace the required sum of daily amounts. Likewise, a correction compares rounded daily targets rather than rounding their raw difference. The [small examples](../research/09-daily-interest-research.md#corrections-and-payment) show why that alternative can miss a monetary adjustment. This rejection concerns daily interest, not a policy for settling a negative payment total.

TODO: Record refused acceptance criteria and further abandoned approaches as they are reviewed.
