# Ambiguities

## Rounding Mode

**Decision: use HALF_UP, with two decimal places for AED and three for BHD.** Round to the nearest representable amount, with exact ties away from zero.

**Rationale and assumption:** favoring the recipient of positive interest at exact ties is the desired project policy. HALF_UP gives that recipient the higher amount. We accept the upward bias at those ties; HALF_EVEN can reduce that bias. This rationale does not imply a customer benefit for fees or negative adjustments.

The exercise specifies currency precision but leaves the rounding mode undefined. HALF_UP is a project choice, not a mandatory rule established for these accounts by the sources below. Currency alone does not establish the applicable jurisdiction or contract.

### Supporting Sources and Limits

* [Emirates NBD AT1 prospectus](https://www.emiratesnbd.com/-/media/enbd/files/investor-relations/public-issuances/list/perpetual_nc6_at1_prospectus.pdf), section 5.1, printed page 45 (PDF page 56): interest for periods shorter than a full interest period on these USD securities uses the nearest cent, with positive exact ties rounded upwards. This is a precedent within that product's scope.
* [Government of Bahrain GMTN offering circular](https://www.rns-pdf.londonstockexchange.com/rns/7262H_2-2025-5-7.pdf), section 5.1, printed page 44 (PDF page 58): interest on fixed rate notes uses the nearest currency subunit, with exact halves upwards, subject to another applicable market convention. This is a contractual securities rule.
* [Mambu deposit interest documentation](https://docs.mambu.com/docs/truncating-and-rounding-interest-deposits/): distinguishes calculation precision, storage precision, and rounding of aggregated journal entries. It does not specify HALF_UP or HALF_EVEN and does not justify the selected mode.

## Booking and Value Dates

**Decision:** `booking_date` is the accounting recording day, supplied as "Booked". `value_date` is the day from which the transaction affects the balance. Neither necessarily identifies when the event occurred or arrived. Preserve the supplied event order and dates.

**Rationale:** separate when a transaction is recorded from when it has financial effect. A backdated transaction can change a historical balance without changing an existing record. Queries limited to earlier records reproduce the earlier view.

## Late Transaction Adjustments

**Scope:** legitimate transactions delivered after they occurred, such as an official transaction received from Mastercard later. Events resulting from system errors and corrections of those errors are outside this discussion's scope.

**Decision:** append the original transaction with its supplied dates. Recalculate affected fees and interest, then append a separate adjustment linked to that transaction. For each component, record `corrected amount - net amount already recorded`; do not repeat the original transaction amount.

The adjustment uses the correction day for both `booking_date` and `value_date`. Keep the historical days and each fee or interest component in its calculation breakdown. Interest corrections before capitalization change unpaid interest; corrections to interest already credited affect the account balance.

**Assumption and rationale:** interpret the fee's "day assessed" as the day the correction is assessed and recorded. This makes the adjustment affect the current balance while preserving the original records and the explanation of each historical difference. In the approved example, J has both dates on Day 5; Days 2, 3, and 4 are calculation references.

**Basis and limits:** [the research](../research/02-booking-and-value-dates-research.md) records ISO date definitions, Mambu's backdating and reversal examples, and their limits. Difference adjustments and their dates are project choices. The cited CBUAE provisions address error correction and do not establish this policy.

The [fictional example](../examples/04-backdated-adjustment.md) illustrates the approved decision.
