# Worklog

## Recording Method

The initial retrospective log was recorded on 07 September 2026 at 11:48:18 in America/Sao_Paulo, whose UTC offset is minus three hours on this date.

Commit milestones use the author and committer timestamps verified in Git history. These timestamps mark recorded repository changes. Earlier discussions are documented separately because their individual start and completion times were not captured. No work durations or missing timestamps have been reconstructed.

Later dated activity entries identify when the summary was recorded, not when the underlying work started or ended. They use the same time zone.

## Research and Documentation Updates

### 08 September 2026, 22:15:42

* The user approved overdraft fees configured by account type in the account's own currency: AED 25.00 for ACC-001's type and BHD 0.000 for ACC-002's type. Recorded the zero BHD fee as an explicit exception to the exercise's literal mandatory fee, chosen for simplicity because conversion requirements are missing. Updated [study 12](../research/12-fee-currency-research.md), ambiguity decisions, business rules, numerical notes, README and references in studies 06/07.
* Revalidated the committed studies 10 and 11 before integration. Preserved E10 after E9 with both dates on Day 5, its approved installment allocation, the authorization funding requirement and legitimate confirmed debits. Principal remains separate from final balances with interest. Study 06's open timing and input questions and study 10's unresolved calendar mapping and negative payable policy remain unchanged.
* Independent review found no actionable issues. Checks completed so far covered the diff, whitespace, 114 local links, 55 anchors, five decimal calculations and preservation of the primary checkout's 31-file snapshot. The approved study 12 hash, studies 10/11 and the exercise statement were verified. No executable ledger or test suite exists.
* This entry records completed work at the current America/Sao_Paulo recording time, with no inferred execution duration. The user requested a commit and push; neither is claimed complete here. The local commit follows final verification.

### 08 September 2026, 22:07:50

* Reviewed and simplified [study 11](../research/11-installments-research.md), separating exact conservation of E10's BHD 10.000 credit from the convention for placing the remaining BHD 0.001. The user approved installment 3, giving BHD 3.333, 3.333 and 3.334; the other positions remain valid alternatives.
* Preserved ACC-002, both Day 5 dates and E10 after E9. Explained that individual installment credits must not be followed by another credit for the full parent amount. No installment calendar or interest between installments was introduced. The Mambu loan example remains illustrative, not a mandatory rule for E10.
* Prepared integration in fresh isolation from the verified study 10 commit. Added the approved decision, numerical inputs and derived results, criterion 7's contradiction (`3 * 3.334 = 10.002`), the README link and a narrow business-rule reference update. Used current common documents and preserved the earlier studies, preexisting AGENTS.md change and unrelated untracked files.
* Independent review found no actionable issues. Verified exact arithmetic, local links and anchors, whitespace and the focused diff; coordination also checked preservation of the primary checkout. No executable ledger or test suite exists, and no implementation was introduced.
* The user authorized a signed commit and push to main. This entry records completed preparation before those actions, not their completion. Recording time is America/Sao_Paulo; no execution duration is inferred.

### 08 September 2026, 21:58:21

* Reviewed [study 10](../research/10-interest-capitalization-research.md) against the committed studies 06 through 09. The user approved booking and valuing interest credits on the actual payment day, and monthly payment on the first business day for the previous month's ordinary accruals plus eligible unpaid adjustments. A payment does not change the previous day's interest calculated on that payment day.
* Recorded actual system receipt separately from supplied booking and value dates, without inventing receipt moments. Added the pragmatic rationale for excluding pending interest from earning interest, with explicit regulatory uncertainty and the limits of the CBUAE disclosure source. The exercise's 0.04% daily rate remains a supplied requirement.
* At the user's request, removed duplicated explanations: study 06 now covers receipt, processing, late inputs and corrections; study 10 covers payment, capitalization and financial limits. Study 06 changed from 884 to 635 words and study 10 from 822 to 518. Aligned ambiguity decisions, business rules, numerical notes, study dependencies and the README link.
* Preserved unresolved E10 completeness and checkpoint choices, negative payable settlement, business day calendar and mapping of synthetic Days 1 through 6 to the monthly schedule. The fixed Day 6 credit remains required. Study 06's ordinary checkpoints, fee assessment dates and final E7 fee counts remain open. Authorization to publish this documentary state does not approve those proposals.
* Independent review found no actionable issues. Coordination verified 193 local links and anchors, nine arithmetic checks, the diff and whitespace, and preservation of the original checkout's 31-file snapshot. No executable ledger or test suite exists; these were documentation and arithmetic checks. No ledger implementation or study 11 work was performed.
* The user explicitly authorized a signed commit and push to main. This entry records completed preparation before commit and publication, not their completion. Recording time is America/Sao_Paulo; no execution duration is inferred.

