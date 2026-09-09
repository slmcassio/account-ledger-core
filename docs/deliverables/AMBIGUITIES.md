# Ambiguities

Each item answers four questions: what is unclear, why, what we decided, and why we chose it. **Open** means no resolution has been approved. Research links retain the supporting evidence and its limits.

## Rounding Mode

1. **Ambiguity:** How should exact monetary ties be rounded?
2. **Why unclear:** The exercise sets currency precision but names no rounding mode.
3. **Decision:** Use HALF_UP: nearest amount, with exact ties away from zero. Store and round AED to two decimal places and BHD to three.
4. **Reason:** The approved assumption favors positive interest recipients at exact ties. We accept the upward bias; this rationale does not imply a customer benefit for fees or negative adjustments.

<a id="supporting-sources-and-limits"></a>

**Source:** [Study 01, analysis](../research/01-rounding-research.md#analysis) and [sources and limits](../research/01-rounding-research.md#sources-and-limits). The securities documents are contractual precedents, not mandatory account rules. Currency alone establishes neither jurisdiction nor contract. Mambu's [precision documentation](https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/) does not specify HALF_UP or HALF_EVEN.

**Open:** Intermediate precision and rounding stages outside daily interest remain undefined.

## Daily Interest Calculation

### Daily product and rounding

1. **Ambiguity:** At which calculation stage should daily interest be rounded?
2. **Why unclear:** The required rate and currency precision do not specify intermediate rounding or carrying fractions between days.
3. **Decision:** Calculate `HALF_UP(max(daily_base, 0) * 0.0004, currency_precision)` once per account and day. Keep the product exact until that rounding; carry no fractions between days.
4. **Reason:** Each day's amount can be reproduced from its own base. Intermediate rounding can change it. This selects no language or arithmetic library.

**Source:** [Study 09, calculation](../research/09-daily-interest-research.md#rule-and-approved-calculation) and [examples](../research/09-daily-interest-research.md#why-the-stages-matter). Its arithmetic reference is technical support, not a banking mandate.

### Correcting a rounded daily amount

1. **Ambiguity:** Should a correction round the raw interest difference or compare rounded daily amounts?
2. **Why unclear:** These methods can give different results at rounding boundaries.
3. **Decision:** Subtract the original accrual and all previous adjustments, including paid ones, from the revised rounded daily target. Subtract exactly and append only a nonzero difference.
4. **Reason:** The correction reconciles recorded monetary amounts. Repeating an unchanged target gives zero, including after payment.

**Source:** [Study 09, corrections and payment](../research/09-daily-interest-research.md#corrections-and-payment). Payment uses the [unpaid component rule](#interest-adjustments-wait-for-payment); final bases and totals remain open.

## Booking and Value Dates

1. **Ambiguity:** Does "Booked" identify the financial effect, recording, occurrence or receipt day?
2. **Why unclear:** Supplied booking and value dates differ, and E10 follows E9 despite its earlier booking date.
3. **Decision:** `booking_date` is the accounting recording day; `value_date` determines when the balance is affected. Preserve supplied dates and event order. Record actual receipt separately; neither date necessarily identifies occurrence or receipt.
4. **Reason:** Separate recording from financial effect. A later record can change reconstructed historical balances while queries restricted to earlier records reproduce the earlier view.

**Source:** [Study 02, date definitions](../research/02-booking-and-value-dates-research.md#date-definitions) and [limits](../research/02-booking-and-value-dates-research.md#sources-and-limits). ISO defines message dates, not this project's adjustment policy.

## Late Transaction Adjustments

### Amount and audit trail

1. **Ambiguity:** How should a legitimate late transaction change fees and interest already recorded?
2. **Why unclear:** History must remain immutable, but the affected calculations may now be wrong.
3. **Decision:** Append the transaction with its supplied dates. Recalculate each affected component and append only `corrected amount - (original amount + all prior adjustments)`, counting paid adjustments. Link the difference to its trigger and historical day; never repeat principal.
4. **Reason:** Record only what changed while preserving the original result and its explanation. System error corrections are excluded by explicit simplification.

**Source:** [Study 02, approved adjustment method](../research/02-booking-and-value-dates-research.md#approved-adjustment-method).

### Adjustment dates

1. **Ambiguity:** Should a late adjustment affect the historical day or the correction day?
2. **Why unclear:** "Day assessed" could mean the affected balance day or the later assessment day.
3. **Decision:** Use the actual correction day for both adjustment dates. Historical days identify calculation components. Fee differences affect the ledger; interest differences [remain pending](#interest-adjustments-wait-for-payment). [Reversal fee refunds](#reversal-compensation) have a limited exception.
4. **Reason:** The approved interpretation makes the fee difference affect current funds while preserving the calculation history. Interest waits for payment; no earlier record is rewritten.

**Source:** [Study 02, definitions and policy](../research/02-booking-and-value-dates-research.md#date-definitions). Mambu illustrates other product behavior; CBUAE error provisions do not establish this legitimate late transaction policy. [Example 04](../examples/04-backdated-adjustment.md) uses an illustrative Day 6 correction, not a replay checkpoint.

## Interest Adjustments Wait for Payment

### Corrections of paid and unpaid periods

1. **Ambiguity:** Should a correction for a paid period change the balance immediately?
2. **Why unclear:** An earlier payment has already reached the ledger, while unpaid interest has not.
3. **Decision:** Keep every interest difference pending until the next regular payment whose booking cutoff admits it. Positive differences increase pending interest; negative differences reduce it. Both adjustment dates remain the actual correction day.
4. **Reason:** Use one treatment regardless of whether the original period was paid. This approved deferral replaces the earlier immediate correction of credited interest.

**Source:** [Study 10, payment components](../research/10-interest-capitalization-research.md#approved-payment-and-components) and [recorded decision](WORKLOG.md#08-september-2026-000010).

### Components settled by a payment

1. **Ambiguity:** Which amounts should enter the next interest payment after a correction?
2. **Why unclear:** Including paid originals again would duplicate interest; ignoring paid adjustments would distort later corrections.
3. **Decision:** Sum eligible unpaid accruals and adjustments exactly, never rounded aggregated raw interest. Link each component to its settlement and settle it once. Preserve earlier payments; retain settled components when calculating future differences.
4. **Reason:** Settlement links distinguish what remains unpaid from what was already recorded. Never pay a full corrected target plus its adjustment, repeat principal, or discard a reconciliation difference.

**Source:** [Study 10, payment components](../research/10-interest-capitalization-research.md#approved-payment-and-components) and [Study 09, rounding stages](../research/09-daily-interest-research.md#why-the-stages-matter).

**Example:** A transaction is booked and corrected on D30 with value date D5. Assume D5, D20 and D25 each change from AED 1.50 to 2.00; other days are omitted. D5 was paid on D15; D20 and D25 remain unpaid.

Record `3 * 0.50 = 1.50` pending, with both adjustment dates D30 and a breakdown by day. The next eligible payment includes `3.00 + 1.50 = 4.50`, excluding D5's paid original. Each repeated calculation gives `2.00 - (1.50 + 0.50) = 0.00`, before or after payment. D15 is illustrative, not the [monthly schedule](#interest-payment-schedule-and-capitalization); no corrective credit is backdated.

### Balance and interest on pending amounts

1. **Ambiguity:** Do pending accruals or corrections provide funds or earn further interest?
2. **Why unclear:** Economic interest entitlement and actual capitalization occur at different times.
3. **Decision:** Pending amounts affect neither ledger nor available balance and earn no interest. Calculate no hypothetical returns from earlier payment dates.
4. **Reason:** This approved simplification keeps pending calculations separate from posted money. Actual payments start affecting subsequent balances on their payment dates.

**Source:** [Study 10, pending interest](../research/10-interest-capitalization-research.md#why-pending-interest-earns-nothing). The CBUAE disclosure provisions establish neither this method nor its applicability or compliance.

## Interest Payment Schedule and Capitalization

### Positive, negative and zero totals

1. **Ambiguity:** What happens when eligible unpaid interest totals zero or less?
2. **Why unclear:** The exercise literally requires one Day 6 credit per account, without defining those cases.
3. **Decision:** At the end of Day 6, settle each account separately in AED or BHD: positive totals credit; negative totals debit, even into a negative balance; zero settles components without moving funds. Carry no eligible negative total forward.
4. **Reason:** The approved extension recovers excess interest at regular settlement while preserving earlier payments. Negative daily balances still earn zero interest.

**Source:** [Study 10, signed settlement](../research/10-interest-capitalization-research.md#approved-signed-settlement). The [zero settlement protocol](#zero-settlement-protocol) remains open.

### Monthly schedule and Day 6

1. **Ambiguity:** How does the required Day 6 settlement relate to a recurring payment schedule?
2. **Why unclear:** The exercise supplies synthetic days, with no month or calendar.
3. **Decision:** Day 5 ends the previous month; Day 6 is the new month's first business day. Pay monthly on the first business day for the previous month's ordinary accruals plus eligible unpaid adjustments, including older corrections.
4. **Reason:** This approved scenario mapping connects Day 6 to the monthly policy. Booking eligibility remains separate; current month ordinary accruals stay pending.

**Source:** [Study 10, monthly payment](../research/10-interest-capitalization-research.md#approved-monthly-payment). A 30-day example maps Days 1 through 6 to 26, 27, 28, 29, 30 and 01. It chooses no actual month, year, jurisdiction or holiday calendar.

### Payment dates and later bases

1. **Ambiguity:** Does capitalization alter the days whose interest it pays?
2. **Why unclear:** Payment occurs after the accrual period and after its calculation.
3. **Decision:** Book and value the credit or debit on its actual payment day, after calculation. A Day 6 movement enters Day 6 bases calculated on Day 7; it never changes Day 5 interest.
4. **Reason:** Settled interest affects later balances without treating pending interest as already paid. Day 6 ordinary interest belongs to the new month and waits for the following month's payment.

**Source:** [Study 10, capitalization date](../research/10-interest-capitalization-research.md#approved-capitalization-date). Financial payments follow [Authorization validation](#yield-calculation-and-payment).

## Settlements with a Missing Authorization

1. **Ambiguity:** Should E6 be rejected because Auth-Z is absent?
2. **Why unclear:** SETTLEMENT could request a payment or report one already settled. The mandatory rules do not resolve criterion 4.
3. **Decision:** Assume a legitimate externally settled payment. Append E6's AED 180.00 debit with Day 4 dates, retain Auth-Z and report the missing match. Create no authorization or hold; preserve order and history.
4. **Reason:** Omitting a confirmed debit overstates the balance. This assumption supports rejecting criterion 4; it excludes system error corrections.

**Source:** [Study 03, approved decision](../research/03-unmatched-settlements-research.md#the-approved-decision) and [source limit](../research/03-unmatched-settlements-research.md#sources-and-limits). Stripe illustrates unmatched payments but does not establish E6's legitimacy or the exercise policy.

## Authorization and Ledger Responsibilities

### Source of available balance

1. **Ambiguity:** Which module owns the balance used to authorize a hold?
2. **Why unclear:** The balance equation defines arithmetic, not which module provides current state.
3. **Decision:** Authorization uses its latest operational snapshot, containing financial state and active holds. Available balance is `ledger balance - active holds` for the same financial events. Authorization sends financial data to Ledger and never reads it.
4. **Reason:** Authorization owns its decision state. This replaces the Ledger-supplied balance approach; Ledger reports may temporarily lag while delivery is pending.

**Source:** [Study 04, responsibilities](../research/04-authorization-decisions-research.md#approved-responsibilities). Stripe does not determine this snapshot ownership.

### New holds and confirmed financial movements

1. **Ambiguity:** Does the new-hold funding check also apply to confirmed payments?
2. **Why unclear:** Both can reduce availability, but only one is a request to reserve funds.
3. **Decision:** Approve a new hold only if remaining availability is nonnegative. Route confirmed payments, reversals, fees and capitalization through Authorization snapshots; preserve confirmed debits even with missing authorization or negative availability.
4. **Reason:** Hold approval prevents an unfunded reservation. A confirmed debit must reflect money already settled. Holds change availability without financial postings; unpaid interest remains outside both balances.

**Source:** [Study 04, authorization rule](../research/04-authorization-decisions-research.md#authorization-rule) and [responsibilities](../research/04-authorization-decisions-research.md#approved-responsibilities).

### Decisions after balance corrections

1. **Ambiguity:** Should later funds reactivate a declined authorization?
2. **Why unclear:** Immutability preserves history but does not decide whether to append a revised decision.
3. **Decision:** Preserve the recorded decision without automatic reevaluation. A new explicit request is evaluated against the updated snapshot and active holds.
4. **Reason:** Each decision reflects information available when processed. Later balance changes do not turn an earlier refusal into an approved hold.

**Source:** [Study 04, later corrections](../research/04-authorization-decisions-research.md#approved-policy-later-balance-corrections) and [example 06](../examples/06-authorization-decisions.md).

## Snapshot Recording and Retries

### Supplied IDs and duplicate messages

1. **Ambiguity:** How should retry or redelivery affect a previously processed transaction?
2. **Why unclear:** Repeating its calculation or financial effect could change state twice.
3. **Decision:** Retain supplied IDs. Skip a recorded ID, including a decline, before calculation or any amount or content inspection, even if content differs. Compare no payloads and reject no differing content on this path.
4. **Reason:** One recorded ID has one outcome. A skipped message adds no financial effect, snapshot or counter increment.

**Source:** [Study 04, responsibilities](../research/04-authorization-decisions-research.md#approved-responsibilities) and [architecture contract](../architecture.md#snapshots-and-concurrency).

### Snapshot counter allocation

1. **Ambiguity:** What does the event counter count, and where does it start?
2. **Why unclear:** Requests can be declined without changing financial state or holds.
3. **Decision:** Financial events and hold changes create snapshots. Each account starts at 1 and advances by one per snapshot: `candidate.event_counter = base.last_event_counter + 1`. The first candidate uses initial state and 1, even after earlier declines.
4. **Reason:** The counter identifies the saved account version used for calculation, rather than counting every received message.

**Source:** [Architecture, snapshots](../architecture.md#snapshots-and-concurrency) and [recorded snapshot contract](WORKLOG.md#09-september-2026-001355).

### Declined requests

1. **Ambiguity:** Should a decline create a new snapshot and advance the counter?
2. **Why unclear:** It is a recorded decision, but funds and holds remain unchanged.
3. **Decision:** Record only the decision and supplied ID. Check ID uniqueness and unchanged calculation base indivisibly with recording. If the base changes, reevaluate the uncommitted request with the same ID against the latest snapshot.
4. **Reason:** A refusal needs audit and duplicate handling but should not invalidate Yield's financial version. A recorded decline stays unchanged and creates no hold, posting, snapshot or increment.

**Source:** [Study 04, responsibilities](../research/04-authorization-decisions-research.md#approved-responsibilities) and [superseded approach](REJECTED.md#snapshots-for-declined-authorizations).

### Concurrent state changes

1. **Ambiguity:** What if another transaction changes the account before a calculated result is recorded?
2. **Why unclear:** Assigning a new counter alone would attach an old calculation to new state.
3. **Decision:** Check ID uniqueness and unchanged base, then record transaction, effects and snapshot indivisibly. If `latest_snapshot.last_event_counter >= candidate.event_counter`, reread and reapply the uncommitted transaction with its original ID before deriving a fresh candidate.
4. **Reason:** Validation and recording must prevent a competing advance or duplicate from slipping between them. Uncommitted attempts publish no Ledger posting; never attach a fresh counter to an old result.

**Source:** [Architecture, snapshots](../architecture.md#snapshots-and-concurrency). Retry limits, locks and implementation primitives remain open.

## Yield Calculation and Payment

### Calculation inputs

1. **Ambiguity:** Should Yield use Transaction messages, Ledger balances or approved events?
2. **Why unclear:** Those inputs can represent different states while transactions are being processed or delivered.
3. **Decision:** Authorization forwards approved transactions after recording. Yield reconstructs dated bases from that feed and opening state, without direct Transaction input or Ledger or Authorization balances. Confirmed settlements remain included despite missing authorization or negative availability.
4. **Reason:** Calculate from recorded operational outcomes. This replaces the shared Transaction input and Ledger balance dependencies without applying new-hold approval to confirmed payments.

**Source:** [Architecture, domains](../architecture.md#domains-and-module-boundaries) and [Study 06, architecture relation](../research/06-daily-closing-research.md#relation-to-the-architecture).

### Source account version

1. **Ambiguity:** Which state does a calculation's `source_event_counter` identify?
2. **Why unclear:** Authorization can advance while Yield receives inputs or calculates.
3. **Decision:** Use one complete approved transaction set through the target account's source counter; mix in no later transactions. The payment's own counter identifies a new transaction. Gathering customer data creates no customer-wide counter.
4. **Reason:** Each result needs a reproducible account version. Equality of counters alone proves neither input completeness nor correct arithmetic.

**Source:** [Architecture, version validation](../architecture.md#calculation-version-validation). Delivery and establishing complete inputs remain open.

### Recording a payment or fee

1. **Ambiguity:** Can Authorization accept a result calculated before its latest account change?
2. **Why unclear:** The account may advance during calculation or while the result is in transit.
3. **Decision:** For an unrecorded ID, require `source_event_counter == latest_snapshot.last_event_counter`. Check ID and version and record the financial effect, payment or fee, and snapshot indivisibly. Matching counters N produce transaction counter N+1.
4. **Reason:** No state change may intervene between validation and recording. Any mismatch records no payment effect and requests recalculation from complete latest inputs with the same supplied ID.

**Source:** [Architecture, version validation](../architecture.md#calculation-version-validation). Fee submissions use the same approval or recalculation response.

### Duplicate results and unchanged amounts

1. **Ambiguity:** Must a duplicate result or an unchanged numerical amount be recalculated?
2. **Why unclear:** Identity, financial eligibility and operational version are different checks.
3. **Decision:** Skip recorded payment IDs before inspecting amount, content or source counter, even if changed; create no payment, recalculation request, snapshot or increment. An unrecorded stale result must recalculate even when an excluded booking leaves its amount unchanged.
4. **Reason:** Duplicates have already been resolved, while stale results use an old account version. Declines do not advance that version or invalidate a current calculation; they are absent from the approved feed.

**Source:** [Study 06, architecture relation](../research/06-daily-closing-research.md#relation-to-the-architecture). The [booking cutoff](#daily-calculation-timing) remains separate from source validation.

## Ledger Delivery

### Recording boundary

1. **Ambiguity:** Must Authorization and Ledger record in one operation?
2. **Why unclear:** They own separate operational and accounting records.
3. **Decision:** Authorization records first, then forwards committed financial data. Ledger records separately and uses the stable ID to prevent a second balanced journal entry. Hold changes and declines create no financial postings.
4. **Reason:** Authorization can finish its operational decision before Ledger processes it. Delivery may be asynchronous, so accounting reports can lag operational snapshots.

**Source:** [Architecture, Ledger delivery](../architecture.md#ledger-delivery). Authorization consumes no Ledger data; Yield calculates from events.

### Retrying committed delivery

1. **Ambiguity:** Should a failed delivery reauthorize or recalculate the original transaction?
2. **Why unclear:** Authorization may already have recorded it successfully while Ledger has not.
3. **Decision:** Retry delivery or posting with the recorded transaction's original ID, booking date and value date. Do not reapply operational effects or reauthorize against newer state.
4. **Reason:** The operational effect is already committed. Reapplying it could duplicate or change that outcome; delivery delay does not change financial policy.

**Source:** [Architecture, Ledger delivery](../architecture.md#ledger-delivery). Recovery details and reporting readiness checkpoints remain open.

## Hold Settlement and Release

### Auth-A finality

1. **Ambiguity:** Does Auth-A's AED 185.00 settlement end its AED 200.00 reservation?
2. **Why unclear:** A smaller settlement may be partial or final; the absence of later settlements does not prove finality.
3. **Decision:** Assume final settlement. Append the actual debit with Day 4 dates. If the matching reservation is active, end it and release the unused AED 15.00 without a ledger credit.
4. **Reason:** Finality is an approved simplification for this scenario. Criterion 3 supports recording the confirmed payment, independently of the reservation's current state.

**Source:** [Study 05, settlement and release](../research/05-hold-lifecycle-research.md#approved-policy-settlement-and-release). Product and network precedents do not determine Auth-A's finality.

### Other reservation changes

1. **Ambiguity:** How do partial settlement and release differ from a financial refund?
2. **Why unclear:** All can free reserved funds, but only financial movements change the ledger balance.
3. **Decision:** A non-final settlement reduces an active reservation by the settled portion; a final one also releases the remainder. Release without settlement frees the specified reservation without a debit or credit. Append changes and preserve original decisions.
4. **Reason:** Separate confirmed financial payments from reservation state. Missing, released or expired holds never prevent recording a legitimate externally settled debit; report matching problems separately.

**Source:** [Study 05, policy](../research/05-hold-lifecycle-research.md#approved-policy-settlement-and-release) and [sources and limits](../research/05-hold-lifecycle-research.md#sources-and-limits). External deadlines and record models are not adopted; API and network integration design stay outside scope.

## Hold Expiration During the Replay

1. **Ambiguity:** Should Auth-B or another hold expire during Days 1 through 6?
2. **Why unclear:** The exercise supplies no deadline or expiration policy. The replay window is not a hold lifetime.
3. **Decision:** Generate no automatic expiration in the six-day replay. Auth-B has a hold only if approved; lack of settlement does not prove approval.
4. **Reason:** Avoid inventing an expiration event. This does not mean holds never expire; a general duration, time reference and update policy remain undefined.

**Source:** [Study 05, expiration](../research/05-hold-lifecycle-research.md#approved-policy-expiration). Later corrections preserve the [original authorization decision](#decisions-after-balance-corrections).

## Daily Calculation Timing

### Daily reference and eligible entries

1. **Ambiguity:** Which transactions belong to a daily calculation while processing continues?
2. **Why unclear:** Receipt order, accounting recording day and financial effect day can differ.
3. **Decision:** Process the supplied order and update operational balances when recorded. Run one job in D for D-1. Select cumulative inputs with `booking_date <= D-1`, then opening balance and effects with `value_date <= calculated day`.
4. **Reason:** Booking fixes the eligible input set; value dates determine economic effect. Earlier receipt does not admit future bookings. An intraday deficit alone triggers no daily fee.

**Source:** [Study 06, agreed operation](../research/06-daily-closing-research.md#agreed-operation). Pending interest is excluded. Holds neither reduce the interest base nor block capitalization.

### Day 6 payment boundary

1. **Ambiguity:** Can E9 or same-day corrections change the Day 6 payment if processed first?
2. **Why unclear:** They may already be known but have booking dates later than its Day 5 reference.
3. **Decision:** Exclude E9 and corrections booked Day 6 from that payment. The job's new ordinary accrual can participate if its period is eligible; its accrual and payment are outputs, not excluded input transactions.
4. **Reason:** Execution order must not override the booking cutoff. A later booking may still advance Authorization's counter and require a retry with the same numerical amount.

**Source:** [Study 06, agreed operation](../research/06-daily-closing-research.md#agreed-operation) and [Study 10, monthly eligibility](../research/10-interest-capitalization-research.md#approved-monthly-payment).

### Receipt and completeness

1. **Ambiguity:** Does reaching a cutoff prove that all eligible transactions have arrived?
2. **Why unclear:** E10 can arrive later while retaining an eligible earlier booking date.
3. **Decision:** Record actual receipt separately and preserve the [approved E10 scenario](#e10-installment-allocation). General missing-input handling, receipt precision, time zone and tie handling remain open. Queries append nothing.
4. **Reason:** Date eligibility does not prove receipt or completeness. E10 resolves one scenario, not a general protocol.

**Source:** [Study 06, receipt](../research/06-daily-closing-research.md#receipt-and-missing-inputs) and [open decisions](../research/06-daily-closing-research.md#decisions-still-open). Study 06 remains approved with these dependencies pending; revisit it when later decisions affect them.

## Overdraft Fee Assessment Base

### Excluding the assessed period's own fee

1. **Ambiguity:** Should a fee help keep its own assessment base negative?
2. **Why unclear:** The reported ledger includes fees, so an earlier fee could sustain itself after a late credit removes the original deficit.
3. **Decision:** Apply the booking and value filters for historical day H. Remove only H's own fee components and adjustments already included. Keep other periods' charges and refunds at their actual value dates; exclude holds and pending interest.
4. **Reason:** Prevent a circular assessment without changing the reported ledger balance. Charge the configured fee once per account per negative day; zero and positive bases incur none.

**Source:** [Study 07, approved base](../research/07-overdraft-fees-research.md#rule-and-approved-base). This is a project interpretation, not an explicit exercise rule.

### Ordinary fee dates

1. **Ambiguity:** Does an ordinary fee use balance day H or the later assessment day?
2. **Why unclear:** The exercise says "day assessed" while the daily job evaluates the previous day.
3. **Decision:** Assess H's ordinary fee on H+1, with both booking and value dates H+1. Keep H as its balance-period reference.
4. **Reason:** This follows the approved daily schedule and makes the fee affect funds when assessed. Late corrections and reversal refunds retain their separate dates.

**Source:** [Study 07, assessment dates](../research/07-overdraft-fees-research.md#approved-assessment-dates).

### Correcting a fee target

1. **Ambiguity:** Which prior adjustments count when reconciling a revised fee?
2. **Why unclear:** An adjustment can be excluded from H's dated balance yet already correct H's fee.
3. **Decision:** Calculate `corrected fee - (original fee + all earlier adjustments)`. Count every prior adjustment; append only a nonzero linked difference with a breakdown by day. Positive differences debit; negative differences refund. Review periods chronologically using applicable dates.
4. **Reason:** Separate the assessment base from the amount already recorded. Repeated unchanged targets produce zero, without losing other periods' dated effects.

**Source:** [Study 07, corrections](../research/07-overdraft-fees-research.md#corrections) and [isolated examples](../research/07-overdraft-fees-research.md#calculation-snapshots). Late adjustments use correction-day dates; [reversal refunds](#reversal-compensation) retain original fee value dates. Every new fee submission still requires source counter validation; final fee counts remain open.

## Overdraft Fee Currency

1. **Ambiguity:** How should the mandatory AED 25.00 fee apply to a BHD account?
2. **Why unclear:** The exercise exempts no account but supplies no exchange rate, reference date or conversion policy.
3. **Decision:** Configure fees by account type in its own currency: AED 25.00 for ACC-001's type and BHD 0.000 for ACC-002's type. The zero BHD fee is an explicit approved exception to the literal mandatory rule.
4. **Reason:** The user chose simplicity because conversion requirements are missing. Neither BHD precision nor HALF_UP implies this exception.

**Source:** [Study 12, exception](../research/12-fee-currency-research.md#rule-and-approved-exception) and [boundaries](../research/12-fee-currency-research.md#negative-example-and-boundaries). A negative BHD base remains negative after a zero fee. No foreign exchange, separate AED obligation, extra account types or prohibition of negative balances is adopted. New holds still need funds; confirmed debits still post.

## Reversal Compensation

### Principal and affected components

1. **Ambiguity:** Does E9 reverse only E7's principal or also its fee and interest consequences?
2. **Why unclear:** The exercise names a reversal without defining compensation scope; criterion 6 claims blanket restoration.
3. **Decision:** Reverse principal once with supplied dates. Recalculate affected fees and interest from the affected value day onward, including later changed bases within the calculation boundary. Append linked `corrected amount - (original amount + all prior adjustments)`, counting paid ones.
4. **Reason:** Correct the reversed transaction's affected consequences without rewriting history. Recalculation does not automatically refund every fee. Legitimate transactions are assumed; system error correction is excluded.

**Source:** [Study 08, scope](../research/08-reversals-research.md#approved-decisions-and-remaining-limits). E9 credits AED 620.00 with booking Day 6 and value Day 2; supplied order remains unchanged.

### Reversal fee refund dates

1. **Ambiguity:** Should a reversal fee refund use today's value date or the original fee's date?
2. **Why unclear:** Both return money now but reconstruct different historical balances and interest.
3. **Decision:** Use approved method B: book on the correction day and value at the original charge's value date. That date need not equal balance day H or the principal's value date.
4. **Reason:** Neutralize affected fees at their economic dates while retaining when funds were returned. Funds become available when recorded; historical reconstruction does not deliver money in the past.

**Source:** [Study 08, methods](../research/08-reversals-research.md#correcting-fees-and-interest-after-a-reversal) and [source limits](../research/08-reversals-research.md#sources-and-limits). This is a limited exception to late adjustment dates. ISO and Canopy do not mandate the project's policy.

### Interest, payments and earlier decisions

1. **Ambiguity:** Does method B restore every balance, payment and authorization to its earlier state?
2. **Why unclear:** Principal, historical balances, net fees, pending interest and decisions are different records and outcomes.
3. **Decision:** Keep interest corrections dated today and pending, including paid periods. Preserve payments and authorizations, count paid adjustments in differences, and settle components once. Apply the booking cutoff, approved fee base and HALF_UP unchanged.
4. **Reason:** Fee refund dates do not authorize hypothetical capitalization, interest on pending amounts or automatic authorization reevaluation. Criterion 6's blanket restoration remains unsupported; current replay totals remain open.

**Source:** [Study 08, limits](../research/08-reversals-research.md#approved-decisions-and-remaining-limits) and [Study 13, criterion 6](../research/13-acceptance-criteria-research.md#analysis). The [15-day simulation](../research/examples/08-reversal-15-day-simulation.md) uses illustrative checkpoints, Day 10 payment and Day 15 consultation; it establishes no replay totals.

## E10 Installment Allocation

### Conserving the credit

1. **Ambiguity:** How can three BHD installments be equal and still total 10.000?
2. **Why unclear:** BHD 0.001 is the smallest stored unit, and 10,000 units are not divisible by three.
3. **Decision:** Allocate BHD 3.333, 3.333 and 3.334, giving the remaining 0.001 to installment 3. Keep ACC-002 and both supplied Day 5 dates.
4. **Reason:** A fixed remainder position is reproducible, and the final installment completes the total. Positions 1 or 2 would also conserve it; HALF_UP does not choose the position.

**Source:** [Study 11, remainder position](../research/11-installments-research.md#approved-remainder-position) and [source limit](../research/11-installments-research.md#source-and-limit). Mambu illustrates a loan convention, not a rule for E10.

### Late receipt and interest

1. **Ambiguity:** How does E10 affect Day 5 interest if its credit arrives after that calculation?
2. **Why unclear:** Its supplied Day 5 dates do not tell when it actually arrives.
3. **Decision:** E10 arrives on Day 6 after E9 and after Day 5 interest was recorded without it. Correct ACC-002's Day 5 interest from BHD 0.000 to 0.004; append +0.004 with both dates Day 6.
4. **Reason:** The approved receipt scenario preserves supplied order and dates. The correction misses Day 6's booking cutoff and remains pending until the next eligible monthly payment.

**Source:** [Study 06, approved receipt scenario](../research/06-daily-closing-research.md#receipt-and-missing-inputs). General input completeness and final results after E10 remain open.

### Installment representation

1. **Ambiguity:** Is E10 one event, three credits or another record structure?
2. **Why unclear:** The exercise requires three installments without specifying their representation or IDs.
3. **Decision:** **Open:** representation, IDs, event count and counter effects. If installments are individual financial credits, link them to E10 and never credit the full parent amount again.
4. **Reason:** Allocation alone does not define an event model. No installment calendar, interest between installments or general allocation algorithm has been approved.

**Source:** [Study 11, limits](../research/11-installments-research.md#source-and-limit).

## Pending Calculation Decisions

### Schedule and review checkpoints

1. **Ambiguity:** When exactly do jobs and historical reviews run?
2. **Why unclear:** The daily D-1 rule and E10 receipt scenario do not set all replay checkpoints.
3. **Decision:** **Open:** business time zone, clock times and general checkpoints. Midnight, a 00:30 start, review after E7 before E8 and review after E9 remain proposals. E8 does not close Day 5.
4. **Reason:** No complete schedule has been approved. Illustrative examples cannot establish replay timing.

**Source:** [Study 06, open decisions](../research/06-daily-closing-research.md#decisions-still-open).

### Complete eligible inputs

1. **Ambiguity:** How is a complete calculation input set established?
2. **Why unclear:** A valid date or source counter does not prove all eligible approved events have arrived.
3. **Decision:** **Open:** general handling of missing cutoff-eligible records and establishing complete approved inputs for each source account counter. E10 resolves only its stated scenario.
4. **Reason:** Calculation boundaries are approved; a completeness mechanism is not.

**Source:** [Study 06, open decisions](../research/06-daily-closing-research.md#decisions-still-open) and [architecture, input limit](../architecture.md#calculation-version-validation).

### Calculation record validation

1. **Ambiguity:** How are accrual and correction records protected from changes during calculation?
2. **Why unclear:** Authorization's payment check does not define validation of eligible inputs or previous calculation results.
3. **Decision:** **Open:** validate cutoff-relevant inputs and prior results, record indivisibly and retry on change. This remains a proposal separate from the approved Authorization source counter check.
4. **Reason:** The calculation-record mechanism has not been selected. Excluded bookings need not change its financial result but still can invalidate an Authorization payment attempt.

**Source:** [Study 06, calculation-record concurrency](../research/06-daily-closing-research.md#decisions-still-open).

### Final replay totals

1. **Ambiguity:** What are the final fee counts, capitalization amounts and balances?
2. **Why unclear:** Remaining checkpoints and input completeness affect which corrections and payments are recorded.
3. **Decision:** **Open:** final replay totals, including E7's fee count and the proposal to calculate final results after E10. Criterion 2's exactly-one-fee claim is unresolved for this replay.
4. **Reason:** Historical counterfactuals and fictional examples establish no approved replay total.

**Source:** [Study 06, open totals](../research/06-daily-closing-research.md#decisions-still-open) and [Study 13, criterion 2](../research/13-acceptance-criteria-research.md#analysis).

### Actual business calendar

1. **Ambiguity:** Which real dates count as monthly business days?
2. **Why unclear:** The approved synthetic mapping names no actual month, year, jurisdiction or holidays.
3. **Decision:** **Open:** actual month, year and business day calendar. Preserve Day 6 as the new month's first business day and its ordinary interest payment in the following month.
4. **Reason:** The scenario mapping establishes the payment period, not future calendar dates.

**Source:** [Study 10, monthly payment](../research/10-interest-capitalization-research.md#approved-monthly-payment).

### Zero settlement protocol

1. **Ambiguity:** How is a zero interest settlement recorded and validated?
2. **Why unclear:** Components must settle once without a financial movement, while the payment contract covers credits and debits.
3. **Decision:** **Open:** representation, IDs, validation, snapshots and counter effects. Keep the approved zero financial movement and component settlement policy.
4. **Reason:** Financial policy alone does not select this protocol.

**Source:** [Study 10, architecture relation](../research/10-interest-capitalization-research.md#relation-to-the-architecture).

## System and Domain Boundaries

### One system with explicit modules

1. **Ambiguity:** Should Ledger, Authorization and Yield and Fees be combined or separate systems?
2. **Why unclear:** The exercise requires an in-memory core but supplies no module design; detailed Part 2 instructions are unavailable.
3. **Decision:** Use one in-memory system with three domain modules, explicit interfaces and ownership of their records and behavior. Keep taxes on yield and tax changes outside scope; preserve the supplied interest rate.
4. **Reason:** Clear responsibilities make balances, holds and calculations easier to explain and test. One system limits coordination overhead.

**Source:** [Architecture, scope](../architecture.md#scope-and-tradeoffs). Process count, primitives, deployment topology, language and technology remain unspecified. Current Part 2 coverage uses only its supplied title and evaluation guidance.

### Transaction entry and test reporting

1. **Ambiguity:** Should the Transaction module coordinate replay and query every domain?
2. **Why unclear:** Submitting a transaction and inspecting daily test results serve different purposes.
3. **Decision:** Transaction communicates only with Authorization. After recording, Authorization forwards approved transactions to Yield and Fees and committed financial movements to Ledger. A separate test suite or script preserves replay order and inspects daily outputs.
4. **Reason:** Keep entry, financial ownership and test reporting distinct. This replaces the earlier replay-coordinator module with direct Ledger and Yield access.

**Source:** [Architecture, entry and replay](../architecture.md#transaction-entry-and-test-replay) and [component view](../architecture.md#c3-component-view).

## Ledger Bookkeeping

### Balanced financial entries

1. **Ambiguity:** How should Ledger represent each received money movement?
2. **Why unclear:** The exercise specifies customer balance changes but no bookkeeping model.
3. **Decision:** Use double-entry bookkeeping: equal debit and credit postings in the same currency, kept together as one balanced journal entry. Never record only one side. Ledger records after Authorization's separate operation.
4. **Reason:** Make each movement's counterpart and balanced totals explicit. This is an approved architecture choice, not an exercise requirement.

**Source:** [Architecture, domains](../architecture.md#domains-and-module-boundaries) and [OpenStax, section 3.1](https://openstax.org/books/principles-financial-accounting/pages/3-1-describe-principles-assumptions-and-concepts-of-accounting-and-their-relationship-to-financial-statements). OpenStax is a conceptual reference, not a jurisdictional mandate.

### Book accounts and event mapping

1. **Ambiguity:** Which book accounts and debit or credit postings represent each exercise event?
2. **Why unclear:** Book accounts need not be customer accounts. Accounting debit and credit do not universally mean money leaving and entering.
3. **Decision:** **Open:** chart of accounts, event-to-posting mapping and how ACC-001 and ACC-002 relate to book accounts. Keeping both postings together selects no implementation primitive.
4. **Reason:** The balanced-entry choice does not define those mappings. Exercise CREDIT and DEBIT labels describe customer balance changes only.

**Source:** [Architecture, Ledger ownership](../architecture.md#domains-and-module-boundaries) and the [recorded architecture contract](WORKLOG.md#09-september-2026-001355).
