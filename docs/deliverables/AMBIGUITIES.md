# Ambiguities

## Rounding Mode

**Decision: use HALF_UP, with two decimal places for AED and three for BHD.** Round to the nearest representable amount, with exact ties away from zero.

**Rationale and assumption:** favoring the recipient of positive interest at exact ties is the desired project policy. HALF_UP gives that recipient the higher amount. We accept the upward bias at those ties; HALF_EVEN can reduce that bias. This rationale does not imply a customer benefit for fees or negative adjustments.

The exercise specifies currency precision but leaves the rounding mode undefined. HALF_UP is a project choice, not a mandatory rule established for these accounts by the sources below. Currency alone does not establish the applicable jurisdiction or contract.

**Still unresolved:** Intermediate calculation precision and any additional rounding stages. The exercise already requires the rounded daily interest accruals to sum exactly to the capitalized total.

### Supporting Sources and Limits

* [Emirates NBD AT1 prospectus](https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf), section 5.1, printed page 45 (PDF page 56): interest for periods shorter than a full interest period on these USD securities uses the nearest cent, with positive exact ties rounded upwards. This is a precedent within that product's scope.
* [Government of Bahrain GMTN offering circular](https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf), section 5.1, printed page 44 (PDF page 58): interest on fixed rate notes uses the nearest currency subunit, with exact halves upwards, subject to another applicable market convention. This is a contractual securities rule.
* [Mambu deposit interest documentation](https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/): distinguishes calculation precision, storage precision, and rounding of aggregated journal entries. It does not specify HALF_UP or HALF_EVEN and does not justify the selected mode.

## Booking and Value Dates

**Decision:** `booking_date` is the accounting recording day, supplied as "Booked". `value_date` is the day from which the transaction affects the balance. Neither necessarily identifies when the event occurred or arrived. Preserve the supplied event order and dates.

**Rationale:** separate when a transaction is recorded from when it has financial effect. A backdated transaction can change a historical balance without changing an existing record. Queries limited to earlier records reproduce the earlier view.

## Late Transaction Adjustments

**Scope:** legitimate transactions delivered after they occurred, such as an official transaction received from Mastercard later. Events resulting from system errors and corrections of those errors are outside this discussion's scope.

**Decision:** append the original transaction with its supplied dates. Recalculate affected fees and interest, then append a separate adjustment linked to that transaction. For each component, record `corrected amount - net amount already recorded`; do not repeat the original transaction amount.

