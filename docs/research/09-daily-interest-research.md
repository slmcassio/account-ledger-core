# Daily interest calculation

**Source:** Research from `main` at `ec4e20cc9eb9f788e5195be574b384ec97108f32`.

**Integration status:** The daily calculation below is adopted under [Daily Interest Calculation](../deliverables/AMBIGUITIES.md#daily-interest-calculation): exact multiplication followed by one HALF_UP currency rounding, with no fractions carried between days. This decision does not set intermediate precision or rounding stages for other calculations.

## Rule and approved calculation

For each account and day, multiply its positive closing ledger balance by `0.0004`, then round once using [HALF_UP and the currency's precision](01-rounding-research.md#scope). Zero and negative balances earn nothing. Keep the product exact until daily rounding, with no intermediate rounding or fractions carried between days.

The [exercise](../exercise-inputs/exercise-statement.md#nonnegotiable-rules) supplies the daily rate and currency precisions. Exact products need at most six fractional places for AED or seven for BHD, distinct from stored monetary precision, with no limit implied on integer digits. No language is selected.

## Why the stages matter

These are independent example bases, not exercise balances.

* AED `12.49 × 0.0004 = 0.004996 → 0.00`. Rounding first to three places gives `0.005 → 0.01` instead.
* BHD `1.249 × 0.0004 = 0.0004996 → 0.000`. Rounding first to four places gives `0.0005 → 0.001` instead.

Two days at AED 465.00 each give `0.186000 → 0.19` daily. Pay their sum, `0.38`, if both are eligible and unpaid. Rounding the raw sum gives `0.372000 → 0.37`, which breaks the required equality with the daily amounts.

## Corrections and payment

Apply [study 02's incremental comparison](02-booking-and-value-dates-research.md#approved-adjustment-method) to monetary amounts rounded for that day.

**Example:** assume a day originally accrued AED 1.00 and received an earlier adjustment of +0.20. Its recorded total is 1.20. If its corrected daily interest is now 1.30, append only `1.30 - 1.20 = +0.10`. Paying the earlier 0.20 does not remove it from this comparison. Repeating gives `1.30 - (1.00 + 0.20 + 0.10) = 0.00`.

Compare rounded targets, not raw differences: changing the base from 12.49 to 12.50 changes daily interest from 0.00 to 0.01, although the raw difference `0.000004` rounds to zero.

Interest adjustments retain the correction dates defined in study 02 and [remain pending until eligible payment](10-interest-capitalization-research.md#approved-payment-and-components), even for paid periods.

## Remaining dependencies

Apply the approved [booking cutoff](06-daily-closing-research.md#agreed-operation) and [reversal fee refund dates](08-reversals-research.md#correcting-fees-and-interest-after-a-reversal). Final bases and totals depend on [the open calculation decisions](06-daily-closing-research.md#decisions-still-open).

## Technical reference

[Oracle's arithmetic contract and preferred multiplication scales](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html) explain exact products. This is technical support, not a Java choice or mandatory banking policy.
