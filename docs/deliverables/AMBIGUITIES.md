# Ambiguities

## Rounding Mode

**Decision: use HALF_UP, with two decimal places for AED and three for BHD.** Round to the nearest representable amount, with exact ties away from zero.

**Rationale and assumption:** favoring the recipient of positive interest at exact ties is the desired project policy. HALF_UP gives that recipient the higher amount. We accept the upward bias at those ties; HALF_EVEN can reduce that bias. This rationale does not imply a customer benefit for fees or negative adjustments.

The exercise specifies currency precision but leaves the rounding mode undefined. HALF_UP is a project choice, not a mandatory rule established for these accounts by the sources below. Currency alone does not establish the applicable jurisdiction or contract.

For daily interest, the [calculation decision](#daily-interest-calculation) now defines exact multiplication and one daily rounding stage. It does not establish intermediate precision or rounding stages for other calculations.

### Supporting Sources and Limits

* [Emirates NBD AT1 prospectus](https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf), section 5.1, printed page 45 (PDF page 56): interest for periods shorter than a full interest period on these USD securities uses the nearest cent, with positive exact ties rounded upwards. This is a precedent within that product's scope.
* [Government of Bahrain GMTN offering circular](https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf), section 5.1, printed page 44 (PDF page 58): interest on fixed rate notes uses the nearest currency subunit, with exact halves upwards, subject to another applicable market convention. This is a contractual securities rule.
* [Mambu deposit interest documentation](https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/): distinguishes calculation precision, storage precision, and rounding of aggregated journal entries. It does not specify HALF_UP or HALF_EVEN and does not justify the selected mode.

## Daily Interest Calculation

**Decision:** For each account and day, calculate `HALF_UP(max(daily_base, 0) * 0.0004, currency_precision)`. Preserve the exact product until that single rounding to AED two places or BHD three. Do not round an intermediate product or carry fractions between days.

**Assumption and rationale:** Each daily amount depends only on its own base, so it can be reproduced independently. This is an approved project choice for daily interest; the rate, monetary precisions and HALF_UP mode were already established. It selects no language or arithmetic library.

**Reconciliation:** Compare the revised rounded daily target with the original accrual plus all earlier adjustments, including paid ones. Subtract those monetary amounts exactly; do not round the raw interest difference. At payment, sum only eligible unpaid accruals and adjustments, settling each once. Never replace that sum with rounded aggregated raw interest or discard a reconciliation difference. Corrections retain the [pending treatment and actual dates](#interest-adjustments-wait-for-payment).

The [small examples](../research/09-daily-interest-research.md#why-the-stages-matter) show why intermediate rounding, rounding after aggregation and rounding the raw difference can change the result.

**Limits:** This settles daily calculation precision and stages, not the historical bases or final replay totals. Study 06's checkpoints, ordinary assessment dates, missing eligible inputs and final E7 fee count remain open. [Study 10](../research/10-interest-capitalization-research.md) defines payment dates and the monthly schedule; calendar mapping, missing eligible inputs and handling a negative total remain open. The approved booking cutoff and reversal refund dates are unchanged.

## Interest Payment Schedule and Capitalization

**Decision:** Book and value interest credits on the actual payment day. They join later bases when booking and value dates permit, like ordinary inflows. A Day 6 payment affects Day 6 bases calculated on Day 7, not Day 5 interest calculated on Day 6.

**Decision and assumption:** Pay monthly on the first business day for the previous month. This is a deliberate exercise choice. The ordinary accrual period is separate from the cumulative `booking_date <= D-1` boundary. Ordinary accruals for the current month remain pending; eligible unpaid adjustments can concern older paid periods. The job's newly calculated ordinary accrual can participate in payment for its month; a correction booked on payment day remains excluded.

**Rationale:** Paying the previous month's interest together gives a clear settlement period. Using the actual payment date lets paid interest affect subsequent balances without treating pending interest as previously paid.

**Limits:** No business day calendar or mapping of synthetic Days 1 through 6 to actual months is supplied. Preserve the required Day 6 credit; its relationship to the monthly schedule and the later payment date for Day 6 interest remain unresolved. Missing eligible inputs, ordinary fee dates and negative totals remain open. See [study 10](../research/10-interest-capitalization-research.md).

## Booking and Value Dates

**Decision:** `booking_date` is the accounting recording day, supplied as "Booked". `value_date` is the day from which the transaction affects the balance. Neither necessarily identifies when the event occurred or arrived. Preserve the supplied event order and dates.

**Rationale:** separate when a transaction is recorded from when it has financial effect. A backdated transaction can change a historical balance without changing an existing record. Queries limited to earlier records reproduce the earlier view.

**Receipt metadata:** Record when the system actually receives each event separately from its supplied booking and value dates. This distinguishes receipt from accounting eligibility: earlier receipt does not admit a future booking, and an eligible booking may arrive late. Preserve the supplied dates and event order; invent no receipt moments for the replay. Timestamp precision, time zone and tie handling remain unspecified. This metadata does not resolve study 06's missing input or checkpoint policy.

## Late Transaction Adjustments

**Scope:** legitimate transactions delivered after they occurred, such as an official transaction received from Mastercard later. Events resulting from system errors and corrections of those errors are outside this discussion's scope.

**Decision:** append the original transaction with its supplied dates. Recalculate affected fees and interest, then append a separate adjustment linked to that transaction. For each component, record `corrected amount - net amount already recorded`; do not repeat the original transaction amount.

For these late transaction adjustments, use the actual correction day for both `booking_date` and `value_date`. The separate [reversal refund decision](#reversal-compensation) is a limited exception. Keep the historical days and each fee or interest component in its breakdown. Fee differences affect the ledger; all interest differences follow the [pending interest decision](#interest-adjustments-wait-for-payment), including corrections for previously paid periods.

**Assumption and rationale:** interpret the fee's "day assessed" as the day the correction is assessed and recorded. This makes the fee component affect the current balance while preserving the original records and the explanation of each historical difference. In the approved example, J has both dates on Day 5; Days 2, 3, and 4 are calculation references.

**Basis and limits:** [the research](../research/02-booking-and-value-dates-research.md) records ISO date definitions, Mambu's backdating and reversal examples, and their limits. Difference adjustments and their dates are project choices. The cited CBUAE provisions address error correction and do not establish this policy.

The [fictional example](../examples/04-backdated-adjustment.md) illustrates the approved decision.

## Interest Adjustments Wait for Payment

**Decision:** Keep every interest correction pending until the next regular payment whose booking cutoff includes it, including corrections for previously paid periods. Do not settle an overdue portion immediately. Positive differences increase pending interest; negative differences reduce it.

**Calculation and dates:** Compare each corrected [rounded daily target](#daily-interest-calculation) with its original accrual plus all earlier adjustments, including paid adjustments. Append only a nonzero difference, linked to the transaction with a breakdown by historical day. Both adjustment dates are the actual correction day. The principal retains its supplied dates and affects the ledger normally; the interest adjustment does not change ledger or available balance then.

**Payment:** Capitalize eligible unpaid daily accruals and adjustments exactly once. Record which components the payment settles and derive what remains unpaid from those links, preserving earlier records and payments. Later calculations still count settled components when finding a new difference. Never add a full corrected target plus its adjustment or repeat the principal.

**Assumption and rationale:** Accept deferred settlement even for previously paid periods, using one pending treatment independent of processing order. Pending amounts are unavailable and earn no interest, including hypothetical returns from an earlier capitalization date. This replaces the earlier immediate balance correction for previously credited interest. The difference method and dates remain; fee adjustments still debit or credit the ledger on the correction day.

**Product assumption and limits:** This is a pragmatic project choice, not a conclusion that regulation requires this method. A real provider's rate and terms may face regulatory constraints; their applicability here has not been established. The exercise still supplies 0.04% per day. See the [rationale and source limits](../research/10-interest-capitalization-research.md#why-pending-interest-earns-nothing).

**Example:** A transaction is booked and corrected on D30 with value date D5. Assume three daily amounts each change from AED 1.50 to 2.00: D5 was paid on D15; D20 and D25 remain unpaid. These are illustrative calculation results; other days are omitted.

* D30: record one pending adjustment of `3 * (2.00 - 1.50) = 1.50`, with both dates D30 and a breakdown of 0.50 per day.
* Next eligible payment: these components contribute `3.00 + 1.50 = 4.50`, comprising two unpaid original accruals and the whole adjustment. D5's original 1.50 is not paid again.
* Repeating for each day, before or after payment: `2.00 - (1.50 + 0.50) = 0.00`. Paid adjustments still count when calculating the difference.

The day 15 payment is illustrative and does not define the [approved monthly schedule](#interest-payment-schedule-and-capitalization). No corrective credit is backdated to D15 or D5.

**Limits:** Settlement of a negative total payable remains unresolved; no direct debit or carry rule is adopted. The [reversal decision](#reversal-compensation) applies the same pending treatment to E9's affected interest. The [daily cutoff and timing questions](#daily-calculation-timing) continue to apply.

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

**Payment choice and limit:** The Day 6 payment uses reference Day 5. E9 and adjustments booked on Day 6 cannot change it, even if processed before that payment. The job's resulting accrual and payment are outputs, not input transactions excluded by the cutoff. The payment is recorded and valued on Day 6. The Day 7 routine calculates Day 6, whose interest awaits later payment. The [monthly schedule](#interest-payment-schedule-and-capitalization) is now approved, but its calendar mapping to the required Day 6 credit and that later payment remains unresolved.

**Still proposed:** The midnight boundary, 00:30 start, validation and indivisible recording, and replay checkpoints in [study 06](../research/06-daily-closing-research.md). The business time zone and ordinary assessment dates remain undecided. Validation must concern inputs and results relevant to the cutoff. A negative total payable remains unresolved; [reversal compensation](#reversal-compensation) is now defined. The [earlier fictional example](../examples/08-daily-closing.md) awaits alignment with the booking cutoff and does not establish current results.

**Review status:** Study 06 is approved for now, with its recorded open items and dependencies explicitly pending. Revisit it when a later study affects these decisions.

## Overdraft Fee Assessment Base

**Decision:** For the daily job in D, first select inputs cumulatively with `booking_date <= D-1`. For each period being evaluated, obtain its ledger balance from those inputs using their applicable value dates. Exclude only the fee components attributed to that period and their adjustments already included in the balance. Keep other periods' fees and refunds according to their actual value dates. A negative assessment base uses the [configured fee in the account's currency](#overdraft-fee-currency); a zero or positive base requires no fee. Holds and pending interest do not enter the base.

**Assumption and rationale:** A fee should not sustain its own assessment after a legitimate late credit removes the original deficit. Excluding only its own period components prevents that circular result without removing other periods' dated charges or refunds. This is an approved project interpretation of the exercise, not an explicit rule in its statement.

**Effect and reconciliation:** The reported ledger balance continues to include every eligible financial entry with an applicable value date. The exclusion is only for fee assessment. Compare the corrected fee target with the original charge plus all earlier fee adjustments; append only the difference. For legitimate late transactions, both adjustment dates remain the actual correction day. Reviewing periods in date order does not itself backdate them. [Reversal fee refunds](#reversal-compensation) instead retain the original charge's value date. See [study 07](../research/07-overdraft-fees-research.md) for examples.

**Accepted limits:** Study 06 remains approved with its unresolved replay checkpoints and ordinary assessment dates; its commit did not establish a final fee count after E7. Preserve that limit when concluding this study's independent assessment decision. Study 08 defines reversal compensation without resolving these timing and numerical limits. [Study 10](../research/10-interest-capitalization-research.md) now defines the payment date and explains its effect on later bases. The [currency decision](#overdraft-fee-currency) defines the fee for the supplied account types. These decisions do not determine the final Day 6 fee.

## Overdraft Fee Currency

**Requirement and exception:** The exercise requires AED 25.00 per account per negative closing day without exempting BHD accounts. The user explicitly chose a different treatment: configure the daily fee by account type in the account's own currency, with AED 25.00 for the type corresponding to ACC-001 and BHD 0.000 for the type corresponding to ACC-002.

**Assumption and rationale:** Accept this departure from the literal mandatory rule for simplicity because no conversion requirements are supplied. The zero BHD fee is a project choice, not a consequence of monetary precision or HALF_UP.

**Limits:** A negative BHD base produces the configured zero fee; it does not become nonnegative. Authorization still requires available balance after a new hold to remain at or above zero in the account's currency, and legitimate confirmed debits still enter the ledger. No foreign exchange, separate AED obligation, additional account types or general prohibition on negative balances is adopted. [Study 12](../research/12-fee-currency-research.md) separates the supplied principal example from final balances with interest.

## Reversal Compensation

**Scope and decision:** Every correction in this exercise concerns a legitimate transaction; system error correction is excluded as an explicit simplification. Reverse principal once with its supplied dates. Recalculate all affected fees and daily interest from the affected value day onward, including later periods whose bases change, within the applicable calculation boundary. Compare each corrected component with its original amount plus all earlier adjustments, including paid ones; append only the difference, linked to the reversal and affected components.

**Fee refund dates:** Book each refund caused by reversal on the actual correction day, with value on the original charge's value date. This is method B in [study 08](../research/08-reversals-research.md). It is a limited exception to the current dating of [late transaction adjustments](#late-transaction-adjustments). A fee for balance day H is offset at its original charge's value date, which need not be H or the principal's value date.

**Rationale:** Neutralize the reversed transaction's affected fee consequences at their economic dates while retaining when funds were actually returned. Principal only and current value dates for these refunds were not selected. Preserve every record, the approved fee assessment base and HALF_UP; do not automatically reevaluate authorizations.

**Interest and cutoff:** Interest differences retain both dates on the actual correction day and remain pending until an eligible regular payment, even for previously paid periods. Preserve actual payments, count paid adjustments when finding differences, and settle each pending component once. No hypothetical capitalization or interest on pending amounts is introduced. The job in D still requires `booking_date <= D-1`; E9 and adjustments booked on Day 6 cannot alter the Day 6 payment referencing Day 5.

**Accepted limits:** Study 08 is approved with study 06's checkpoints, ordinary assessment dates, missing eligible inputs and final E7 fee count still open. Study 09 now defines the [daily calculation stages](#daily-interest-calculation); exact replay interest still depends on the unresolved bases. [Study 10](../research/10-interest-capitalization-research.md) now defines payment dates and the monthly schedule; calendar mapping, capitalization amounts and final balances remain open. No negative total payable policy is adopted. Restoring principal, historical balances, net fees and interest are distinct claims; the illustration does not establish criterion 6's blanket restoration or current replay totals.

## E10 Installment Allocation

**Requirement:** Preserve E10's BHD 10.000 credit exactly at BHD's three decimal places. Three equal stored amounts are impossible; conservation does not determine which installment receives the remaining BHD 0.001.

**Decision, assumption and rationale:** Assign that remainder to installment 3, giving BHD 3.333, 3.333 and 3.334. A fixed final position makes the allocation reproducible and lets the last installment complete the original total. This is an approved convention for E10; assigning it to installment 1 or 2 would also conserve the credit.

All installments belong to ACC-002 with booking and value dates on Day 5. Preserve E10 after E9; no installment calendar is introduced. If installments are individual financial credits, link them to E10 and do not credit its full amount again. See [study 11](../research/11-installments-research.md) for the calculation and source limits.
