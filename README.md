# Account Ledger Core

Documentation for an account ledger that runs in memory.

## Running the Suite

No executable ledger or test suite exists yet.

TODO: Document prerequisites and the commands required to run the suite.

## Reading the Output

TODO: Explain the daily closing ledger balances, fee assessments, authorization states, and errors shown in the output.

## Documentation

### Exercise Reference

* [Exercise statement](docs/exercise-inputs/exercise-statement.md)

### Required Deliverables

* [Constants and numerical decisions](docs/deliverables/NUMBERS.md)
* [Ambiguities and decisions](docs/deliverables/AMBIGUITIES.md)
* [Rejected criteria and approaches](docs/deliverables/REJECTED.md)
* [Worklog](docs/deliverables/WORKLOG.md)
* [Annotated failing test requirement](tests/README.md)
* [Part 2: Architecture and Tradeoffs](docs/architecture.md)

### Working Notes

Research includes proposals and source review. [AMBIGUITIES](docs/deliverables/AMBIGUITIES.md) records adopted decisions and remaining questions.

* [Corrected business rules](docs/exercise-inputs/business-rules-corrected.md)
* [01: Monetary rounding](docs/research/01-rounding-research.md)
* [02: Booking dates, value dates, and corrections](docs/research/02-booking-and-value-dates-research.md)
* [03: Settlements without a matching authorization](docs/research/03-unmatched-settlements-research.md)
* [04: Authorization and ledger balances](docs/research/04-authorization-decisions-research.md)
* [05: Hold lifecycle](docs/research/05-hold-lifecycle-research.md)
* [06: Daily closing and calculation timing](docs/research/06-daily-closing-research.md)
* [07: Daily overdraft fee assessment](docs/research/07-overdraft-fees-research.md)
* [08: Principal reversals and financial corrections](docs/research/08-reversals-research.md)
* [09: Daily interest precision and rounding](docs/research/09-daily-interest-research.md)
* [10: Interest capitalization](docs/research/10-interest-capitalization-research.md)
* [11: Installment allocation](docs/research/11-installments-research.md)
* [12: Overdraft fee currency](docs/research/12-fee-currency-research.md)
* [13: Acceptance criteria assessment](docs/research/13-acceptance-criteria-research.md)
