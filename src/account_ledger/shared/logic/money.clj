(ns account-ledger.shared.logic.money
  (:import [java.math BigDecimal RoundingMode]))

(def scales {:AED 2 :BHD 3})
(def daily-rate 0.0004M)

(defn amount
  "Validate a decimal and normalize its currency scale without monetary rounding."
  [currency decimal]
  (when-not (contains? scales currency)
    (throw (ex-info "Unsupported currency" {:reason :unsupported-currency :currency currency})))
  (when-not (instance? BigDecimal decimal)
    (throw (ex-info "Money must be a BigDecimal" {:reason :invalid-amount})))
  (try (.setScale ^BigDecimal decimal (int (scales currency)) RoundingMode/UNNECESSARY)
       (catch ArithmeticException _
         (throw (ex-info "Amount exceeds currency precision" {:reason :excess-precision})))))

(defn daily-interest
  "Round the exact daily product once; nonpositive balances earn zero."
  [currency base]
  (let [base (amount currency base)
        product (.multiply ^BigDecimal (max base 0M) daily-rate)]
    (.setScale product (int (scales currency)) RoundingMode/HALF_UP)))

(defn allocate-three
  "Allocate three installments, with all remaining minor units in the last."
  [currency total]
  (let [total (amount currency total)]
    (when-not (pos? total)
      (throw (ex-info "Installment total must be positive" {:reason :invalid-amount})))
    (let [third (.divide ^BigDecimal total 3M (int (scales currency)) RoundingMode/DOWN)]
      (when (zero? third)
        (throw (ex-info "Three installments require at least three minor units"
                        {:reason :invalid-installments})))
      [third third (amount currency (- total third third))])))
