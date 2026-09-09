# Acceptance criteria assessment

## Reference and scope

This study assesses the eight criteria using the rules, events and approved decisions recorded across research. Classifications distinguish arithmetic contradictions, accepted assumptions and unresolved results; they adopt no new policy.

## Rule and open question

The [exercise](../exercise-statement.md) deliberately includes incorrect criteria. Mandatory rules take precedence unless an explicit project exception is recorded. Missing definitions require a stated assumption, not an inferred industry standard. A result also needs its account, target day, processed events and fee treatment to be clear.

## Analysis

1. **Correct at the stated boundary:** before E9 and excluding fees, Day 2 principal is AED -370.00. It is not the balance after fee assessment or reversal.
2. **Unresolved for the current replay:** exactly one fee is not established while [assessment timing and the final E7 fee count remain open](06-daily-closing-research.md#decisions-still-open). The historical counterfactual below produces three; it does not establish the approved replay's totals.
3. **Supported:** [record Auth-A's confirmed AED 185.00 settlement](05-hold-lifecycle-research.md#settlement-rule), even if its local hold is missing, released or expired. If the matching reservation is still active, apply the [approved final-settlement assumption](05-hold-lifecycle-research.md#approved-policy-settlement-and-release) to end the AED 200.00 reservation and release the unused AED 15.00 without a ledger credit.
4. **Rejected under an approved assumption:** [SETTLEMENT reports a legitimate payment already settled externally](03-unmatched-settlements-research.md#the-approved-decision). Record unmatched E6; omitting its confirmed debit overstates the balance. The mandatory rules alone do not resolve criterion 4.
5. **Correct as a condition:** [an approved hold reduces availability without changing the ledger](04-authorization-decisions-research.md#authorization-rule). This does not establish that Auth-B was approved.
6. **Blanket restoration is unsupported:** [method B recalculates affected fees and interest](08-reversals-research.md#approved-decisions-and-remaining-limits), but principal, historical balances, net fees, pending interest and authorizations are distinct outcomes. Earlier records and payments remain; authorizations are not automatically reevaluated.
7. **Incorrect:** `3 * BHD 3.334 = BHD 10.002`, exceeding the supplied 10.000 credit. [Study 11](11-installments-research.md#small-example) preserves the total.
8. **Incorrect:** discarding a difference between rounded daily accruals and capitalization violates their required exact equality. [Study 09](09-daily-interest-research.md#why-the-stages-matter) distinguishes raw from rounded calculations.

## Example

For criterion 1, E1 credits AED 1,200 with Day 1 value date, E2 debits 950 on Day 1, and E7 debits 620 on Day 2. Before E9, Day 2 principal is `1,200 - 950 - 620 = -370`.

**Historical counterfactual for criterion 2:** assume E5 posts 185, E6 is rejected, and Day 5 closes before E9. Daily principal closes are `250, -370, 30, -155, -155`. Applying AED 25 fees chronologically, including earlier fees in later balances, gives closes of `250, -395, 5, -205, -230`. Fees occur on Days 2, 4, and 5.

This retained arithmetic assumes same-day fee effects and rejects E6, contrary to study 03's approved treatment. The [ordinary job runs in D for D-1](06-daily-closing-research.md#agreed-operation); ordinary assessment dates remain unresolved. These illustrative closes establish no current replay totals.

## Sources and limits

The linked studies supply the rules, examples, decisions and limits used here. The historical fee example assumes intermediate closing and recalculation without supplied close events. This assessment neither resolves pending decisions nor covers unavailable Part 2 instructions.