The adjustment uses the actual correction day for both `booking_date` and `value_date`. Keep the historical days and each fee or interest component in its breakdown. Fee differences affect the ledger; all interest differences follow the [pending interest decision](#interest-adjustments-wait-for-payment), including corrections for previously paid periods.

**Assumption and rationale:** interpret the fee's "day assessed" as the day the correction is assessed and recorded. This makes the fee component affect the current balance while preserving the original records and the explanation of each historical difference. In the approved example, J has both dates on Day 5; Days 2, 3, and 4 are calculation references.

**Basis and limits:** [the research](../research/02-booking-and-value-dates-research.md) records ISO date definitions, Mambu's backdating and reversal examples, and their limits. Difference adjustments and their dates are project choices. The cited CBUAE provisions address error correction and do not establish this policy.

The [fictional example](../examples/04-backdated-adjustment.md) illustrates the approved decision.

## Interest Adjustments Wait for Payment

**Decision:** Keep every interest correction pending until the next regular payment whose booking cutoff includes it, including corrections for previously paid periods. Do not settle an overdue portion immediately. Positive differences increase pending interest; negative differences reduce it.

**Calculation and dates:** Compare each corrected daily amount with its original accrual plus all earlier adjustments, including paid adjustments. Append only a nonzero difference, linked to the transaction with a breakdown by historical day. Both adjustment dates are the actual correction day. The principal retains its supplied dates and affects the ledger normally; the interest adjustment does not change ledger or available balance then.

**Payment:** Capitalize eligible unpaid daily accruals and adjustments exactly once. Record which components the payment settles and derive what remains unpaid from those links, preserving earlier records and payments. Later calculations still count settled components when finding a new difference. Never add a full corrected target plus its adjustment or repeat the principal.

**Assumption and rationale:** Accept deferred settlement even for previously paid periods, using one pending treatment independent of processing order. Pending amounts are unavailable and earn no interest, including hypothetical returns from an earlier capitalization date. This replaces the earlier immediate balance correction for previously credited interest. The difference method and dates remain; fee adjustments still debit or credit the ledger on the correction day.

**Example:** A transaction is booked and corrected on D30 with value date D5. Assume three daily amounts each change from AED 1.50 to 2.00: D5 was paid on D15; D20 and D25 remain unpaid. These are illustrative calculation results; other days are omitted.

* D30: record one pending adjustment of `3 * (2.00 - 1.50) = 1.50`, with both dates D30 and a breakdown of 0.50 per day.
* Next eligible payment: these components contribute `3.00 + 1.50 = 4.50`, comprising two unpaid original accruals and the whole adjustment. D5's original 1.50 is not paid again.
* Repeating for each day, before or after payment: `2.00 - (1.50 + 0.50) = 0.00`. Paid adjustments still count when calculating the difference.

The next day 15 is illustrative, not an approved monthly calendar. No corrective credit is backdated to D15 or D5.

**Limits:** Settlement of a negative total payable remains unresolved; no direct debit or carry rule is adopted. Study 08 still determines E9's compensation scope. The [daily cutoff and timing questions](#daily-calculation-timing) continue to apply.

## Settlements with a Missing Authorization

**Question:** Does SETTLEMENT request a payment, or report a payment that has already settled? The exercise does not define this precisely.

**Decision and assumption:** Treat SETTLEMENT as a legitimate payment already settled outside the ledger. If the local authorization is missing, append the debit with the supplied dates, preserve the authorization reference, and report the missing match. Do not invent an authorization or hold. Preserve the event order and existing records.

**Rationale:** The ledger must reflect the confirmed payment. Leaving out the debit would overstate the account balance. Under this assumption, E6 debits AED 180.00 on Day 4 and reports that Auth-Z was not found. This is why the project rejects acceptance criterion 4.

The [research](../research/03-unmatched-settlements-research.md) explains the alternatives and the Stripe precedent. Stripe documents such payments, but does not determine the exercise's policy. The [fictional example](../examples/05-unmatched-settlement.md) illustrates the decision. Corrections of system errors are outside this decision's scope.

## Authorization and Ledger Responsibilities

**Decision:** Separate the ledger's financial entries, active authorization holds, and the history of authorization decisions. Authorization uses the ledger balance and holds known when a request is processed. A declined request creates no hold or financial debit.

**Rationale and scope:** This distinction explains why a hold changes available balance without changing ledger balance. It is a conceptual separation within the in-memory exercise, not a requirement for separate deployed systems. Updating the balance after a financial correction and reconsidering an earlier authorization are separate actions.

**Decision after balance corrections:** Preserve the original authorization decision. Do not automatically reevaluate it when the balance changes. A later increase in funds does not activate a declined request. Evaluate a new explicit request against the updated balance and active holds.

**Rationale:** Each decision reflects the information available when the request was processed. The exercise does not specify automatic reevaluation, so this is an approved project choice. The [example](../examples/06-authorization-decisions.md) applies this policy after a reversal.

## Hold Settlement and Release

**Question:** Does a settlement below the held amount finish the reservation or leave the unused amount reserved?

**Decision and assumption:** Treat Auth-A's AED 185.00 settlement as final. Append the actual debit with the supplied Day 4 dates and end its AED 200.00 reservation. The unused AED 15.00 becomes available without a ledger credit.

**Rationale:** Final settlement is an explicit simplifying assumption for this scenario. Neither the lower amount nor the absence of another settlement proves finality. A partial, non-final settlement would instead leave the unused amount reserved.

Separate the financial payment from its effect on authorization. A non-final settlement reduces the matching active reservation by the settled portion; a final settlement also releases the remainder. A release without settlement frees the specified reserved amount without a ledger debit or credit. Preserve the original authorization and decision records and append information explaining the reservation changes.

An absent, released, or expired hold does not prevent recording a legitimate payment already settled externally. Any reservation matching problem is separate from that debit. Under the approved settlement interpretation, criterion 3 is supported because E5 reports such a payment; it does not determine finality.

The [research](../research/05-hold-lifecycle-research.md) explains the Mambu, Stripe, Mastercard, and Visa precedents and their limits. The [fictional example](../examples/07-hold-lifecycle.md) compares the reservation effects. API structure and network integration design remain outside this decision.

## Hold Expiration During the Replay

**Decision and assumption:** Generate no automatic hold expiration during Days 1 through 6. The exercise provides neither an expiration policy nor a deadline. The six days define the scenario window, not a hold's lifetime.

**Rationale:** Avoid introducing an unsupported expiration event into the supplied replay. This does not establish that holds never expire. A general policy would need a duration or deadline, a time reference, and rules for relevant updates; behavior beyond the window remains undefined.

Auth-B has a hold only if its request is approved. The absence of settlement alone does not prove that a reservation exists. The [approved authorization policy](#authorization-and-ledger-responsibilities) preserves earlier decisions after balance corrections and requires a new explicit request for another evaluation.

## Daily Calculation Timing

**Decision and assumption:** Process events in the supplied order and update the running balance as financial transactions are recorded. Run one daily job in D for reference day D-1. Select its input entries cumulatively by `booking_date <= D-1`, then use their value dates for the days being calculated. Knowing a later booking does not make it eligible. Pending interest is separate from the ledger balance.

**Rationale:** The booking cutoff fixes the accounting input considered by each calculation, independently of execution order. Value dates retain their economic meaning. A cutoff does not guarantee that every eligible event has arrived; handling missing eligible records, including E10 after an earlier job, remains unresolved.

**Payment choice and limit:** The Day 6 payment uses reference Day 5. E9 and adjustments booked on Day 6 cannot change it, even if processed before that payment. The job's resulting accrual and payment are outputs, not input transactions excluded by the cutoff. The payment is recorded on Day 6. The Day 7 routine calculates Day 6; under this interpretation, its interest awaits a later payment whose details remain for study 10. The exercise still requires one credit at the end of Day 6; no weekly or monthly calendar is inferred.

**Still proposed:** The midnight boundary, 00:30 start, validation and indivisible recording, and replay checkpoints in [study 06](../research/06-daily-closing-research.md). The business time zone and ordinary assessment dates remain undecided. Validation must concern inputs and results relevant to the cutoff. Reversal scope and a negative total payable remain unresolved. The [earlier fictional example](../examples/08-daily-closing.md) awaits alignment with the booking cutoff and does not establish current results.

**Review status:** Study 06 is approved for now, with its recorded open items and dependencies explicitly pending. Revisit it when a later study affects these decisions.

## Overdraft Fee Assessment Base

**Decision:** For the daily job in D, first select inputs cumulatively with `booking_date <= D-1`. For each period being evaluated, obtain its ledger balance from those inputs using their applicable value dates. Exclude only the fee components attributed to that period and their adjustments already included in the balance. Keep other periods' fees and refunds according to their actual value dates. A negative assessment base requires AED 25.00; a zero or positive base requires no fee. Holds and pending interest do not enter the base.

**Assumption and rationale:** A fee should not sustain its own assessment after a legitimate late credit removes the original deficit. Excluding only its own period components prevents that circular result without removing other periods' dated charges or refunds. This is an approved project interpretation of the exercise, not an explicit rule in its statement.

**Effect and reconciliation:** The reported ledger balance continues to include every eligible financial entry with an applicable value date. The exclusion is only for fee assessment. Compare the corrected fee target with the original charge plus all earlier fee adjustments; append only the difference. Both adjustment dates remain the actual correction day. Reviewing periods in date order does not move a new charge or refund into an earlier balance. See [study 07](../research/07-overdraft-fees-research.md) for examples.

**Accepted limits:** Study 06 remains approved with its unresolved replay checkpoints and ordinary assessment dates; its commit did not establish a final fee count after E7. Preserve that limit when concluding this study's independent assessment decision. Reversal compensation remains for study 08, capitalization order for study 10, and the negative BHD case for study 12. Keeping these dependencies open does not adopt their proposals or determine the final Day 6 fee.
