# Overdraft fees in the account's currency

## Rule and approved exception

The [exercise](../exercise-statement.md#nonnegotiable-rules) requires AED 25.00 per account per day with a negative closing balance, without exempting ACC-002, denominated in BHD. It supplies no exchange rate, reference date or conversion policy.

**Approved decision:** Configure the daily overdraft fee by account type, in that account's own currency. For the types corresponding to the supplied accounts, use AED 25.00 for ACC-001 and BHD 0.000 for ACC-002. No additional account types are defined here.

The user chose the zero BHD fee for simplicity and because conversion requirements are missing. This is an explicit project exception to the literal mandatory fee rule; neither the exercise nor [HALF_UP](01-rounding-research.md#analysis) supplies this conclusion. BHD's three decimal places specify representation, not conversion.

## Supplied example

The [accounts](../exercise-statement.md#accounts) and [event stream](../exercise-statement.md#event-stream) provide these inputs:

* ACC-002 opens at BHD 0.000.
* E10 is its only movement: a total credit of BHD 10.000 in three installments.
* Process E10 after E9, with both `booking_date` and `value_date` on Day 5.

Principal here means opening balance plus supplied financial movements, excluding fees and interest. Before E10 is recorded, it is zero. Once its installments are recorded, principal is BHD 0.000 for Days 1 through 4 and `0.000 + 10.000 = 10.000` for Days 5 and 6.

Principal never becomes negative. These figures exclude interest and are not final balances; no capitalization calculation is needed for this conclusion.

## Negative example and boundaries

For a hypothetical assessment base of BHD -1.000, the configured fee is BHD 0.000. Considering only the fee's effect, `-1.000 - 0.000 = -1.000`. No conversion occurs. A zero fee neither floors the balance at zero nor prohibits negative balances.

The fee setting preserves the [authorization rule](04-authorization-decisions-research.md#authorization-rule), using the account's currency, and the [recording of legitimate confirmed debits](03-unmatched-settlements-research.md#the-approved-decision). It authorizes no unfunded hold.

Making an account incapable of becoming negative was considered and not selected. No foreign exchange, separate AED obligation or additional account is introduced.
