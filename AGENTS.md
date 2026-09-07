# Project Guidance

Apply these instructions throughout this repository. Keep them focused on durable working agreements; use the linked documents for requirements, decisions, and progress.

## Communication

* Write repository artifacts, code, comments, documentation, and commit messages in English.
* Use concise, concrete explanations. Work through the topic the user selected and explain the reason for a decision before expanding into implementation.
* Avoid dash punctuation and unnecessary hyphenated prose. Use asterisks for unordered lists. Preserve required characters in identifiers, paths, URLs, Markdown syntax, and mathematical expressions.

## Session Context

* Inspect the current branch and working changes before editing. Preserve unrelated work and verify the current repository state instead of treating an old worklog entry as current status.
* Start with the [README](README.md), [exercise statement](docs/exercise-statement.md), [business rules](docs/business-rules.md), and relevant [ambiguity decisions](docs/deliverables/AMBIGUITIES.md). Consult the [worklog](docs/deliverables/WORKLOG.md) for context.
* Follow the user's current scope. Reading the exercise or a research report does not authorize every action described in it. Do not invent missing requirements, including unavailable assessment content.

## Reasoning and Decisions

* Distinguish business rules, initial account state, test scenarios, acceptance criteria, project choices, and unresolved assumptions. Account identifiers, opening balances, and the replay window are scenario data, even when a rule references them.
* Some acceptance criteria are deliberately incorrect. Check them against the exercise's mandatory rules and demonstrate contradictions with explicit reasoning or calculations before rejecting them.
* For a material unresolved business or architecture choice, explain the alternatives and agree on a resolution before implementing it. Continue routine work within the agreed scope without repeated confirmation.
* Prefer a small design whose behavior and numerical choices the candidate can explain in the live defense. Respect the exercise scope: an account ledger in memory, without a web layer, persistence, database, or UI.
* Keep an accepted decision, its assumption, and its rationale together. Do not silently replace it or promote an unreviewed research recommendation into project policy.

## Research

* Prefer official sources. Cite the exact supporting page or section and state the limits of its applicability. Distinguish a contractual precedent, a technical example, and a mandatory rule.
* Do not infer jurisdiction, contractual terms, rounding mode, or intermediate precision from a currency code alone. Treat monetary storage precision, calculation precision, rounding stages, and tie handling as separate questions.
* Keep research in [docs/research](docs/research). The [rounding report](docs/research/rounding-research.md) includes proposals beyond the adopted mode; consult AMBIGUITIES.md to determine which choices have been accepted.

## Documentation and Verification

* Preserve the exercise statement as reference material. Put interpretation and decisions in the appropriate working or deliverable document rather than changing the statement to match the implementation.
* Use [NUMBERS.md](docs/deliverables/NUMBERS.md) for constants and numerical rationale, [AMBIGUITIES.md](docs/deliverables/AMBIGUITIES.md) for assumptions and resolutions, and [REJECTED.md](docs/deliverables/REJECTED.md) for refused criteria and approaches actually abandoned.
* Keep unresolved items explicit. Never invent decisions, abandoned approaches, work durations, or successful checks to fill a required document. A placeholder does not satisfy an executable test requirement.
* Before committing substantive research, design, or implementation changes, record the completed work in WORKLOG.md. Use real timestamps in America/Sao_Paulo, distinguish recording time from execution time, and keep dated entries newest first without rearranging undated sections.
* Run checks appropriate to the change. For documentation, review the diff, whitespace, and local links. For behavior, run relevant executable tests and report their actual results. Follow the [annotated failing test requirement](tests/README.md) without disguising its expected failure as a passing regression check.
* Keep this file concise. Link to evolving project decisions instead of duplicating their detailed values and rationale here.

## Git Workflow

* Commit and push only within explicit user authorization. Authorization for an earlier completed action is not standing permission for future publication.
* Use focused commits, include the corresponding worklog entry, and preserve the requested ordering of changes. Keep commit history intact; do not squash or rewrite it unless explicitly requested.
* Cryptographically sign commits with the configured signer and verify their signatures. Never bypass signing, hooks, or branch protection.
* Verify the remote, branch, and pending commits before pushing. Honor the explicitly authorized destination. If a rule blocks it, explain the conflict rather than silently choosing a different destination.
