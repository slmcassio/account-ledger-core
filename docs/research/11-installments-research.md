# Installment allocation with an exact total

## Inputs and requirement

In the [exercise](../exercise-statement.md#event-stream), E10 credits ACC-002 with BHD 10.000 in three installments. [Study 06](06-daily-closing-research.md#receipt-and-missing-inputs) defines its late arrival and interest correction.

BHD requires three decimal places, so its smallest stored unit is BHD 0.001. The installments must preserve the original credit exactly. Three equal stored amounts are impossible because 10,000 units cannot be divided equally by three.

## Approved remainder position

**Decision:** Give the remaining BHD 0.001 to installment 3. The final installment then completes the original total. Giving it to installment 1 or 2 would also work; choosing a fixed position makes the result reproducible.

All three installments belong to ACC-002 and retain Day 5 for both booking and value dates; they introduce no payment schedule. If they are individual ledger credits, link them to E10 and do not credit the full parent amount again.

## Small example

For BHD 10.000 and three installments:

`10,000 = 3 * 3,333 + 1` units of BHD 0.001.

The allocation therefore gives **BHD 3.333, 3.333, and 3.334**, totaling **BHD 10.000**. The parts differ by only BHD 0.001, the closest possible equality at this precision.

[HALF_UP](01-rounding-research.md#analysis) does not choose the remainder's position. Rounding each exact third separately gives `3 * 3.333 = 9.999`, leaving BHD 0.001 unallocated.

**Criterion 7 is incorrect:** `3 * 3.334 = 10.002`, creating BHD 0.002 beyond the original credit. This contradiction is independent of which installment receives the remainder.

## Source and limit

[Mambu's repayment schedule documentation][mambu], section 2, "Round Remainder into Last Repayment," illustrates adding a missing cent to the last loan repayment. This is a product example of the chosen convention; it imposes no rule on E10. Conservation follows from the supplied credit.

[mambu]: https://docs.mambu.com/docs/rounding-repayment-schedule/
