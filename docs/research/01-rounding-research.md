# Monetary rounding

## Reference and scope

This document describes the adopted rounding mode used in the examples: HALF_UP, with two decimal places for AED and three for BHD. It covers how to round a monetary amount at a specified precision.

## Rule and open question

The [exercise](../exercise-statement.md) specifies currency precision but does not choose a tie rule. Storage precision, calculation precision, and the moment of rounding are separate questions. A currency code alone does not establish an applicable jurisdiction or account contract.

## Analysis

HALF_UP rounds to the nearest representable amount, with exact ties away from zero. HALF_EVEN instead selects the neighbor whose last retained digit is even at an exact tie, reducing systematic bias across repeated ties.

The adopted choice assumes that positive interest recipients should receive the higher amount at exact ties. It accepts the resulting upward bias. This rationale does not establish a customer benefit for fees or negative adjustments.

The mode does not decide whether to round each day's interest or an aggregate, how to allocate an installment remainder, or which historical amounts a reversal should correct. Those remain separate policy questions; this definition does not adopt an answer to them.

## Example

Inputs are exact decimal amounts before currency rounding:

* AED 0.0049 becomes AED 0.00 because it is below the midpoint.
* AED 0.005 becomes AED 0.01. AED -0.005 becomes AED -0.01.
* BHD 0.0005 becomes BHD 0.001. BHD -0.0005 becomes BHD -0.001.

These examples demonstrate the tie rule without assuming a fee, settlement, or interest calculation policy.

## Sources and limits

* [Emirates NBD, 2025 AT1 prospectus][enbd], §5.1, printed p. 45, PDF p. 56: positive interest for periods shorter than a full interest period on these USD securities uses nearest-cent rounding with exact halves upwards.
* [Government of Bahrain, 29 April 2025 offering circular][bahrain], §5.1, printed p. 44, PDF p. 58: fixed-rate note interest uses nearest-subunit rounding with exact halves upwards, subject to another applicable market convention.

These are limited contractual precedents from securities. Neither establishes a mandatory deposit-account rule or the evaluator's expected answer.

[enbd]: https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf
[bahrain]: https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf
