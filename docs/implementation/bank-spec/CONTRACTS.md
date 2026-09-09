# BANK-SPEC frozen interfaces

Read SPEC section 7 first. These concrete map choices refine its shared schemas. The coordinator owns this file and `src/account_ledger/contracts.clj`; tell the coordinator before changing a shared field. Constructors validate config and return opaque handles, never a public atom. Domain helpers may consume persistent maps; only adapters own mutable storage.

## Configuration

`{:accounts {"ACC-001" {:money/currency :AED :opening-balance 0.00M :account/type :aed-standard}, "ACC-002" {:money/currency :BHD :opening-balance 0.000M :account/type :bhd-exempt}}, :account-types {:aed-standard {:money/currency :AED :daily-fee 25.00M}, :bhd-exempt {:money/currency :BHD :daily-fee 0.000M}}}`.

`contracts/normalize-config` validates and normalizes config, copying each type's configured `:daily-fee` into its account map for domain use. `contracts/validate-command` returns `{:valid? true :command normalized}` or `{:valid? false :reason keyword}`. Authorization calls it only after duplicate lookup. Reversals derive their amount from the referenced principal movement; no reversal amount is required. If supplied, it must agree exactly with the original. Reference existence and lifecycle validation belong to Authorization.

## Events and results

Every approved snapshot event retains the command fields and adds `:event-counter`, signed `:financial/effect` (zero for hold/release), `:purpose` (`:principal`, `:fee`, or `:interest`), and `:occurrences` (a vector). No operational balance is forwarded. E10 adds `:installments [3.333M 3.333M 3.334M]`. Reversals retain `:reversal/of` and carry the inverse original effect.

Financial commands from Yield use `:credit` or `:debit`, positive `:money/amount`, reserved `system/` IDs, `:source-event-counter`, `:purpose`, `:reference-day`, and nonempty `:component/ids`.

Fee commands also require signed `:fee/difference` (positive obligation, negative refund) and `:fee/assessment`. The assessment requires `:component/id`, `:component/type` (`:ordinary` or `:adjustment`), signed `:money/amount`, `:reference-day`, `:booking-day` and `:value-day`. Its ID must be the command's sole `:component/ids` entry; its dates, reference and any `:cause/transaction-id` must match the command. Its amount and `:fee/difference` must equal the normalized debit amount or the negative credit amount. Optional assessment `:source-event-counter` must match the command; optional `:booking-cutoff` equals booking day minus one. Confirmed fee records derive their source counter from the accepted command, including when the nested field is omitted. Invalid assessment metadata returns `:invalid-fee-assessment` before recording.

Generated corrections carry `:cause/transaction-id`, and refunds may carry `:fee/original-value-day`. Preserve these fields unchanged in committed events. Fee and interest amounts must be normalized to currency scale. An external command cannot use a reserved ID without the complete internal financial contract.

Interest commands require `:settlement/id`, `:period/end-day` and `:booking-cutoff`. Treat command `:booking-day` as the settlement's run day: cutoff equals booking day minus one, period end is no later than cutoff, `:reference-day` equals period end, and `:value-day` equals booking day. These fields let a delivered event confirm the original receipt without a new caller request. Yield-generated commands already contain them.

Recorded results include `:recorded/event`; duplicate results include `:original/result` and the original identity. An unknown stale ID returns `:retry-required` with `:reason :stale-source`; it is not recorded. History is an immutable vector of outcome records, including declines, containing original command and `:recorded/event` / `:recorded/snapshot` when recorded. Snapshot includes the SPEC fields, plus `:money/currency`, `:holds` (authorization ID to remaining amount), and `:authorizations` (authorization ID to state such as `:approved`, `:declined`, `:settled`, `:released`), if needed for reports. Authorization may expose decisions through history instead of duplicating this index; report composition derives those views.

Ledger `balance` returns `{:account/id id :money/amount decimal :money/currency currency :query effective-query}`. `journal` returns immutable entries with `:journal/position` (one-based global local append position), original event metadata and `:postings`. Each posting has `:book/account`, `:side :debit|:credit`, `:money/amount`, `:money/currency`, and `:installment/position` for E10. Customer book ID is `customer/<account-id>`; counterpart is `clearing/<currency-name>`. Customer balances are credits minus debits. Opening balances are config, position 0, not synthetic transactions.

