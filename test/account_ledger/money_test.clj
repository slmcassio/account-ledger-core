(ns account-ledger.money-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.money :as money]))

(deftest exact-inputs-and-scales
  (is (= 1.20M (money/amount :AED 1.2M)))
  (is (= 2 (.scale ^BigDecimal (money/amount :AED 1.200M))))
  (is (= 3 (.scale ^BigDecimal (money/amount :BHD 1M))))
  (doseq [[currency input] [[:AED 1.001M] [:BHD 0.0001M] [:AED 1.2]
                            [:AED "1.20"] [:USD 1M]]]
    (is (thrown? clojure.lang.ExceptionInfo (money/amount currency input)))))

(deftest daily-rounding-boundaries
  (doseq [[currency base expected] [[:AED 12.49M 0.00M] [:AED 12.50M 0.01M]
                                  [:BHD 1.250M 0.001M] [:BHD 1.249M 0.000M]
                                  [:AED 0M 0.00M] [:AED -1M 0.00M]
                                  [:AED 465M 0.19M]]]
    (is (= expected (money/daily-interest currency base))))
  (is (= 0.38M (+ (money/daily-interest :AED 465M)
                 (money/daily-interest :AED 465M)))))

(deftest three-installments-conserve-money
  (is (= [3.333M 3.333M 3.334M] (money/allocate-three :BHD 10.000M)))
  (is (= 10.000M (reduce + (money/allocate-three :BHD 10.000M))))
  (is (= [0.01M 0.01M 0.03M] (money/allocate-three :AED 0.05M)))
  (is (thrown? clojure.lang.ExceptionInfo (money/allocate-three :AED 0.02M)))
  (is (thrown? clojure.lang.ExceptionInfo (money/allocate-three :AED 0.00M))))