### 08 September 2026, 20:43:13

* The user approved exact daily interest multiplication followed by one HALF_UP currency rounding, with no intermediate rounding or fractions carried between days. Updated [study 09](../research/09-daily-interest-research.md) and aligned ambiguity decisions, business rules, numerical notes, rejected criterion 8 and alternatives, README and references in studies 01/06/08.
* At the user's request, shortened study 09 to 441 words and explained corrections as the corrected daily amount minus the total already recorded: the original accrual and all previous adjustments, including paid ones. The small example distinguishes a new 0.10 adjustment from the 1.20 already recorded. Every interest correction remains pending; preserve actual payments and settle eligible unpaid components once.
* Kept study 06's checkpoints, ordinary assessment dates, missing eligible inputs and final E7 fee count open, along with study 10's payment ordering and remaining payment details. Preserved the approved booking cutoff and historical value dates for reversal fee refunds. No language, payment calendar or negative payable policy was adopted.
* Independent review found no actionable issues. Coordination checked 104 local links, 58 anchors, the diff and whitespace, six new arithmetic checks and preservation of the original checkout's 31-file snapshot; 21 study calculations were verified in the preceding review. No executable ledger or test suite exists. This records completed work before the authorized signed local commit, not a completed commit or publication. Recording time is America/Sao_Paulo; no execution duration is inferred.

### 08 September 2026, 20:19:53

* Reviewed and simplified [study 08](../research/08-reversals-research.md) through the principal, fee and interest comparisons. Kept a single-fee explanation in the study and preserved the complete [15-day calculation](../research/examples/08-reversal-15-day-simulation.md) separately.
* The user approved recalculating all affected fees and interest and method B for reversal fee refunds: booking on the actual correction day, value on the original charge's value date. Recorded the legitimate-transaction assumption, the limited exception to late transaction dating, and the unchanged pending treatment for all interest corrections, including paid periods. Preserve actual payments and settle each component once.
* Aligned the ambiguity decisions, business rules, numerical rationale, rejected alternatives, README and studies 06/07. Study 06's checkpoints, ordinary assessment dates, missing eligible inputs and final E7 fee count remain open; the user accepted the numerical dependencies on studies 09 and 10. No payment calendar or negative payable policy was added.
* Independent review rechecked the ISO 20022 and Canopy sources and their limits, the example calculations and document consistency. Coordinated checks covered 88 local links and anchors, 14 Markdown tables, whitespace, the final diff and preservation of the 30-file original-checkout snapshot. No executable ledger or test suite exists.
* This summary records completed work before the authorized signed commit on local main, not completion of that commit. No push or PR is authorized for study 08. Recording time is America/Sao_Paulo; no execution duration is inferred.

### 08 September 2026, 00:00:17

* Simplified [study 07](../research/07-overdraft-fees-research.md) from 1,333 to 546 words, removing duplicated explanations and process details while preserving approved policies, both examples, and accepted open dependencies.
* Reviewed the editorial diff and verified six Decimal calculations, local links and anchors, and whitespace. No executable ledger tests exist.
* The user authorized amending the unpushed study 07 commit with this revision and worklog entry. This records the authorization before the amend, not its completion.

### 07 September 2026, 23:52:53