## Yield reports and jobs

Every calculation and settlement request targets `:account/id`. Daily request: `{:account/id id :run-day D :reference-day (dec D) :booking-cutoff (dec D) :mode :daily}`. Historical request: `{:account/id id :run-day D :booking-cutoff (dec D) :mode :historical :from-day H :through-day J :cause/transaction-id cause}`. Review only already assessed days; missing assessments are an invalid request. The cause must exist in the eligible received events. All normal jobs are serial.

Report minimum: `:account/id`, `:money/currency`, `:source-event-counter` (contiguous prefix), `:complete?` (no known gaps or pending financial command), `:interest/components`, `:fees`, `:settlements`, `:interest-paid`, `:pending-interest`, `:financial-intents`, `:pending-financial-commands`. Components have `:component/id`, `:component/type` (`:ordinary` or `:adjustment`), `:reference-day`, `:booking-day`, `:value-day`, signed `:money/amount`, and optional cause link. Ordinary component recording day is H+1. It is eligible at payment when its reference day is within `:period/end-day`; adjustments additionally require their booking day through the cutoff. Current-month ordinary amounts stay unpaid. Preserve all components and use immutable receipt links, not changes to earlier components, to derive paid status.

Fee records retain zero assessments, original amount and nonzero differences with reference and assessment days. Use fields `:reference-day`, `:booking-day`, `:value-day`, `:money/amount` (signed obligation), `:component/id`, and `:cause/transaction-id` when applicable. The API may include further explanatory fields without changing these meanings.

Settlement receipts contain `:settlement/id`, `:component/ids`, signed `:money/amount`, `:run-day`, `:period/end-day` and `:booking-cutoff`; nonzero receipts also identify the original Authorization transaction. A recorded payment duplicate settles only its original links. A zero receipt advances no Authorization counter.

Before `:submit-financial!`, append the complete command to local `:financial-intents` and retain the same command in the pending index. Both report fields return vectors of command maps; the pending vector has at most one command per account under serial jobs. Interest commands link already saved calculation components; fee commands include their assessment. The intent itself confirms no fee or payment.

* Unknown outcomes and exceptions retain the exact pending map. Retry of the same settlement ID resends it with its original amount, dates, links and counter; retry appends no new intent. A calculation first resumes any pending fee. Different financial commands and new zero settlements return `:retry-required` with `:reason :pending-financial-command` while unresolved.
* Definite `:invalid` or `:retry-required`/`:stale-source` clears pending state without deleting proposal history. A later attempt can recalculate from fresh inputs and append a new proposal with the same unrecorded ID. Do not replace only the source counter.
* A recorded result, confirmed duplicate or delivered committed event appends the original fee assessment or interest receipt and clears the pending command atomically. Interest delivery uses the event's original settlement ID and signed amount without requiring that settlement to be called again. Components confirmed in that receipt cannot enter another settlement.

Pure accruals may append components during an unknown payment, but cannot replace its saved command or make the report complete.

Use SPEC's pending-delivery, acknowledgement, drain and Yield port signatures exactly. Never hold a module state lock across a call to another module. A failed delivery returns errors and remains pending; no loop spins until success. For calculation or payment, a known gap, unresolved financial command or unresolved delivery prevents a complete result.

## Reviewed refinements

New commands reject `contracts/computed-event-fields`; committed recipients preserve and validate those generated fields separately. Three positive installment postings require at least three minor units. Ledger returns a fixed current journal position when omitted and rejects future positions.

Example 08 alone uses optional `:input-through-event-counter` on historical requests, which requires `:interest-only? true`. It bounds principal inputs for an explicit earlier historical view, retains eligible derived financial effects, and creates no fee proposals. Every receipt is still recorded on its supplied day. Per-reference review bounds cannot decrease. Default historical jobs continue to use the entire eligible feed. See [decision rationale](../../../agent-decisions.md#boundary-and-review-refinements).
