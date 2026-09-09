# Overdraft fees in the account's currency

**Source:** Research from `main` at `ec4e20cc9eb9f788e5195be574b384ec97108f32`.

**Integration status:** The [account type fee configuration and zero BHD fee](../deliverables/AMBIGUITIES.md#overdraft-fee-currency) and [E10 receipt scenario](../deliverables/AMBIGUITIES.md#e10-installment-allocation) are incorporated into the deliverables. The zero fee remains an explicit project exception to the literal fee rule.

## Rule and approved exception

The [exercise](../exercise-inputs/exercise-statement.md#nonnegotiable-rules) requires AED 25.00 per account per day with a negative closing balance, without exempting ACC-002, denominated in BHD. It supplies no exchange rate, reference date or conversion policy.

**Approved decision:** Configure the daily overdraft fee by account type, in that account's own currency. For the types corresponding to the supplied accounts, use AED 25.00 for ACC-001 and BHD 0.000 for ACC-002. No additional account types are defined here.

The user chose the zero BHD fee for simplicity and because conversion requirements are missing. This is an explicit project exception to the literal mandatory fee rule; neither the exercise nor [HALF_UP](01-rounding-research.md#analysis) supplies this conclusion. BHD's three decimal places specify representation, not conversion.

## Supplied example

[ACC-002 opens at BHD 0.000](../exercise-inputs/exercise-statement.md#accounts). Its only supplied movement is [E10's BHD 10.000 credit](../exercise-inputs/exercise-statement.md#event-stream), with both `booking_date` and `value_date` on Day 5. It [arrives late on Day 6](06-daily-closing-research.md#receipt-and-missing-inputs).

Principal here means opening balance plus supplied financial movements, excluding fees and interest. Before E10 is recorded, it is zero. Once its installments are recorded, principal is BHD 0.000 for Days 1 through 4 and `0.000 + 10.000 = 10.000` for Days 5 and 6.

Principal never becomes negative in this scenario; these are not final account balances.

## Negative example and boundaries

For a hypothetical assessment base of BHD -1.000, the configured fee is BHD 0.000. Considering only the fee's effect, `-1.000 - 0.000 = -1.000`. No conversion occurs. A zero fee neither floors the balance at zero nor prohibits negative balances.

The fee setting preserves the [authorization rule](04-authorization-decisions-research.md#authorization-rule), using the account's currency, and the [recording of legitimate confirmed debits](03-unmatched-settlements-research.md#the-approved-decision). It authorizes no unfunded hold.

Making an account incapable of becoming negative was considered and not selected. No foreign exchange, separate AED obligation or additional account is introduced.
