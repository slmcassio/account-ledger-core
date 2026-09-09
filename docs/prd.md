# Account Ledger Core: Product Requirements

## Purpose and scope

Build an explainable ledger in memory for the candidate and exercise reviewer. Support credits, debits, authorizations, holds, confirmed settlements, legitimate late transactions, reversals, fees, interest and E10 installments. No web layer, UI, persistence or database. System error correction, foreign exchange, yield taxes, general installment scheduling and general hold expiration are outside scope.

The candidate must explain every number and decision in the live defense.

These requirements consolidate the [research](#sources), [exercise](exercise-inputs/exercise-statement.md) and [corrected rules](exercise-inputs/business-rules-corrected.md). Exercise rules remain mandatory; approved interpretations and explicit exceptions are identified below. Open decisions remain unresolved. Examples supply no replay totals. External references are precedents, not additional account contracts or jurisdictional requirements. This is a requirements document, not evidence of implementation or passing tests.

## Supplied scenario

Replay [E1 through E10](exercise-inputs/exercise-statement.md#event-stream) in supplied order, never sorted by dates. ACC-001 opens at AED 0.00; ACC-002 at BHD 0.000. Accounts, opening balances and Days 1 through 6 are scenario data.

E7 debits AED 620.00 with booking Day 5 and value Day 2. E9 reverses it with booking Day 6 and value Day 2. E10 stays after E9. Auth-B is never settled during the window and has a hold only if approved.

## Requirements

### FR01. Preserve history

Only append records. Preserve supplied IDs, references, dates, delivery order, financial history, decisions and reservation changes. Never mutate or delete earlier records. Reverse principal once through an opposite movement; retain the original. Queries and reports append nothing. Corrections preserve earlier payments and decisions.

### FR02. Separate funds, holds and decisions

Ledger balance is opening balance plus recorded financial movements for the stated view. Available balance is ledger balance minus active holds, comparing the same financial events. An active hold is the remaining reservation; it changes availability only. Pending interest is unpaid accruals and signed adjustments: unavailable, outside both balances and earning nothing until financial settlement.

Process movements in order. Evaluate new holds from the latest saved account snapshot. Approve only when:

`ledger balance - active holds - requested hold >= 0`

A decline records its decision and ID without changing funds, holds, snapshot or counter. Corrections never automatically reconsider decisions or activate declines. Evaluate a new explicit request against updated state.

**Approved responsibilities:** Transaction communicates only with Authorization, which owns financial state and holds. After recording, Authorization sends financial data to Ledger and approved transactions to Yield and Fees. Authorization consumes no Ledger data. Yield reconstructs bases from that feed and opening state, with no direct Transaction input or balance input from Ledger or Authorization.

### FR03. Prevent duplicates and retry changed calculations

Transactions supply IDs. Skip recorded IDs, including declines, before inspecting amount, content or source version, even if content differs. Repeat no effects, snapshots or counter increments.

Check ID uniqueness and unchanged calculation base together with recording, as one operation: decision alone for a decline; transaction plus snapshot otherwise. If the base changed, reevaluate the uncommitted request using the latest snapshot and same ID. Uncommitted attempts publish no financial posting.

Financial events and hold changes create snapshots. Counters start at 1 per account; each new snapshot uses its saved calculation base's counter plus one. Financial journal delivery follows recording and prevents duplicates using the same ID. Asynchronous delivery can make reports lag operational state.

### FR04. Record settlements independently of holds

**Approved assumption:** SETTLEMENT reports a legitimate payment already settled outside the ledger. Debit its actual amount despite missing authorization, absent, released or expired holds or negative availability; funding checks for new holds do not apply. Report every missing authorization, preserve its reference and dates, and create no synthetic authorization or hold. E6 debits AED 180.00, booking and value Day 4, and reports missing Auth-Z.

For a matching active reservation, a partial settlement that is not final consumes only the settled reservation. Final settlement also releases the unused remainder. Release without settlement frees the specified reservation without financial movement. Append changes and derive remaining holds from history.

**Scenario assumptions:** E5 is final: debit AED 185.00, end Auth-A's AED 200.00 hold and release AED 15.00 without a credit. Generate no automatic expiration during Days 1 through 6; holds do not thereby last forever.

### FR05. Separate dates, receipt and calculation boundaries

`booking_date` is the supplied accounting recording day; `value_date` starts its balance effect. Record actual receipt separately. Receipt overrides neither dates nor cutoff, even when received early.

Run one daily job in D referencing D-1. Select cumulative bookings with `booking_date <= D-1`; closing balance for historical day H includes opening balance and selected financial entries with `value_date <= H`. Holds and pending interest are excluded. Intraday deficits alone trigger no daily fee.

The job's accrual and payment are outputs allowed by this calculation. Corrections booked on D are not admitted. Thus E9 and Day 6 adjustments cannot change Day 6 payment amounts even if processed first. Day 7 evaluates Day 6, including its payment movements.

### FR06. Correct only the difference

Append legitimate late principal with supplied dates, then recalculate affected fees and interest by historical day and component:

`difference = corrected target - (original amount + all prior adjustments)`

Include paid and unpaid adjustments; compare rounded daily interest targets. Append only nonzero differences, linked to the cause with a historical breakdown, never repeating principal. Unchanged recalculations yield zero.

Ordinary late adjustments use correction day for both dates. Positive fee differences debit; negative differences refund. Interest differences, including corrections of paid periods, remain pending until the next regular payment admitting them under its booking cutoff.

### FR07. Assess fees without making them sustain themselves

Use FR05's filters. For H's assessment base, remove only H's own fee components and adjustments already included. Retain other periods' charges and refunds at actual value dates. Reported ledger balance retains all filtered financial entries.

Set one daily target per account: configured fee for a negative base, otherwise zero. Assess ordinary H fees on H+1 with both dates H+1. Review corrections chronologically using FR06, counting all prior adjustments even if excluded from the historical balance.

**Approved exception:** configure fees by account type in its currency: AED 25.00 for ACC-001's type; BHD 0.000 for ACC-002's type. Zero replaces the literal AED fee for BHD because conversion requirements are missing. It neither prohibits negative balances nor permits unfunded holds. Infer no exchange rate, separate AED obligation or additional type.

### FR08. Correct reversal effects using original fee value dates

**Approved method B:** after reversing principal, recalculate affected fees and interest from the affected value day onward, including later periods whose bases change within the calculation boundary. Apply FR06 differences, not automatic refunds of every fee.

Book reversal fee refunds on correction day, with the original charge's value date, which may differ from H and the principal's value date. Funds become available when recorded; historical balances are reconstructed. Interest corrections keep current dates and remain pending. Do not rewrite payments or invent returns from past payment dates. Principal, historical balances, net fees, interest and decisions are distinct outcomes.

### FR09. Round interest once per day

Store and round amounts to two decimal places for AED and three for BHD. The exercise requires daily interest of 0.04%:

`daily interest = HALF_UP(max(daily base, 0) * 0.0004, currency precision)`

Zero or negative bases earn zero. Keep multiplication exact until one currency rounding; no intermediate rounding or fractions carried between days. Exact products need at most 6 fractional places for AED, 7 for BHD, without limiting integer digits. Holds do not reduce this base.

**Approved rounding:** HALF_UP rounds exact ties away from zero, favoring recipients of positive interest and accepting upward bias. It establishes no customer benefit for negative amounts or fees or intermediate precision for unrelated calculations.

### FR10. Settle eligible interest exactly once

Sum eligible unpaid rounded accruals and signed adjustments exactly, including corrections of older paid periods. Never round aggregated raw products or discard differences. Record settled components, exclude them from later payments and preserve earlier payments.

**Approved schedule:** pay separately in AED and BHD on each month's first business day for ordinary accruals from the previous month plus eligible adjustments. Payment period and booking cutoff are separate; ordinary accruals from the current month wait.

**Scenario mapping:** Day 5 ends the previous month; Day 6 is the new month's first business day. Settle at end of Day 6 referencing Day 5, including that job's ordinary accrual. Day 6 interest is calculated Day 7 and paid the following month's first business day. No specific month, year or holiday calendar is selected.

**Approved extension of the single credit wording:** positive totals make one credit; negative totals debit even into a negative balance; zero settles components without funds moving. Carry no eligible negative total forward. Book and value financial payment on actual payment day, after calculation.

### FR11. Reject stale fee and payment submissions

All financial effects use FR03's checks. A new fee or financial interest payment supplies `source_event_counter`, its account calculation version, distinct from booking cutoff or global delivery order. Check equality with the current snapshot counter together with ID uniqueness and recording the transaction and snapshot, as one operation.

A mismatch applies no financial effect. Recalculate from approved transactions for the latest account version and retry the same unrecorded ID. The resulting snapshot uses current counter plus one. E9 can invalidate a source version although its excluded booking leaves the payment amount unchanged. A decline invalidates neither snapshot nor source version.

### FR12. Conserve E10 and retain its late correction

Allocate BHD 10.000 as 3.333, 3.333, 3.334: `10,000 = 3 * 3,333 + 1` units of BHD 0.001. Assigning the remainder to installment 3 is approved independently of HALF_UP. All booking and value dates are Day 5; no installment schedule. Individual credits, if used, must link to E10 without also crediting its parent.

**Approved receipt:** E10 arrives Day 6 after E9 and after the job recorded Day 5 interest without it. Correct BHD 0.000 to `10.000 * 0.0004 = 0.004`. Append +0.004 with both dates Day 6, excluded from Day 6 payment and pending until the next eligible monthly payment.

## Acceptance and evidence

The runnable suite or script must exercise FR01 through FR12 and print daily closing ledger balances, fees, authorization states and errors. Identify account, historical day, processed-event boundary, booking cutoff and fee treatment. Test redelivery with changed content, persistent declines, same-ID retries, stale submissions with unchanged amounts, hold variants, fee bases and dates and E10 eligibility.

Keep these independent numerical checks, not replay totals:

| Check | Expected result |
|---|---|
| AED bases 12.49 / 12.50 | Daily interest 0.00 / 0.01; zero or negative bases earn zero |
| Two eligible unpaid days at AED 465.00 | 0.19 + 0.19 = 0.38, not 0.37 |
| AED original 1.00, prior +0.20, corrected 1.30 | Only +0.10, even if +0.20 was paid; repeating yields zero |
| Eligible AED 0.10 + 0.20 - 0.02 | Settle 0.28 |
| Eligible AED 0.10 - 0.30 | Debit 0.20; zero totals settle without movement |

[Original criterion assessments](research/13-acceptance-criteria-research.md):

| # | Treatment |
|---|---|
| 1 | Correct before E9, excluding fees: Day 2 principal `1,200 - 950 - 620 = AED -370.00`, not a final closing total. |
| 2 | The final E7 fee count remains open, not its date rules. Day 2 is the historical balance day: ordinary assessment uses H+1; late corrections use their actual correction day. Assert neither one nor three fees. |
| 3 | Accept Auth-A's confirmed settlement; apply the final settlement assumption. |
| 4 | Reject the demand to refuse unmatched settlements under the confirmed-payment assumption. Mandatory rules alone leave this undefined. |
| 5 | Correct conditionally: an approved Auth-B hold changes availability only. Approval is not established. |
| 6 | Blanket restoration after E9 is unsupported: preserve history, payments and decisions while applying FR08. |
| 7 | Incorrect: `3 * BHD 3.334 = 10.002`, exceeding E10 by 0.002. |
| 8 | Incorrect: discarding a remainder violates exact equality with eligible rounded components. |

Retain the exercise's repository and live defense requirements, including one annotated failing test explaining a limitation of the chosen design; report that intentional failure separately from passing checks.

## Open decisions

* **Checkpoints and totals:** reviews after E7 for Days 2 through 4 before E8, and after E9 for Days 2 through 5, remain proposals. Ordinary checkpoints beyond E10's receipt scenario are open; E8 does not close Day 5. Final bases, fee counts, balances and capitalization totals remain unresolved.
* **Clock and receipt:** time zone, precision and ties; midnight and 00:30 starts are proposals.
* **Completeness and concurrency:** general missing inputs, feed coverage of the required account version, and validation and atomic recording of calculation records. These do not replace approved source counter validation.
* **Delivery and reporting:** asynchronous delivery mechanism and report readiness.
* **Calendar and zero settlement:** actual business day calendar, zero settlement representation and counter effects.
* **Installments:** physical representation, IDs, event count and counter effects.
* **Expiration beyond replay:** duration or deadline, time reference and update rules.

## Sources

[01 Rounding](research/01-rounding-research.md), [02 Dates](research/02-booking-and-value-dates-research.md), [03 Unmatched settlements](research/03-unmatched-settlements-research.md), [04 Authorization](research/04-authorization-decisions-research.md), [05 Holds](research/05-hold-lifecycle-research.md), [06 Closing](research/06-daily-closing-research.md), [07 Fees](research/07-overdraft-fees-research.md), [08 Reversals](research/08-reversals-research.md), [09 Interest](research/09-daily-interest-research.md), [10 Capitalization](research/10-interest-capitalization-research.md), [11 Installments](research/11-installments-research.md), [12 Fee currency](research/12-fee-currency-research.md), [13 Acceptance](research/13-acceptance-criteria-research.md).

The [15-day reversal simulation](research/examples/08-reversal-15-day-simulation.md) illustrates method B before or after payment. Its Day 10 payment and review schedule are not the E1 through E10 calendar.
