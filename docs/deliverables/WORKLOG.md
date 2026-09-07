# Worklog

## Recording Method

The initial retrospective log was recorded on 07 September 2026 at 11:48:18 in America/Sao_Paulo, whose UTC offset is minus three hours on this date.

Commit milestones use the author and committer timestamps verified in Git history. These timestamps mark recorded repository changes. Earlier discussions are documented separately because their individual start and completion times were not captured. No work durations or missing timestamps have been reconstructed.

Later dated activity entries identify when the summary was recorded, not when the underlying work started or ended. They use the same time zone.

## Research and Documentation Updates

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