* Reviewed [study 07](../research/07-overdraft-fees-research.md) against the signed, verified study 06 commit. Preserved the approved exclusion of only the assessed period's own fee components and adjustments already included in its base, applying the cumulative booking cutoff before value dates.
* Replaced earlier immediate closing examples with explicit historical calculation snapshots. Kept all prior adjustments in each component's recorded net without admitting them to a historical balance outside their actual dates. Pending interest remains separate from the ledger.
* Recorded the approved assessment decision and rationale in [AMBIGUITIES](AMBIGUITIES.md#overdraft-fee-assessment-base), business rules, numerical notes, and README. Updated study 06's dependency note because study 07 resolves the base, not the final assessment count.
* Preserved the accepted open checkpoints, ordinary assessment dates, and missing eligible input handling from study 06, plus reversal compensation for study 08, capitalization order for study 10, and the negative BHD case for study 12. No new material decision was required for study 07's independent scope.
* Independent review verified the existing Mambu source and its limits, the final study diff, 10 Decimal calculations, four local links and anchors, and three formatting checks. Finalization checked 18 Decimal calculations; the coordinated documentation check passed 58 local links and anchors, whitespace, and preservation of 30 original checkout files. No executable ledger or test suite exists yet.
* Prepared a focused signed commit on local main under the user's existing conditional authorization. This entry records completed work immediately before that commit; it does not assert that the commit or any publication has occurred. No push is authorized for study 07.

### 07 September 2026, 23:28:06

* Reviewed and simplified [study 06](../research/06-daily-closing-research.md). Recorded the daily D-1 job and cumulative booking cutoff while preserving supplied value dates, event order, and current balance updates.
* Documented the approved pending interest treatment in [AMBIGUITIES](AMBIGUITIES.md#interest-adjustments-wait-for-payment): record corrections on the actual correction day, including differences for previously paid periods, and settle them once at the next eligible regular payment. Pending interest is unavailable and earns no interest.
* Added the D30 correction example and aligned business rules, study 02, numerical notes, and the rejection of historical adjustment dates. Explicitly identified examples 04 and 08 as earlier scenarios awaiting numerical alignment with the booking cutoff.
* The user approved study 06 for now with its recorded open items and dependencies pending, to revisit when a later study affects it. Remaining proposals were not adopted.
* Reviewed the documentation diff, local links and anchors, whitespace, and Decimal arithmetic. Independent review checked consistency. No executable ledger or test suite exists yet.
* The user authorized a signed commit and push restricted to study 06 and its related changes. This entry records that authorization before either action is performed.

### 07 September 2026, 20:36:37

* Reviewed and simplified the [daily closing research](../research/06-daily-closing-research.md), following the style of studies 01 through 04. Added a [complete fictional example](../examples/08-daily-closing.md) covering arrivals during and after calculation, repeated calculations, and duplicate delivery.
* Recorded the approved active processing approach, its numerical example, and the rejected alternative of waiting for all input before calculating. Kept the daily schedule, business time zone, recording validation, and replay checkpoints as proposals. Preserved the existing adjustment dates, unpaid interest treatment, and authorization policy.
* Checked the Microsoft source and its limits, example arithmetic, halved-input comparisons, links and anchors, Markdown tables, whitespace, and the scope of the diff. Independent document review found no actionable issues. No executable ledger or test suite exists yet.
* The user authorized a signed commit on local main and a push. Prepared the changes against the current main, preserving unrelated work. Architectural notes remain temporary, outside the repository and outside the commit.

### 07 September 2026, 20:24:51

* Standardized the [hold lifecycle research](../research/05-hold-lifecycle-research.md) to match the concise style of studies 01 through 04. Separated the approved settlement and expiration policies and removed repetition while preserving the decisions, source references, and limits.
* Checked the diff, whitespace, local links, reference labels, and example amounts. Independent document review found no actionable issues. The user authorized a signed commit and push of the working branch.

### 07 September 2026, 20:18:53

* Reviewed the [hold lifecycle research](../research/05-hold-lifecycle-research.md) and its [fictional example](../examples/07-hold-lifecycle.md). The user approved the documents, treating Auth-A as final and generating no automatic expiration during the six-day replay, and authorized the focused commit.
* Checked Mambu, Stripe, Mastercard, and Visa documentation. Recorded source limits, including differing Mambu descriptions of partial clearing and the distinction between reservation expiration and later financial settlement.
* Recorded the approved assumptions in the ambiguity decisions, numerical notes, and business rules. Documented the abandoned inference that missing later settlements prove finality. Kept API design notes temporary and excluded from Git staging.
* At the user's request, compared the changes with commit `b075af0`, the review of studies 01 through 04. Prepared a new isolated worktree from that commit, preserved the earlier worktree, and aligned research 05 with the approved policy of no automatic authorization reevaluation. Preserved the reviewed studies and their examples.
* Checked the diff, local links and anchors, Markdown formatting, example balances, and numerical comparisons. Independent document review found no actionable issues.

### 07 September 2026, 20:09:41

* The user canceled the local history rewrite and requested one new commit for this review and the approved authorization policy.
* Restored local main to its original signed history. Verified that all 13 pending files were preserved. No remote references were changed.

### 07 September 2026, 19:37:46

* The user approved no automatic authorization reevaluation after balance corrections. Preserve the original decision; later funds do not activate a declined request. Evaluate a new explicit request against the updated balance and active holds.
* Updated research 04, its example, the ambiguity decision, business rules, and numerical notes. Checked the diff, local links, formatting, and example calculations.

### 07 September 2026, 19:25:18

* Reviewed research 01 through 04 in order, with their examples, README, business rules, deliverables, and test instructions. Simplified wording and separated requirements, scenario data, approved choices, and open questions.
* Checked official source claims and example calculations. Clarified the required sum of rounded daily accruals and the distinction between balances before and after capitalization. Added direct technical and regulatory source links.
* Checked local links and anchors, Markdown tables, whitespace, the final diff, and preservation of the original checkout. Independent review found no actionable issues in the documentation diff before this entry. No executable ledger tests exist.

### 07 September 2026, 18:19:48

* Reviewed the [authorization research](../research/04-authorization-decisions-research.md) with the user, who approved the separation of ledger entries, holds, and decisions and authorized a new commit. Automatic reevaluation after corrections remains unresolved.
* Added the [fictional example](../examples/06-authorization-decisions.md) and aligned the decision record, numerical rationale, and business rules.
* Checked the source, calculations, local links, and formatting. Independent document review found no actionable issues.

### 07 September 2026, 18:02:12

* Reviewed and simplified the [settlement research](../research/03-unmatched-settlements-research.md) and its [example](../examples/05-unmatched-settlement.md). The user approved the documents and authorized the commit.
* Recorded the confirmed settlement assumption, updated BR06 and the numerical rationale, and documented the rejection of criterion 4.
* Checked calculations, links, formatting, and consistency across the related documents.

### 07 September 2026, 17:34:39

* Reviewed [Booking dates, value dates, and corrections](../research/02-booking-and-value-dates-research.md) with the user, who approved the document and its fictional example.
* Recorded the decisions, numerical rationale, business rules, and rejected historical adjustment dates.
* Checked calculations, formatting, and links.

### 07 September 2026, 16:13:57

* Reviewed [Monetary rounding](../research/01-rounding-research.md) with the user, who approved keeping its existing text.
* Updated references to the renamed report in AGENTS.md and this worklog.
* Checked the sources, five rounding examples, formatting, and local links.

### 07 September 2026, 14:46:53

Recorded the creation of [AGENTS.md](../../AGENTS.md) to preserve project working agreements across sessions.

* Captured English artifact conventions, separation of requirements and assumptions, source limits, documentation responsibilities, verification, and Git practices.
* Linked to existing project documents so detailed decisions remain in their appropriate files.
* Removed the instruction about the chat language at the user's request while retaining English for repository artifacts.

### 07 September 2026, 14:32:54

Recorded the completed review of the rounding research and the resulting documentation changes. The individual research and review start and completion times were not captured.

* Reviewed the [rounding research](../research/01-rounding-research.md) and checked its official sources. The securities examples provide limited precedents; Mambu describes precision and rounding stages without specifying HALF_UP or HALF_EVEN.
* Recorded HALF_UP as a project choice in [AMBIGUITIES.md](AMBIGUITIES.md), keeping AED at two decimal places and BHD at three. The explicit assumption is to favor recipients of positive interest at exact ties and accept the upward bias in those cases.
* Added the decision and a link to its rationale to the [business rules](../business-rules.md). Intermediate precision and rounding stages remain unresolved.
* Moved the report into docs/research and removed the temporary suffix. Its additional calculation proposals remain research recommendations, not adopted project decisions.

## Verified Commit Milestones

### 07 September 2026, 11:35:44

Signed commit `9a69223` organized the documentation and added the required deliverable scaffolds.

* Kept the [README](../../README.md) at the repository root, with TODOs for running the suite and interpreting its output.
* Grouped NUMBERS.md, AMBIGUITIES.md, REJECTED.md, and WORKLOG.md under docs/deliverables.
* Kept the exercise reference and working analysis separate from required deliverables.
* Added the [annotated failing test requirement](../../tests/README.md) as a placeholder, with implementation explicitly pending.
* Checked Markdown whitespace and the destinations of README links before committing.
* Verified the cryptographic signatures of both documentation commits.

### 07 September 2026, 11:35:01

Signed commit `6aa6245` added the [exercise statement](../exercise-statement.md) as a separate reference document.

The statement includes the event stream in its supplied order, the acceptance criteria, the required deliverables, and the evaluation guidance. Criteria that the exercise asks the candidate to challenge remain in the reference text. The document explicitly records that the detailed instructions for Part 2 have not been provided.

### 07 September 2026, 10:58:18

Signed commit `9b8eae0` added the [business rules](../business-rules.md).

The document records 12 business rules separately from initial account data and the test scenario. It leaves rounding, the currency of overdraft fees, the effects of backdated entries and reversals, and the lifecycle of authorization holds as open questions.

### 07 September 2026, 10:23:43

Commit `139563b` initialized the repository with a README. This milestone comes from the existing Git history.

## Earlier Session Activities

The following activities were recorded retrospectively at 11:48:18 on 07 September 2026. Their individual execution times were not captured.

* Reviewed the exercise and clarified the distinction between business rules, initial state, and test data.
* Discussed AED and BHD precision, ledger balance, available balance, and active authorization holds.
* Established English as the language for repository artifacts and Portuguese for this conversation. Refined the documentation wording and punctuation at the user's request.
* Performed preliminary research using official UAE and Bahrain sources to distinguish currency subdivisions, intermediate calculation precision, rounding modes, and the stage at which rounding occurs. Rules specific to taxes or other operations were not adopted as general ledger requirements.
* Prepared an English research prompt for a separate session to investigate the remaining rounding questions and produce a temporary report with conclusions and sources.
* Designated docs/rounding-research.tmp.md as the temporary research output and kept it outside the commits. The file is now present, but its conclusions have not yet been reviewed in this session.
* Agreed to work through the deliverables individually. Left unresolved decisions and implementation work as TODOs.

## Status at Initial Recording

* The repository contains documentation only. No implementation language or test framework has been selected, and no executable ledger or test suite exists yet.
* The annotated failing test remains a requirement to implement. The placeholder does not satisfy that requirement.
* NUMBERS.md, AMBIGUITIES.md, and REJECTED.md remain scaffolds awaiting substantive entries. No design constants, ambiguity resolutions, or abandoned implementation approaches have been invented to fill them.
* The detailed instructions for Part 2 remain unavailable.
* Rounding policy and the other documented business ambiguities remain unresolved, pending evidence review and explicit decisions.
* The three documentation commits listed above have verified signatures. No push has been performed in this session.
