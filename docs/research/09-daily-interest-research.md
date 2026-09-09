# Daily interest calculation

## Rule and approved calculation

For each account and day, multiply its positive closing ledger balance by `0.0004`, then round once with HALF_UP to two decimal places for AED or three for BHD. Zero and negative balances earn nothing. Keep the product exact until daily rounding, with no intermediate rounding or fractions carried between days. Each daily amount is reproducible from its own base.

The [exercise](../exercise-statement.md#nonnegotiable-rules) supplies the daily rate and currency precisions. The [approved calculation](../deliverables/AMBIGUITIES.md#daily-interest-calculation) separates monetary precision from calculation precision: exact products need at most six fractional places for AED or seven for BHD, with no limit implied on integer digits. No language is selected.

## Why the stages matter

These are independent example bases, not exercise balances.

* AED `12.49 × 0.0004 = 0.004996 → 0.00`. Rounding first to three places gives `0.005 → 0.01` instead.
* BHD `1.249 × 0.0004 = 0.0004996 → 0.000`. Rounding first to four places gives `0.0005 → 0.001` instead.

Two days at AED 465.00 each give `0.186000 → 0.19` daily. Pay their sum, `0.38`, if both are eligible and unpaid. Rounding the raw sum gives `0.372000 → 0.37`, which breaks the required equality with the daily amounts.

## Corrections and payment

`new adjustment = corrected daily interest - interest already recorded for that day`

“Already recorded” means the original accrual plus every earlier adjustment, including paid ones. Both sides are monetary amounts rounded for that day.

**Example:** assume a day originally accrued AED 1.00 and received an earlier adjustment of +0.20. Its recorded total is 1.20. If its corrected daily interest is now 1.30, append only `1.30 - 1.20 = +0.10`. Paying the earlier 0.20 does not remove it from this comparison. Repeating gives `1.30 - (1.00 + 0.20 + 0.10) = 0.00`.

Compare rounded targets, not raw differences: changing the base from 12.49 to 12.50 changes daily interest from 0.00 to 0.01, although the raw difference `0.000004` rounds to zero.

Every interest adjustment uses the actual correction day for both dates and [stays pending](../deliverables/AMBIGUITIES.md#interest-adjustments-wait-for-payment), even for paid periods. Pending interest changes neither ledger nor available balance and earns nothing. Preserve earlier payments; settle each eligible unpaid component once.

## Remaining dependencies

The [D−1 booking cutoff](06-daily-closing-research.md#agreed-operation) and [reversal fee refund dates](08-reversals-research.md#approved-decisions-and-remaining-limits) remain approved. Study 06's checkpoints, ordinary assessment dates, missing inputs and final E7 fee count remain open, so actual bases and totals are unresolved. [Study 10](10-interest-capitalization-research.md) now defines payment dates and a monthly schedule, with calendar mapping and negative totals unresolved. Older examples establish no current replay results.

## Technical reference

[Oracle's arithmetic contract and preferred multiplication scales](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html) explain exact products. This is technical support, not a Java choice or mandatory banking policy.
