# BANK-SPEC: launch prompt

Paste the text below into a new execution session opened on `/Users/slmcassio/Developer/account-ledger-core`. The user intends to select GPT-6 Astra with Ultra reasoning for that session. This file does not create or start a session by itself.

---

Execute BANK-SPEC to completion. This message authorizes the implementation, the specification's explicit defaults, use of built-in subagents and creation of an isolated worktree. I will be away for approximately six hours and want a complete, tested implementation ready to inspect when I return.

Read these two files completely before starting:

* `/Users/slmcassio/Documents/Codex/BANK-SPEC/SPEC.md`
* `/Users/slmcassio/Documents/Codex/BANK-SPEC/PLAN.md`

Repository: `/Users/slmcassio/Developer/account-ledger-core`.
User-selected codename: `BANK-SPEC`.
Intended branch: `codex/bank-spec`.
Intended worktree: `/Users/slmcassio/Developer/account-ledger-core-bank-spec`.
Prepared baseline: `ba4201b51901b355c896cc94981bea14139c925e`; verify the actual state and applicable instructions before changing anything.

Build one Clojure application in memory, with the documented Authorization, Ledger and Yield and Fees modules, small hexagonal boundaries, explicit APIs, unit tests, module integration tests and the real exercise end-to-end replay. Use the deterministic in-memory dispatcher in the specification. No Kafka or HTTP is required. Keep the implementation small enough for me to defend without AI assistance.

Follow the PLAN's ownership and dependencies. Obtain a working path through the actual modules early, then extend it. Do not postpone integration until each agent has finished its module. You own the correctness of the combined worktree, not just coordination of agent reports.

After launch, do not stop to ask routine questions or seek further design approval. I delegate the remaining implementation decisions to you: choose the smallest consistent solution and record its reason in root `agent-decisions.md`. Respect already adopted rules and the SPEC's explicit scenario boundaries. Each added scenario or abstraction must serve a documented requirement or its necessary verification. Do not invent complicated hypothetical systems or overengineer retries.

Create the implementation worktree only during this execution. Preserve my original checkout and unrelated work. Do not stage, commit, push, create a PR, merge, rebase, publish, change visibility or bypass signing, hooks or permissions. This task must not require my Touch ID signature.

The normal suite must pass with meaningful coverage and actual executed tests. The exercise's one annotated failing test must run separately and fail only for its explained design limitation. Do not hide failures, skip required scenarios, generate expected results from the production implementation or claim an unexecuted check passed.

Update README, WORKLOG, NUMBERS, AMBIGUITIES, REJECTED, architecture, API documentation and verification evidence as specified. Preserve the original exercise statement. Write code and repository documents in English; communicate with me in Portuguese.

Treat six hours as the intended budget. Keep milestone records that allow work to continue through context compaction. Prioritize missing required behavior and integration over optional polish. Continue until the definition of done has evidence; do not claim completion because time ran out. An actual external blocker must be reported accurately, with unaffected work completed where possible.

When finished, provide the worktree path, verified run commands, actual test results, deliberate challenge result, financial outcomes with their temporal boundaries, important decisions and any genuine remaining limitation. Do not stop at a plan, scaffolding, isolated modules or partial tests.
