# Monetary rounding

## Scope

The project uses HALF_UP to round monetary amounts. AED uses two decimal places; BHD uses three.

## Requirements and decision

The [exercise](../exercise-statement.md) requires those currency precisions. It also requires the rounded daily interest accruals to sum exactly to the capitalized total. It does not specify how to round exact ties.

## Analysis

HALF_UP rounds to the nearest representable amount, with exact ties away from zero. HALF_EVEN selects the neighbor whose last retained digit is even at an exact tie. This can reduce bias across repeated ties.

The adopted choice assumes that positive interest recipients should receive the higher amount at exact ties. It accepts the resulting upward bias. This rationale does not establish a customer benefit for fees or negative adjustments.

[Study 09](09-daily-interest-research.md#rule-and-approved-calculation) now defines daily interest: exact multiplication followed by one currency rounding, with no fractions carried between days. This does not set intermediate precision or stages for other calculations. The rounding mode alone does not determine installment allocation or reversal compensation; the latter follows the [study 08 decision](../deliverables/AMBIGUITIES.md#reversal-compensation).

## Example

Inputs are exact decimal amounts before currency rounding:

* AED 0.0049 becomes AED 0.00 because it is below the midpoint.
* AED 0.005 becomes AED 0.01. AED -0.005 becomes AED -0.01.
* BHD 0.0005 becomes BHD 0.001. BHD -0.0005 becomes BHD -0.001.

The small amounts isolate the rounding boundaries. They are example inputs, not business constants.

## Sources and limits

* [Emirates NBD, 2025 AT1 prospectus][enbd], §5.1, printed p. 45, PDF p. 56: positive interest for periods shorter than a full interest period on these USD securities uses nearest-cent rounding with exact halves upwards.
* [Government of Bahrain, 29 April 2025 offering circular][bahrain], §5.1, printed p. 44, PDF p. 58: fixed-rate note interest uses nearest-subunit rounding with exact halves upwards, subject to another applicable market convention.

These are contractual precedents for securities. Neither establishes a mandatory rounding rule for these accounts. A currency code alone does not establish an applicable jurisdiction or account contract.

[Oracle's rounding definitions][rounding] explain HALF_UP and HALF_EVEN, including negative ties.

[enbd]: https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf
[bahrain]: https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf
[rounding]: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html#HALF_UP
