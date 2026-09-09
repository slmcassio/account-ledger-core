# Public APIs

Run these APIs in one JVM. Module constructors return opaque handles. Application callers use each module's `api` namespace; `domain` contains pure rules and `memory` is the local adapter. `!` denotes an effect. All queries return immutable values and cause no delivery, recalculation or posting.

## Configuration and money

`account-ledger.replay/config` contains the two exercise accounts and their account types. A config has `:accounts`, keyed by account ID, and `:account-types`, keyed by type. Each account supplies `:money/currency`, `:opening-balance` and `:account/type`. Each type supplies its own matching currency and `:daily-fee`. AED uses 25.00 and BHD uses the explicit zero-fee exception. Constructors reject unknown types, mismatched currencies, missing values and invalid precision. Opening state is version/journal position zero, not a transaction.

`account-ledger.money/amount [currency decimal]` requires a BigDecimal exactly representable at the currency scale. Use `250.00M` in Clojure, never `250.00` binary floating point. `daily-interest [currency base]` applies the exact 0.0004 product and one HALF_UP rounding; nonpositive balances earn zero. `allocate-three [currency total]` puts the remainder into the third installment; all three must be positive.

## Authorization

| Function | Contract |
|---|---|
| `(create config)` | Validates config and creates isolated operational state. |
| `(submit! module command)` | Atomically checks identity, validates new input, calculates against current state and records the decision/transaction. |
| `(snapshot module account-id)` | Returns financial balance, held amount, available balance, active `:holds`, currency and `:last-event-counter`. |
| `(history module account-id)` | Returns immutable ordered outcomes, original commands and recorded events/snapshots. Includes declined decisions. |
| `(pending-deliveries module)` | Returns ordered `{:delivery/id [transaction-id destination] :destination :ledger\|:yield :event committed-event}` envelopes. |
| `(ack-delivery! module delivery-id)` | Confirms one recipient idempotently; unknown IDs return `:acknowledged? false` and `:reason :unknown-delivery`. Business history/counters stay unchanged. |

New commands require a nonempty string `:transaction/id`, known `:account/id`, matching `:money/currency`, positive integer `:booking-day`, `:value-day`, `:received-day`, and a supported `:transaction/type`. Booking cannot follow receipt. Value may precede booking. All amount-bearing external operations require positive BigDecimal `:money/amount`; normalization never rounds money. Generated event fields cannot be supplied as new command inputs.

| Type | Additional fields and effect |
|---|---|
| `:credit` / `:debit` | Positive amount; changes financial balance. Optional `:installment-count 3` is supported on credits. |
| `:authorization` | `:authorization/id`; creates a hold only if remaining availability is nonnegative. |
| `:settlement` | `:authorization/id`, explicit boolean `:settlement/final?`; always debits the actual confirmed amount. Final ends the remaining hold; partial retains any unused reservation. Missing/inactive correspondence produces an occurrence. |
| `:release` | `:authorization/id` and amount no greater than the active reservation; releases funds without a journal movement. |
| `:reversal` | `:reversal/of` identifies a recorded same-account principal movement. Derives and negates its effect once. Optional amount must agree exactly; no hold decision is reevaluated. |

Authorization IDs identify one lifecycle per account. A new explicit request uses a new authorization ID. Transaction IDs are globally unique in Authorization, including declines. A known transaction ID is ignored before inspecting any changed/malformed payload. An unknown invalid or stale request is not reserved.

Results have `:outcome`:

* `:recorded`: local effect confirmed, with `:recorded/event` and `:recorded/snapshot`. It does not imply downstream delivery.
* `:declined`: decision and ID recorded; no hold, money, snapshot, counter or delivery.
* `:duplicate`: `:original/result` is the prior outcome. Callers must use its original amount/links.
* `:retry-required`: an unrecorded financial source mismatch (`:stale-source`); refresh input and recompute with the same ID.
* `:invalid`: stable `:reason` describes the rejected boundary/reference. No operational effect.

Approved events retain ID/dates/purpose, assigned `:event-counter`, signed `:financial/effect`, occurrences and relevant links. Hold-only events have zero financial effect and go only to Yield. E10 adds `:installments`; it remains one event. Operational balances are not in the event feed.

Internal fee/interest commands use reserved `system/` IDs, `:credit` or `:debit`, `:purpose :fee` or `:interest`, current `:source-event-counter`, `:reference-day` and nonempty `:component/ids`. They still require the ordinary amount/currency/dates.

