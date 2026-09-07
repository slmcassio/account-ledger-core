# Worklog

## Recording Method

This retrospective log was recorded on 07 September 2026 at 11:48:18 in America/Sao_Paulo, whose UTC offset is minus three hours on this date.

Commit milestones use the author and committer timestamps verified in Git history. These timestamps mark recorded repository changes. Earlier discussions are documented separately because their individual start and completion times were not captured. No work durations or missing timestamps have been reconstructed.

## Verified Commit Milestones

### 07 September 2026, 10:23:43

Commit `139563b` initialized the repository with a README. This milestone comes from the existing Git history.

### 07 September 2026, 10:58:18

Signed commit `9b8eae0` added the [business rules](../business-rules.md).

The document records 12 business rules separately from initial account data and the test scenario. It leaves rounding, the currency of overdraft fees, the effects of backdated entries and reversals, and the lifecycle of authorization holds as open questions.

### 07 September 2026, 11:35:01

Signed commit `6aa6245` added the [exercise statement](../exercise-statement.md) as a separate reference document.

The statement includes the event stream in its supplied order, the acceptance criteria, the required deliverables, and the evaluation guidance. Criteria that the exercise asks the candidate to challenge remain in the reference text. The document explicitly records that the detailed instructions for Part 2 have not been provided.

### 07 September 2026, 11:35:44

Signed commit `9a69223` organized the documentation and added the required deliverable scaffolds.

* Kept the [README](../../README.md) at the repository root, with TODOs for running the suite and interpreting its output.
* Grouped NUMBERS.md, AMBIGUITIES.md, REJECTED.md, and WORKLOG.md under docs/deliverables.
* Kept the exercise reference and working analysis separate from required deliverables.
* Added the [annotated failing test requirement](../../tests/README.md) as a placeholder, with implementation explicitly pending.
* Checked Markdown whitespace and the destinations of README links before committing.
* Verified the cryptographic signatures of both documentation commits.

## Earlier Session Activities

The following activities were recorded retrospectively at 11:48:18 on 07 September 2026. Their individual execution times were not captured.

* Reviewed the exercise and clarified the distinction between business rules, initial state, and test data.
* Discussed AED and BHD precision, ledger balance, available balance, and active authorization holds.
* Established English as the language for repository artifacts and Portuguese for this conversation. Refined the documentation wording and punctuation at the user's request.
* Performed preliminary research using official UAE and Bahrain sources to distinguish currency subdivisions, intermediate calculation precision, rounding modes, and the stage at which rounding occurs. Rules specific to taxes or other operations were not adopted as general ledger requirements.
* Prepared an English research prompt for a separate session to investigate the remaining rounding questions and produce a temporary report with conclusions and sources.
* Designated docs/rounding-research.tmp.md as the temporary research output and kept it outside the commits. The file is now present, but its conclusions have not yet been reviewed in this session.
* Agreed to work through the deliverables individually. Left unresolved decisions and implementation work as TODOs.

## Status at Recording

* The repository contains documentation only. No implementation language or test framework has been selected, and no executable ledger or test suite exists yet.
* The annotated failing test remains a requirement to implement. The placeholder does not satisfy that requirement.
* NUMBERS.md, AMBIGUITIES.md, and REJECTED.md remain scaffolds awaiting substantive entries. No design constants, ambiguity resolutions, or abandoned implementation approaches have been invented to fill them.
* The detailed instructions for Part 2 remain unavailable.
* Rounding policy and the other documented business ambiguities remain unresolved, pending evidence review and explicit decisions.
* The three documentation commits listed above have verified signatures. No push has been performed in this session.
