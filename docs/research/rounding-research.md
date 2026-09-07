# Monetary rounding

**Recommendation: use HALF_UP for this exercise, with two decimal places for AED and three for BHD.** Round to the nearest representable amount, with exact ties away from zero.

**Why:** for positive interest exactly halfway between two amounts, HALF_UP consistently gives the account holder the higher amount. **Assumption:** favoring the interest recipient at those ties is the desired policy. **Tradeoff:** repeated positive ties create an upward bias; HALF_EVEN can reduce that bias. This rationale does not imply that HALF_UP favors customers for fees or negative adjustments.

The sources below provide enough support for a documented project choice. They do not establish a mandatory deposit-account rule or the evaluator's expected answer. Using AED/BHD does not establish the applicable jurisdiction or contract.

## Supporting evidence

* **[Emirates NBD, AT1 prospectus][enbd], 2025, §5.1, printed p. 45 (PDF p. 56):** for interest covering less than a full interest period, these USD securities use the nearest cent, with half a cent rounded upwards. This is an explicit precedent for HALF_UP on positive interest, within that product's scope.
* **[Government of Bahrain, GMTN offering circular][bahrain], 29 April 2025, §5.1, printed p. 44 (PDF p. 58):** fixed-rate note interest is rounded to the nearest currency sub-unit, with exact halves upwards, unless another applicable market convention governs. This is a contractual securities rule.
* **Mambu:** its [deposit-interest documentation][mambu-interest] separates calculation at 20 decimals, storage truncated to 10 decimals, and currency rounding of aggregated journal entries. **It does not specify HALF_UP or HALF_EVEN.** Its [repayment-schedule documentation][mambu-installments] offers an option to put the remainder in the last loan installment, a technical precedent for preserving a split total.

## Exercise requirements and proposed choices

The [exercise][exercise] requires 0.04% daily interest on positive closing ledger balances, AED/BHD precision, a single capitalization credit at the end of Day 6, exact reconciliation with rounded daily accruals, and append-only history.

The recommendation additionally assumes:

1. **Daily calculation:** multiply the positive closing balance by exactly 0.0004, then round once with HALF_UP. The rate is already daily. Preserve the exact intermediate, requiring at most six fractional decimal places for AED or seven for BHD balances at their currency precision, plus sufficient integer capacity.
2. **Capitalization:** sum the six independently rounded daily accruals, with no fractional carry between days. Calculate Day 6 interest before posting the credit. Never discard a reconciliation mismatch.
3. **Installments:** allocate BHD 10.000 as **3.333, 3.333, 3.334**, assigning the extra fil to the last installment. Preserving the total is necessary; placing the remainder last is a choice.
4. **Signed amounts and reversals:** HALF_UP rounds negative exact ties away from zero. Reverse a posting by appending its exact negation, including any allocated remainder. Backdated interest and fee recalculation require separate policies.

[exercise]: /Users/slmcassio/Developer/account-ledger-core/docs/exercise-statement.md
[enbd]: https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf
[bahrain]: https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf
[mambu-interest]: https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/
[mambu-installments]: https://docs.mambu.com/docs/rounding-repayment-schedule/