Fee commands additionally require `:fee/assessment` with `:component/id`, `:component/type` (`:ordinary` or `:adjustment`), signed `:money/amount`, `:reference-day`, `:booking-day` and `:value-day`. The command's `:component/ids` must contain exactly that component ID. Reference, booking/value days and any `:cause/transaction-id` must agree between command and assessment. The assessment amount and `:fee/difference` must equal the charge amount for a debit or its negative for a refund credit. An optional assessment `:source-event-counter` must match the command; optional `:booking-cutoff` must equal booking day minus one. Yield records the confirmed command's source counter even if the assessment omits it. Missing or inconsistent assessment metadata returns `:invalid` with `:reason :invalid-fee-assessment` before any effect or ID reservation. Ordinary generated fee commands already include this metadata.

Interest commands require `:settlement/id`, `:period/end-day` and `:booking-cutoff` so a committed event can reconstruct the original receipt. The reference day equals the period end; the booking cutoff equals booking day minus one; the period end cannot follow that cutoff. Value day equals booking day, the actual settlement run day. Yield-generated commands already satisfy these requirements.

Source version checking and recording are one local atomic operation. A decline or a different account's event does not change that account version.

## Ledger

| Function | Contract |
|---|---|
| `(create config)` | Creates an empty journal and opening state. |
| `(post! module committed-movement)` | Validates and atomically appends a balanced journal, or returns `:duplicate` / `:invalid`. Deduplicates by original transaction ID before payload validation. |
| `(balance module query)` | Returns `{:account/id id :money/amount amount :money/currency currency :query effective-query}`. |
| `(journal module account-id)` | Returns that account's immutable entries in global local append order. |

A balance query requires `:account/id`, `:value-through-day` and `:booking-through-day`. Optional `:as-of-journal-position` bounds what had been appended locally. Its default is the actual current position and is included in the returned query; future positions are rejected. Position zero returns opening state. Position is neither receipt day nor an account snapshot counter.

Entries preserve original metadata and add `:journal/position` and `:postings`. Postings identify `:book/account`, `:side`, positive `:money/amount`, currency and, for E10, `:installment/position`. Customer liabilities are `customer/<id>`; clearing is `clearing/<currency>`. Every pair balances in its currency. E10 contains six postings, three customer credits, and no parent credit. Holds/releases cannot post a journal. Invalid constructor/query inputs throw `ExceptionInfo`; an invalid financial delivery returns `:invalid` without appending anything.

## Yield and Fees

| Function | Contract |
|---|---|
| `(create config ports)` | Requires function ports `:submit-financial! [command]` and `:flush-deliveries! []`. |
| `(receive! module approved-event)` | Returns recorded/duplicate/invalid receipt. Buffers out-of-order counters; confirms original fee assessments and interest settlement links from committed events. |
| `(calculate! module request)` | Runs one serial daily or historical job for one account, producing fee records and pending interest components. |
| `(settle! module request)` | Selects eligible unpaid components, settles their signed total once and returns an immutable receipt. |
| `(report module account-id)` | Returns source prefix/readiness, components, fees, settlement receipts, paid/pending interest, saved financial intents and unresolved commands. |

Daily request:

```clojure
{:account/id "ACC-001" :run-day 2 :reference-day 1
 :booking-cutoff 1 :mode :daily}
```

The reference and booking cutoff must both be D-1. Ordinary fees are booked/valued H+1. A repeated unchanged job appends nothing; changed targets require an explicit historical cause.

Historical request:

```clojure
{:account/id "ACC-001" :run-day 6 :booking-cutoff 5 :mode :historical
 :from-day 2 :through-day 4 :cause/transaction-id "E7"}
```

All referenced days must already be assessed and no later than the cutoff; the cause must exist in the same account's eligible history. Periods are reviewed in order. Fees compare corrected targets with original amounts plus every adjustment; only nonzero differences move funds. Each fee is confirmed and delivered before the next proposal is recomputed. Interest differences compare rounded targets and remain pending. Reversal refunds use the original charge's value date and the actual correction booking day; interest adjustments retain both current dates.

Only [example 08](examples/08-daily-closing.md#executable-historical-views) uses optional `:input-through-event-counter` with `:interest-only? true` for an explicit earlier principal-input view. Both keys are required together and only in historical mode. It creates no fee proposal and cannot regress a later recorded view. Default jobs use all eligible inputs.

Settlement request:

```clojure
{:account/id "ACC-001" :settlement/id "month-one/ACC-001"
 :run-day 6 :period/end-day 5 :booking-cutoff 5}
```

The request supplies no payment amount. Ordinary components through the period end are eligible; adjustments also require booking through the cutoff. Current-month ordinary components remain pending. Positive totals credit; negative totals debit even into negative funds; zero totals link components in a local receipt without a financial command. A stale payment settles nothing. A confirmed original payment settles only its original links even if a retry would choose a different total. Settlement IDs are global within Yield. A recorded receipt returned again is a duplicate.

Before calling `:submit-financial!`, Yield atomically appends the complete command to `:financial-intents` and keeps the same map in `:pending-financial-commands`. The command fixes its ID, amount, dates, component links and source counter. Interest components are already saved; a fee command includes its calculated assessment. These report fields are vectors of command maps. Saving an intent alone neither confirms a fee nor settles interest.

If submission throws or leaves its outcome unknown, retrying that interest settlement ID resends the exact saved map, including the original dates, even if the supplied request or available components have changed. A pending fee is retried before the next calculation replaces its assessment. While a command remains unresolved, a different financial command or new settlement, including a zero settlement, returns `:retry-required` with `:reason :pending-financial-command`. Pure interest calculation may still append components, but the report remains incomplete.

An explicit `:invalid` result or `:retry-required` with `:reason :stale-source` proves the command was not recorded. Yield clears its pending entry and preserves the proposal in `:financial-intents`; a later attempt may recalculate and save another proposal with the same unrecorded ID. A stale source requires fresh calculation, not replacing only the saved counter. Retrying an unresolved command does not append another intent.

A recorded result, its confirmed duplicate, or delivery of the committed event confirms the original assessment or settlement and clears that command's pending entry in one local transition. Interest delivery creates the receipt using the original settlement ID, signed amount, dates and component links without requiring a retry of that ID. A later settlement therefore cannot pay those same components again. Positive and negative payments use this same confirmation path.

Each component preserves ID, type (`:ordinary`/`:adjustment`), reference, booking/value days and signed amount, with calculation/cause metadata. Receipts link selected IDs; earlier components are never marked or rewritten. Previously paid adjustments remain part of later target comparisons. `:complete?` on Yield's report requires both a contiguous known local account prefix and no pending financial command. System reporting exposes the same intent fields and also checks pending deliveries. Accepted payments can have `:complete? false` when their delivery remains unresolved.

Expected validation, stale-view and delivery outcomes are data. Broken invariants/technical failures remain visible exceptions. Jobs are serial; concurrent job orchestration and restart recovery are outside scope.

## Composition and replay

`account-ledger.transaction/submit! [authorization-module command]` calls only Authorization.

`account-ledger.system` exposes `create [config]`, `submit! [system command]`, `drain! [system]`, `run-day! [system request]`, `settle! [system request]` and `report [system query]`. The system map contains opaque module handles for test/replay composition. It never edits module state.

Drain tries each pending recipient once, acknowledges only recorded/duplicate receipt and returns `{:pending-count n :errors [...]}`. Errors identify delivery IDs. Explicit retry resends original recorded events. `run-day!` and `settle!` drain before invoking Yield and refuse incomplete input. `report` requires `{:account/id id :day day}`, optionally with Ledger temporal fields. It is a pure operational view at capture time and includes a separately bounded `:accounting` result; supplying an earlier `:day` does not reconstruct an earlier operational snapshot. Preserve captured reports for that purpose.

Working example from the worktree root:

```clojure
(require '[account-ledger.system :as system]
         '[account-ledger.replay :as replay])
(def app (system/create replay/config))
(system/submit! app {:transaction/id "demo-credit" :transaction/type :credit
                    :account/id "ACC-001" :money/currency :AED :money/amount 250M
                    :booking-day 1 :value-day 1 :received-day 1})
(system/run-day! app {:account/id "ACC-001" :run-day 2 :reference-day 1
                     :booking-cutoff 1 :mode :daily})
(system/settle! app {:account/id "ACC-001" :settlement/id "demo-month"
                    :run-day 2 :booking-cutoff 1 :period/end-day 1})
(select-keys (system/report app {:account/id "ACC-001" :day 2})
             [:financial-balance :interest-paid :pending-interest :complete?])
;; => {:financial-balance 250.10M, :interest-paid 0.10M,
;;     :pending-interest 0.00M, :complete? true}
```

`replay/run []` returns `{:system system :daily-reports twelve-reports :summary by-account}`. `continue-day-seven [system]` returns the separate later report/summary and preserves the captured six-day values. `clojure -M:demo` runs both and labels the boundary.
