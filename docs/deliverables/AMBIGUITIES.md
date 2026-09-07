# Ambiguities

## Rounding Mode

**Decision: use HALF_UP, with two decimal places for AED and three for BHD.** Round to the nearest representable amount, with exact ties away from zero.

**Rationale and assumption:** favoring the recipient of positive interest at exact ties is the desired project policy. HALF_UP gives that recipient the higher amount. We accept the upward bias at those ties; HALF_EVEN can reduce that bias. This rationale does not imply a customer benefit for fees or negative adjustments.

The exercise specifies currency precision but leaves the rounding mode undefined. HALF_UP is a project choice, not a mandatory rule established for these accounts by the sources below. Currency alone does not establish the applicable jurisdiction or contract.

### Supporting Sources and Limits

* [Emirates NBD AT1 prospectus](https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf), section 5.1, printed page 45 (PDF page 56): interest for periods shorter than a full interest period on these USD securities uses the nearest cent, with positive exact ties rounded upwards. This is a precedent within that product's scope.
* [Government of Bahrain GMTN offering circular](https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf), section 5.1, printed page 44 (PDF page 58): interest on fixed rate notes uses the nearest currency subunit, with exact halves upwards, subject to another applicable market convention. This is a contractual securities rule.
* [Mambu deposit interest documentation](https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/): distinguishes calculation precision, storage precision, and rounding of aggregated journal entries. It does not specify HALF_UP or HALF_EVEN and does not justify the selected mode.

TODO: Resolve and document the remaining ambiguities, including intermediate precision and rounding stages.
