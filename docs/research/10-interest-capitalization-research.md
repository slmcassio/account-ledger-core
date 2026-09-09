# Interest capitalization

## Approved payment and components

The [exercise](../exercise-statement.md#nonnegotiable-rules) requires one interest credit per account at the end of Day 6. Keep AED and BHD separate. The [study 06 job](06-daily-closing-research.md#agreed-operation) references Day 5; Day 6 interest is calculated on Day 7, payable later.

Sum eligible unpaid daily accruals and positive or negative interest adjustments exactly, including corrections of paid periods. Never repay settled components. Record what the payment settles and preserve earlier payments. Use the daily amounts calculated under [study 09](09-daily-interest-research.md).

**Independent inputs:** unpaid eligible AED accruals 0.10 and 0.20, plus eligible correction -0.02 for a paid period. Pay `0.10 + 0.20 - 0.02 = 0.28`; exclude that period's earlier payment.

## Why pending interest earns nothing

Pending interest stays outside the ledger and available balances, so it earns nothing until paid, including hypothetical returns from earlier payment dates. This project choice keeps daily calculations easy to explain. The exercise supplies 0.04% per day; we are not choosing a different rate.

A real provider defines its offered rate and terms within applicable regulatory constraints. We are not certain this simplified choice suits every regulated product. [CBUAE Consumer Protection Standards](https://www.centralbank.ae/media/5crd24gm/cp-standards-pdf.pdf#page=26), 2.1.2.4 and 2.3.2.2(d) and (e), require disclosure of payment frequency and the compounding basis; they do not establish our method. Applicability and compliance have not been established, and currency alone does not determine jurisdiction. Payment frequency alone does not determine compounding.

## Approved capitalization date

Book and value the credit on its actual payment day, after the calculations producing it. Like an ordinary inflow, a Day 6 credit enters Day 6 fee and interest bases evaluated on Day 7. It cannot change the previous day's interest calculated on payment day. Payment order neither overrides input dates nor assigns [ordinary fee dates](07-overdraft-fees-research.md).

**Independent example:** Day 6 balance AED 12.49 before an eligible payment of 0.01, with complete inputs and no fees, holds or other movements. Day 7 calculates `12.50 × 0.0004 = 0.005 → 0.01`. Without the credit, `12.49 × 0.0004 = 0.004996 → 0.00`. Neither result changes the payment already made.

## Approved monthly payment

Pay on the first business day for the previous month. This period is separate from booking eligibility: current month accruals remain pending, while eligible unpaid adjustments can concern older periods. When day 1 is Monday and a business day, calculate the preceding month's last day first, then pay. The job's new ordinary accrual participates; corrections booked that day do not.

This is an explicit exercise choice. No business day calendar or mapping of synthetic Days 1 through 6 to months is supplied. Preserve the fixed Day 6 credit; its relationship to this schedule and later payment of Day 6 interest remains unresolved.

## Remaining questions

[Study 06](06-daily-closing-research.md#receipt-and-missing-inputs) covers receipt, missing eligible inputs and checkpoints. If correction follows payment, preserve that payment and keep interest differences pending until the next eligible monthly payment. Final replay amounts remain open.

**Negative total:** eligible AED accrual 0.10 and correction -0.30 give -0.20. Carrying this against future interest or debiting the account requires a policy. Recommend carrying it to preserve payment as a credit; neither option is approved.
