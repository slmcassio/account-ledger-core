# Ambiguities

## Rounding Mode

**Decision: use HALF_UP, with two decimal places for AED and three for BHD.** Round to the nearest representable amount, with exact ties away from zero.

**Rationale and assumption:** favoring the recipient of positive interest at exact ties is the desired project policy. HALF_UP gives that recipient the higher amount. We accept the upward bias at those ties; HALF_EVEN can reduce that bias. This rationale does not imply a customer benefit for fees or negative adjustments.

The exercise specifies currency precision but leaves the rounding mode undefined. HALF_UP is a project choice, not a mandatory rule established for these accounts by the sources below. Currency alone does not establish the applicable jurisdiction or contract.

**Scope:** The [daily interest decision](#daily-interest-calculation) defines its calculation precision and rounding stages. Intermediate precision and stages outside daily interest remain unresolved. The exercise already requires the rounded daily interest accruals to sum exactly to the capitalized total.

### Supporting Sources and Limits

* [Emirates NBD AT1 prospectus](https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf), section 5.1, printed page 45 (PDF page 56): interest for periods shorter than a full interest period on these USD securities uses the nearest cent, with positive exact ties rounded upwards. This is a precedent within that product's scope.
* [Government of Bahrain GMTN offering circular](https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf), section 5.1, printed page 44 (PDF page 58): interest on fixed rate notes uses the nearest currency subunit, with exact halves upwards, subject to another applicable market convention. This is a contractual securities rule.
* [Mambu deposit interest documentation](https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/): distinguishes calculation precision, storage precision, and rounding of aggregated journal entries. It does not specify HALF_UP or HALF_EVEN and does not justify the selected mode.

## Daily Interest Calculation

**Decision:** For each account and day, calculate `HALF_UP(max(daily_base, 0) * 0.0004, currency_precision)`. Preserve the exact product until that single rounding to AED two places or BHD three. Do not round an intermediate product or carry fractions between days.

**Assumption and rationale:** Each daily amount depends only on its own base, so it can be reproduced independently. This is an approved project choice for daily interest; the rate, monetary precisions and HALF_UP mode were already established. It selects no language or arithmetic library.

**Reconciliation:** Compare the revised rounded daily target with the original accrual plus all earlier adjustments, including paid ones. Subtract those monetary amounts exactly; do not round the raw interest difference. At payment, sum only eligible unpaid accruals and adjustments, settling each once. Never replace that sum with rounded aggregated raw interest or discard a reconciliation difference. Corrections retain the [pending treatment and actual dates](#interest-adjustments-wait-for-payment).

The [small examples](../research/09-daily-interest-research.md#why-the-stages-matter) show why intermediate rounding, rounding after aggregation and rounding the raw difference can change the result.

**Limits:** This settles daily calculation precision and stages, not historical bases or final replay totals. General checkpoints, missing eligible inputs, the actual business day calendar and the zero-settlement protocol remain [pending](#pending-calculation-decisions). The approved booking cutoff, payment schedule and reversal refund dates remain unchanged.

## Booking and Value Dates

**Decision:** `booking_date` is the accounting recording day, supplied as "Booked". `value_date` is the day from which the transaction affects the balance. Neither necessarily identifies when the event occurred or arrived. Preserve the supplied event order and dates.

**Rationale:** separate when a transaction is recorded from when it has financial effect. A backdated transaction can change a historical balance without changing an existing record. Queries limited to earlier records reproduce the earlier view.

## Late Transaction Adjustments

**Scope:** legitimate transactions delivered after they occurred, such as an official transaction received from Mastercard later. Events resulting from system errors and corrections of those errors are outside this discussion's scope.

**Decision:** append the original transaction with its supplied dates. Recalculate affected fees and interest, then append a separate adjustment linked to that transaction. For each component, record `corrected amount - net amount already recorded`; do not repeat the original transaction amount.

For these late transaction adjustments, use the actual correction day for both `booking_date` and `value_date`. The separate [reversal refund decision](#reversal-compensation) is a limited exception. Keep the historical days and each fee or interest component in its breakdown. Fee differences affect the ledger; all interest differences follow the [pending interest decision](#interest-adjustments-wait-for-payment), including corrections for previously paid periods.

**Assumption and rationale:** interpret the fee's "day assessed" as the day the correction is assessed and recorded. This makes the fee component affect the current balance while preserving the original records and the explanation of each historical difference. In the fictional example, J has both dates on its Day 6 correction checkpoint; Days 2, 3 and 4 are calculation references.

**Basis and limits:** [the research](../research/02-booking-and-value-dates-research.md) records ISO date definitions, Mambu's backdating and reversal examples, and their limits. Difference adjustments and their dates are project choices. The cited CBUAE provisions address error correction and do not establish this policy.

The [fictional example](../examples/04-backdated-adjustment.md) illustrates the approved decision.

## Interest Adjustments Wait for Payment

**Decision:** Keep every interest correction pending until the next regular payment whose booking cutoff includes it, including corrections for previously paid periods. Do not settle an overdue portion immediately. Positive differences increase pending interest; negative differences reduce it.

**Calculation and dates:** Compare each corrected [rounded daily target](#daily-interest-calculation) with its original accrual plus all earlier adjustments, including paid adjustments. Append only a nonzero difference, linked to the transaction with a breakdown by historical day. Both adjustment dates are the actual correction day. The principal retains its supplied dates and affects the ledger normally; the interest adjustment does not change ledger or available balance then.

**Payment:** Capitalize eligible unpaid daily accruals and adjustments exactly once. Record which components the payment settles and derive what remains unpaid from those links, preserving earlier records and payments. Later calculations still count settled components when finding a new difference. Never add a full corrected target plus its adjustment or repeat the principal.

**Assumption and rationale:** Accept deferred settlement even for previously paid periods, using one pending treatment independent of processing order. Pending amounts are unavailable and earn no interest, including hypothetical returns from an earlier capitalization date. This replaces the earlier immediate balance correction for previously credited interest. The difference method and interest adjustment dates remain. Fee adjustments change current funds when recorded; [reversal fee refunds](#reversal-compensation) also affect reconstructed balances from the original charge's value date.

**Example:** A transaction is booked and corrected on D30 with value date D5. Assume three daily amounts each change from AED 1.50 to 2.00: D5 was paid on D15; D20 and D25 remain unpaid. These are illustrative calculation results; other days are omitted.

* D30: record one pending adjustment of `3 * (2.00 - 1.50) = 1.50`, with both dates D30 and a breakdown of 0.50 per day.
* Next eligible payment: these components contribute `3.00 + 1.50 = 4.50`, comprising two unpaid original accruals and the whole adjustment. D5's original 1.50 is not paid again.
* Repeating for each day, before or after payment: `2.00 - (1.50 + 0.50) = 0.00`. Paid adjustments still count when calculating the difference.

D15 is an illustrative earlier payment date; it does not define the [approved monthly schedule](#interest-payment-schedule-and-capitalization). No corrective credit is backdated to D15 or D5.

**Settlement:** Apply the [approved sign rule](#interest-payment-schedule-and-capitalization) to the eligible total. The [reversal decision](#reversal-compensation) applies the same pending treatment to E9's affected interest. The [daily cutoff and timing questions](#daily-calculation-timing) continue to apply.

## Interest Payment Schedule and Capitalization

**Decision:** At the end of Day 6, settle each account's eligible unpaid interest total separately in AED or BHD. A positive total credits the account; a negative total debits it, even into a negative balance. A zero total settles the components without a financial movement. Settle each component once and do not carry an eligible negative total forward.

**Assumption and rationale:** This extends the exercise's literal credit wording to negative and zero totals. Recover excess interest at regular settlement while preserving earlier payments and the [component reconciliation rules](#interest-adjustments-wait-for-payment). Negative daily balances still earn zero interest.

**Schedule and scenario:** Pay monthly on the first business day for the previous month's ordinary accruals, plus eligible unpaid adjustments, including corrections of older paid periods. Day 6 is the first business day of a new month; Day 5 ends the previous month. For an illustrative 30-day month, Days 1 through 6 map to dates 26, 27, 28, 29, 30 and 01. This assumption selects no actual month, year, jurisdiction or holiday calendar.

Keep the monthly accrual period separate from the cumulative `booking_date <= D-1` cutoff. Current month ordinary accruals remain pending. A newly calculated ordinary accrual for the previous month may join the payment; corrections booked on payment day remain excluded.

**Payment dates and capitalization:** Book and value each credit or debit on its actual payment day, after calculation. A Day 6 movement enters Day 6 fee and interest bases calculated on Day 7; it does not change Day 5 interest. Day 6 interest belongs to the new month and is paid on the first business day of the following month. Actual payment dates let settled interest affect subsequent balances without treating pending interest as already paid.

**Limits:** Actual future payment dates require a business day calendar. Final replay totals, input completeness and the representation, IDs, validation, snapshots and counter effects of zero settlement remain [pending](#pending-calculation-decisions). Financial credits and debits follow the [Authorization payment contract](#yield-calculation-and-payment). [Study 10](../research/10-interest-capitalization-research.md) records the supporting examples.

## Settlements with a Missing Authorization

**Question:** Does SETTLEMENT request a payment, or report a payment that has already settled? The exercise does not define this precisely.

**Decision and assumption:** Treat SETTLEMENT as a legitimate payment already settled outside the ledger. If the local authorization is missing, append the debit with the supplied dates, preserve the authorization reference, and report the missing match. Do not invent an authorization or hold. Preserve the event order and existing records.

**Rationale:** The ledger must reflect the confirmed payment. Leaving out the debit would overstate the account balance. Under this assumption, E6 debits AED 180.00 on Day 4 and reports that Auth-Z was not found. This is why the project rejects acceptance criterion 4.

The [research](../research/03-unmatched-settlements-research.md) explains the alternatives and the Stripe precedent. Stripe documents such payments, but does not determine the exercise's policy. The [fictional example](../examples/05-unmatched-settlement.md) illustrates the decision. Corrections of system errors are outside this decision's scope.

## Authorization and Ledger Responsibilities

**Decision:** Authorization guards available balance through its own operational snapshots, including financial state and active holds. Every authorization uses the latest snapshot. An approved hold creates a new snapshot. A declined request records its decision and supplied ID, preserving funds, holds, the current snapshot and its counter.

**Superseded approach and rationale:** This replaces the earlier responsibility choice in which Ledger supplied the balance to Authorization. Authorization now owns the operational snapshot and supplies downstream accounting data. It sends financial data to Ledger and never consumes Ledger data. The available balance equation remains `ledger balance - active holds` for the same set of financial events. A Ledger report may temporarily reflect fewer events while delivery is pending; it is not Authorization's input.

All financial effects that change available balance, including confirmed payments, reversals, fees, and capitalization, enter through Authorization's snapshot path. Processing a confirmed payment applies its financial effect without the approval check for a new hold. Preserve confirmed debits despite missing authorization or negative availability. Hold release creates no financial entry. Unpaid interest and its corrections remain outside available and ledger balances until capitalization.

**Decision after balance corrections:** Preserve the original authorization decision. Do not automatically reevaluate it when the balance changes. A later increase in funds does not activate a declined request. Evaluate a new explicit request against the updated balance and active holds.

**Rationale:** Each decision reflects the information available when the request was processed. The exercise does not specify automatic reevaluation, so this is an approved project choice. The [example](../examples/06-authorization-decisions.md) applies this policy after a reversal.

## Snapshot Recording and Retries

**Decision:** Transactions arrive with their own IDs; the modules retain those IDs across retry and redelivery rather than generating replacements. Financial events and hold changes are saved with a new snapshot. The event counter is monotonic per account, starting at **1** for its first recorded transaction that creates a snapshot and increasing by one for each subsequent snapshot. The snapshot's `last_event_counter` identifies that account version. Preserve recorded history.

**Declined requests:** Record the decision with its supplied ID, without a hold, financial posting, new snapshot or counter increment. Check ID uniqueness and the unchanged calculation base indivisibly with decision recording. If the base changes before recording, reevaluate the uncommitted request against the latest snapshot with the same ID. A recorded decline remains unchanged after later balance changes.

If an ID is already recorded, including a declined request's ID, skip the message before repeating calculations or inspecting its amount or content. Do not compare payloads or reject differing content. A skipped message creates no financial effect, snapshot, or counter increment. For an unrecorded transaction that creates a snapshot, calculate from the latest saved account snapshot and assign `candidate.event_counter = base.last_event_counter + 1`. The first such transaction uses the initial account state and candidate **1**, even if requests were declined before it.

For a transaction that creates a snapshot, atomically check ID uniqueness and that the calculation base is unchanged, then record the transaction and resulting snapshot. If `latest_snapshot.last_event_counter >= candidate.event_counter`, another recorded transaction advanced the base. Reread it, reapply the uncommitted transaction with the original supplied ID, and calculate a fresh candidate from the new base. Do not attach a fresh counter to a result calculated from an older base. An uncommitted attempt must not publish a Ledger posting.

**Revised choice and rationale:** This replaces creating a snapshot and advancing the counter for every decline. A refusal leaves financial state and holds unchanged, so it should not invalidate Yield's source version. Its decision and ID remain in history for audit and duplicate handling. The [abandoned approach](REJECTED.md#snapshots-for-declined-authorizations) records the previous choice.

**Rationale and limits:** Tying the candidate to the calculation base makes the comparison detect any advance in that account's snapshot version. Enforcing uniqueness in the same operation prevents concurrent attempts with one ID from both affecting state. The retry limit remains an implementation detail to define. No lock or implementation primitive is selected.

## Yield Calculation and Payment

**Decision and superseded approach:** Authorization sends approved transactions to Yield and Fees after recording the transaction and snapshot. Yield receives no input directly from Transaction and consumes no Ledger or Authorization balance. On a trigger, it uses the approved transactions and opening state to reconstruct the dated interest bases. This replaces the earlier shared transaction-input approach and Ledger balance dependency. Confirmed settlements retain their required financial effect and are forwarded even when authorization is missing or availability is negative; the new-hold approval check does not apply to them.

Each payment targets an account and carries a `source_event_counter` identifying the account version whose approved transactions are represented completely. Include the relevant approved transactions through that version, with no later transactions mixed in. Gathering customer data does not introduce an aggregate customer counter. The source counter identifies the calculation input version; the payment's own counter identifies the new recorded transaction.

Authorization skips an already recorded payment ID before inspecting its amount, content, or source counter, even if those values differ. It creates no credit, recalculation request, snapshot, or counter increment. For an unrecorded payment, require `source_event_counter == latest_snapshot.last_event_counter`. Check ID and source version and conditionally record the payment plus snapshot in one indivisible operation. Matching source and current counters of **N** give the payment counter **N+1**. Any mismatch has no payment effect and asks Yield to recalculate with approved transactions for the latest account counter. Because the stale payment was not recorded, that recalculation retains the supplied ID and is evaluated normally.

**Rationale and limits:** Authorization keeps processing while Yield calculates. A transaction that creates a snapshot before calculation finishes or while payment is in transit advances the account counter. Indivisible comparison and recording prevent another transaction from intervening between validation and credit. Counter equality does not establish complete input or correct arithmetic.

Declines do not advance Authorization's counter and are absent from the approved feed. If approved transaction 10 is followed by a decline, the account version remains 10. That decline alone does not invalidate a payment calculated from version 10. Delivery and establishing complete approved inputs for a source account counter remain undefined.

Preserve the positive closing accounting-balance interest base, required rate and rounding, and the approved [daily booking cutoff](#daily-calculation-timing). The Day 6 payment uses reference Day 5. All interest adjustments follow the [pending payment treatment](#interest-adjustments-wait-for-payment). Fee submissions use the same source counter validation and approval or recalculation response. Their [assessment base and correction method](#overdraft-fee-assessment-base) are also approved; remaining policy and protocol details stay open.

## Ledger Delivery

**Decision:** Authorization records its transaction and snapshot first, then sends the committed financial data to Ledger. Ledger posting is a separate operation outside the original conditional recording. Ledger identifies repeated forwarded transactions by their stable ID and does not create another balanced journal entry for the same transaction. Hold-only changes retain their IDs and snapshots without financial postings. Declines retain their decision IDs without creating snapshots or financial postings.

If delivery or Ledger posting fails after Authorization records the transaction, retry delivery of that recorded transaction with its original ID. Do not reapply its operational effects or reauthorize it against a newer snapshot. Delivery preserves the supplied booking and value dates.

**Rationale and tradeoff:** Authorization can record operational decisions before Ledger processes their financial effects. Delivery may be asynchronous. Accounting reports may lag the operational snapshot until the forwarded events are processed. Authorization consumes no Ledger data; Yield calculates from events rather than Ledger reports.

**Still unresolved:** Delivery recovery details and the readiness checkpoints for accounting reports. Delivery delay does not itself change the selected calculation bases or financial policies.

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

**Rationale:** The booking cutoff fixes the accounting input considered by each calculation, independently of execution order. Value dates retain their economic meaning. A cutoff does not guarantee that every eligible event has arrived; general handling of missing eligible records remains unresolved beyond the [approved E10 receipt scenario](#e10-installment-allocation). Record actual receipt separately from booking and value dates; receipt precision, time zone and tie handling remain unspecified.

**Payment boundary:** The Day 6 payment uses reference Day 5. E9 and corrections booked on Day 6 cannot change it, even if processed before that payment. The job's ordinary accrual and resulting payment are outputs, not input transactions excluded by the cutoff. Payment dates and periods follow the [capitalization decision](#interest-payment-schedule-and-capitalization).

**Limits:** Scheduling and calculation-record validation remain [pending](#pending-calculation-decisions). The approved [Authorization payment check](#yield-calculation-and-payment) still compares the current account counter, even when a new booking is outside the calculation cutoff. It can require a retry with the same numerical result. The [fictional example](../examples/08-daily-closing.md) separates the cutoff from later historical corrections without defining replay checkpoints.

**Review status:** Study 06 is approved for now, with its recorded open items and dependencies explicitly pending. Revisit it when a later study affects these decisions.

## Overdraft Fee Assessment Base

**Decision:** For the daily job in D, select inputs cumulatively with `booking_date <= D-1`. Calculate historical day H's balance from those inputs with `value_date <= H`. Exclude only H's own fee components and their adjustments already included in that balance. Keep other periods' fees and refunds at their actual value dates. A negative assessment base uses the [configured fee in the account's currency](#overdraft-fee-currency); zero or a positive base requires no fee. Holds and pending interest do not enter the base.

**Ordinary assessment dates:** Assess historical day H's ordinary fee on H+1, with both booking and value dates H+1. H identifies the balance period; the fee affects funds on its actual assessment day. Corrections retain their separate dating rules below. [Study 07](../research/07-overdraft-fees-research.md#approved-assessment-dates) records this approved choice.

**Assumption and rationale:** A fee should not sustain its own assessment after a legitimate late credit removes the original deficit. Excluding only its own period components prevents that circular result while retaining other periods' dated charges and refunds. This is an approved project interpretation, not an explicit rule in the exercise statement. The reported ledger balance still includes every financial entry passing both date filters.

**Reconciliation:** Calculate `corrected fee - (original fee + all earlier adjustments)`. All earlier adjustments count in this comparison, even when excluded from the historical balance. Append only a nonzero difference, linked to the triggering transaction with a breakdown by historical day. A positive difference debits the ledger; a negative difference refunds. For legitimate late transactions, both dates remain the actual correction day. Review periods chronologically using each component's applicable dates. [Reversal fee refunds](#reversal-compensation) retain the original charge's value date, including when they affect other periods' bases. The [study 07 examples](../research/07-overdraft-fees-research.md#calculation-snapshots) are isolated scenarios, not final balances or a total assessment count.

**Accepted limits:** The [pending calculation decisions](#pending-calculation-decisions) prevent a final replay fee count. The [source counter check](#yield-calculation-and-payment) still applies to every new fee submission, even when an excluded booking leaves its amount unchanged.

## Overdraft Fee Currency

**Requirement and exception:** The exercise requires AED 25.00 per account per negative closing day without exempting BHD accounts. The user explicitly chose a different treatment: configure the daily fee by account type in the account's own currency, with AED 25.00 for the type corresponding to ACC-001 and BHD 0.000 for the type corresponding to ACC-002.

**Assumption and rationale:** Accept this departure from the literal mandatory rule for simplicity because no conversion requirements are supplied. The zero BHD fee is a project choice, not a consequence of monetary precision or HALF_UP.

**Limits:** A negative BHD base produces the configured zero fee; it does not become nonnegative. Authorization still requires available balance after a new hold to remain at or above zero in the account's currency, and legitimate confirmed debits still enter the ledger. No foreign exchange, separate AED obligation, additional account types or general prohibition on negative balances is adopted. [Study 12](../research/12-fee-currency-research.md) separates the supplied principal example from final balances with interest.

## Reversal Compensation

**Scope and decision:** Every correction in this exercise concerns a legitimate transaction; system error correction is excluded as an explicit simplification. Reverse principal once with its supplied dates. Recalculate all affected fees and daily interest from the affected value day onward, including later periods whose bases change, within the applicable calculation boundary. For each component, append only `corrected amount - (original amount + all earlier adjustments)`, including paid adjustments, linked to the reversal and affected components. Recalculation does not automatically refund every fee.

**Fee refund dates:** Book each refund caused by reversal on the actual correction day, with value on the original charge's value date. This is method B in [study 08](../research/08-reversals-research.md). It is a limited exception to the current dating of [late transaction adjustments](#late-transaction-adjustments). A fee for balance day H is offset at its original charge's value date, which need not be H or the principal's value date.

**Rationale:** Neutralize the reversed transaction's affected fee consequences at their economic dates while retaining when funds were actually returned. Funds become available when the refund is recorded. The historical value date changes reconstructed balances without delivering funds in the past or modifying earlier records. Principal only and current value dates for these refunds were not selected. Preserve every record, the approved fee assessment base and HALF_UP; do not automatically reevaluate authorizations.

**Interest and cutoff:** Interest differences retain both dates on the actual correction day and remain pending until an eligible regular payment, even for previously paid periods. Preserve actual payments, count paid adjustments when finding differences, and settle each pending component once. No hypothetical capitalization or interest on pending amounts is introduced. The job in D still requires `booking_date <= D-1`; E9 and adjustments booked on Day 6 cannot alter the Day 6 payment referencing Day 5.

**Accepted limits:** Study 08 leaves the [pending calculation decisions](#pending-calculation-decisions) open. The [fifteen-day simulation](../research/examples/08-reversal-15-day-simulation.md) uses approved H+1 ordinary fee dates with illustrative checkpoints, a Day 10 payment and a Day 15 consultation. Restoring principal, historical balances, net fees, interest and authorizations are distinct claims; the illustration does not establish criterion 6's blanket restoration or current replay totals.

## E10 Installment Allocation

**Scenario and requirement:** E10 credits ACC-002 with BHD 10.000 in three installments, all booked and valued on Day 5. Preserve E10 after E9. BHD requires three decimal places, giving a minimum stored unit of BHD 0.001. Three equal stored amounts cannot preserve the original credit exactly.

**Decision and rationale:** Allocate BHD 3.333, 3.333 and 3.334. Assigning the remaining BHD 0.001 to installment 3 is an approved convention: a fixed position makes the allocation reproducible and lets the final installment complete the original total. Assigning it to installment 1 or 2 would also conserve the credit. The approved HALF_UP mode remains unchanged and does not determine the remainder's position.

**Receipt and interest correction:** E10 arrives on Day 6 after E9 and after the daily job has recorded Day 5 interest without it. Preserve both supplied Day 5 dates. ACC-002's corrected Day 5 interest is BHD 0.004 instead of 0.000. Append the +0.004 difference with both dates Day 6; the booking cutoff excludes it from Day 6 payment. It remains pending until the next eligible monthly payment. This is the specific scenario approved in [study 06](../research/06-daily-closing-research.md#receipt-and-missing-inputs), not a general input-completeness policy.

**Financial effect:** If the installments are individual financial credits, link them to E10 and do not also credit the full parent amount.

**Limits:** The installment representation, IDs, number of events and effect on account counters remain unspecified. This decision introduces no installment calendar, interest between installments or general allocation algorithm. Calculating final results after E10 remains proposed, subject to the [pending calculation decisions](#pending-calculation-decisions).

## Pending Calculation Decisions

The approved decisions above leave these calculation dependencies open.

* **Schedule:** Business time zone, clock times and general replay checkpoints remain undecided beyond the approved E10 scenario. Midnight and a 00:30 start are proposals, as are the other review positions in [study 06](../research/06-daily-closing-research.md#decisions-still-open).
* **Eligible inputs:** General handling of missing records that satisfy the booking cutoff remains unresolved beyond E10. Establishing complete approved inputs for an account counter also remains open under the [Yield contract](#yield-calculation-and-payment).
* **Calculation records:** Validation of cutoff-relevant inputs and prior results, with indivisible recording, remains proposed. It does not replace the approved Authorization source counter check.
* **Replay totals:** Final fee counts, capitalization amounts and balances remain unresolved. They depend on the remaining checkpoints and eligible input completeness; calculating final results after E10 remains proposed.
* **Calendar:** The actual month, year and business day calendar remain unspecified. Day 6's monthly mapping and the payment period for its interest are approved, but actual future payment dates require that calendar.
* **Zero settlement protocol:** Components settle once without a financial movement, but representation, IDs, validation, snapshots and counter effects remain unspecified. The financial payment contract does not resolve this protocol.

## System and Domain Boundaries

**Decision and assumption:** Keep Ledger, Authorization, and Yield and Fees in one in-memory system for the exercise. Give each domain a module with explicit interfaces and ownership of its records and behavior. Transaction is a technical entry module that communicates only with Authorization. After recording, Authorization forwards approved transactions to Yield and Fees and committed financial movements to Ledger.

The required test suite or script preserves replay order and inspects module outputs for the daily reports. Reporting belongs to that test activity, not the Transaction module. This replaces the earlier replay coordinator depicted as a module with direct access to Ledger and Yield.

**Scope:** Tax charges on yield and changes to those taxes are outside scope. The supplied interest-rate rule remains unchanged.

**Rationale:** Separate responsibilities make balances, holds, and calculations easier to explain and test. One system limits coordination overhead; internal interfaces must preserve the domain boundaries. Snapshot validation and retries follow the [recording decision](#snapshot-recording-and-retries). Process count, implementation primitives, deployment topology, language, and technology remain unspecified.

The [architecture and component view](../architecture.md) describe the boundaries and interactions. Existing policy decisions still apply, and unresolved research proposals remain open. This is the Part 2 document, based on the supplied section title and evaluation guidance.

## Ledger Bookkeeping

**Decision:** Use double-entry bookkeeping for money movements received from Authorization. Record a debit in one book account and an equal credit in another, in the same currency. Keep both postings together as one balanced journal entry; do not record only one side. This project architecture choice is not explicitly required by the supplied exercise statement.

**Rationale and meaning:** Make each movement's counterpart and balanced totals explicit. Book accounts are accounting accounts, not necessarily customer accounts. Debit and credit do not universally mean money leaving and entering. The exercise's CREDIT and DEBIT events describe changes to the customer balance; their mapping to book-account postings has not been selected.

**Still unresolved:** The chart of accounts, debit and credit mapping for each event, and how ACC-001 and ACC-002 relate to book accounts. Keeping the postings together does not select an implementation primitive. Ledger records them after Authorization's separate transaction and snapshot operation, as specified in [Ledger Delivery](#ledger-delivery).

[OpenStax, Principles of Accounting, Volume 1: Financial Accounting, section 3.1](https://openstax.org/books/principles-financial-accounting/pages/3-1-describe-principles-assumptions-and-concepts-of-accounting-and-their-relationship-to-financial-statements) explains double-entry bookkeeping and equal debit and credit totals. It is a conceptual reference, not a jurisdictional mandate for this exercise.
