# Fifteen-day reversal simulation

Detailed calculations for [study 08](../08-reversals-research.md).

This illustration is separate from the supplied E7/E9 replay. Method B is approved for reversal fee refunds; A remains a comparison of the alternative not adopted. The schedule below is illustrative.

## Inputs and assumed schedule

* Open with AED 2,500.00 on Day 1, no holds or pending interest. Debit 3,000.00 with booking/value Day 5. Reverse 3,000.00 with value Day 5 and booking Day 9 or Day 12. No other external movements occur.
* Charge 25.00 per negative assessment base; accrue 0.04% daily on positive balances. Use exact arithmetic and daily HALF_UP rounding: `2,500.00 * 0.0004 = 1.00`. Each 25.00 fee reduces that daily interest by 0.01.
* Run the ordinary job first in D with reference and cumulative booking cutoff D-1. Assess, book, and value the fee for H on H+1. A separate checkpoint immediately after reversal includes that new record and recalculates previously assessed periods. These checkpoints and assessment dates are assumptions for this example.
* Pay once at the end of Day 10, referencing Day 9 and including its resulting accrual. Day 15 is only a consultation after the routine; pending then covers reference days through Day 14.

Both methods recalculate fees and interest. **A** books and values fee refunds on the correction day. **B** books them that day but values each refund on its original fee's date. Both book and value interest corrections on the correction day and keep them pending until eligible payment.

### Reversal on Day 9

Four fees, charged on Days 6 through 9 for balance days 5 through 8, require a `4 * 25.00 = 100.00` refund. The Day 9 job charges the fourth fee before reversal.

All timeline balances are actual balances after the listed event. Pending rows change no funds.

| Booking day | Event | Amount | Balance A | Balance B |
|---|---|---|---|---|
| 1 | Opening | +2,500.00 | 2,500.00 | 2,500.00 |
| 2 to 4 | No cash movement | 0.00 | 2,500.00 | 2,500.00 |
| 5 | Debit | -3,000.00 | -500.00 | -500.00 |
| 6 | Fee | -25.00 | -525.00 | -525.00 |
| 7 | Fee | -25.00 | -550.00 | -550.00 |
| 8 | Fee | -25.00 | -575.00 | -575.00 |
| 9 | Fee | -25.00 | -600.00 | -600.00 |
| 9 | Reversal | +3,000.00 | 2,400.00 | 2,400.00 |
| 9 | Fee refund | +100.00 | 2,500.00 | 2,500.00 |
| 9 | Interest correction, pending | A +3.94; B +4.00 | 2,500.00 | 2,500.00 |
| 10 | One interest payment | A +8.94; B +9.00 | 2,508.94 | 2,509.00 |
| 11 to 15 | No cash movement | 0.00 | 2,508.94 | 2,509.00 |

**Why the interest differs.** The table below reconstructs each balance day after correction, using actual value dates. These are historical calculation results, not new entries on those days. Before reversal, each listed day earned zero interest and incurred a 25.00 fee; afterward each fee target is zero. Day 5's fee is charged on Day 6, which explains the different dates.

| Balance day | Fee charged on | Observed balance | Corrected A | Corrected B | Interest A | Interest B |
|---|---|---|---|---|---|---|
| 5 | 6 | -500.00 | 2,500.00 | 2,500.00 | 1.00 | 1.00 |
| 6 | 7 | -525.00 | 2,475.00 | 2,500.00 | 0.99 | 1.00 |
| 7 | 8 | -550.00 | 2,450.00 | 2,500.00 | 0.98 | 1.00 |
| 8 | 9 | -575.00 | 2,425.00 | 2,500.00 | 0.97 | 1.00 |

A retains earlier fees until the Day 9 refund: `1.00 + 0.99 + 0.98 + 0.97 = 3.94`. B offsets each fee at its original value date: `4 * 1.00 = 4.00`.

Ordinary accruals recorded on Days 2 through 5 and Day 10 total 5.00. The single Day 10 payment settles ordinary and correction components once: **A `5.00 + 3.94 = 8.94`; B `5.00 + 4.00 = 9.00`**. Days 11 through 15 record another 5.00, still pending.

### Reversal on Day 12

Seven fees, charged on Days 6 through 12 for balance days 5 through 11, require a `7 * 25.00 = 175.00` refund. The correction comes after the only payment.

| Booking day | Event | Amount | Balance A | Balance B |
|---|---|---|---|---|
| 1 | Opening | +2,500.00 | 2,500.00 | 2,500.00 |
| 2 to 4 | No cash movement | 0.00 | 2,500.00 | 2,500.00 |
| 5 | Debit | -3,000.00 | -500.00 | -500.00 |
| 6 | Fee | -25.00 | -525.00 | -525.00 |
| 7 | Fee | -25.00 | -550.00 | -550.00 |
| 8 | Fee | -25.00 | -575.00 | -575.00 |
| 9 | Fee | -25.00 | -600.00 | -600.00 |
| 10 | Fee | -25.00 | -625.00 | -625.00 |
| 10 | One interest payment | +4.00 | -621.00 | -621.00 |
| 11 | Fee | -25.00 | -646.00 | -646.00 |
| 12 | Fee | -25.00 | -671.00 | -671.00 |
| 12 | Reversal | +3,000.00 | 2,329.00 | 2,329.00 |
| 12 | Fee refund | +175.00 | 2,504.00 | 2,504.00 |
| 12 | Interest correction, pending | A +6.79; B +7.00 | 2,504.00 | 2,504.00 |
| 13 to 15 | No cash movement | 0.00 | 2,504.00 | 2,504.00 |

The Day 10 payment contains only four ordinary accruals of 1.00, recorded on Days 2 through 5. It remains in historical balances from its actual value date, Day 10. The later correction cannot change that payment.

As above, each listed day's original interest is zero, its original fee is 25.00, and its corrected fee target is zero.

| Balance day | Fee charged on | Observed balance | Corrected A | Corrected B | Interest A | Interest B |
|---|---|---|---|---|---|---|
| 5 | 6 | -500.00 | 2,500.00 | 2,500.00 | 1.00 | 1.00 |
| 6 | 7 | -525.00 | 2,475.00 | 2,500.00 | 0.99 | 1.00 |
| 7 | 8 | -550.00 | 2,450.00 | 2,500.00 | 0.98 | 1.00 |
| 8 | 9 | -575.00 | 2,425.00 | 2,500.00 | 0.97 | 1.00 |
| 9 | 10 | -600.00 | 2,400.00 | 2,500.00 | 0.96 | 1.00 |
| 10 | 11 | -621.00 | 2,379.00 | 2,504.00 | 0.95 | 1.00 |
| 11 | 12 | -646.00 | 2,354.00 | 2,504.00 | 0.94 | 1.00 |

A's correction is `1.00 + 0.99 + 0.98 + 0.97 + 0.96 + 0.95 + 0.94 = 6.79`; B's is 7.00. Both remain pending. Ordinary accruals recorded on Days 13 through 15 add 3.00: **A 9.79 pending; B 10.00 pending**. Previously paid components are not paid again.

### Results at the two checkpoints

See the [result comparison in study 08](../08-reversals-research.md#before-or-after-payment). Balances are actual end-of-day funds. Day 10 pending is zero after payment; Day 15 pays nothing.
