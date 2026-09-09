(ns account-ledger.unit.ledger-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.ledger.domain :as domain]))

(def credit-event
  {:transaction/id "E1" :transaction/type :credit :account/id "ACC-001"
   :money/currency :AED :money/amount 1200.00M :financial/effect 1200.00M
   :booking-day 1 :value-day 1 :received-day 1 :event-counter 1
   :purpose :principal :occurrences []})

(deftest credits-and-debits-use-balanced-liability-postings
  (let [entry (domain/journal-entry fixtures/config credit-event 1)]
    (is (= 1 (:journal/position entry)))
    (is (= "E1" (:transaction/id entry)))
    (is (= [{:book/account "clearing/AED" :side :debit :money/amount 1200.00M :money/currency :AED}
            {:book/account "customer/ACC-001" :side :credit :money/amount 1200.00M :money/currency :AED}]
           (:postings entry))))
  (let [debit (assoc credit-event :transaction/id "E2" :transaction/type :debit
                    :money/amount 950.00M :financial/effect -950.00M :event-counter 2)
        entry (domain/journal-entry fixtures/config debit 2)]
    (is (= [{:book/account "clearing/AED" :side :credit :money/amount 950.00M :money/currency :AED}
            {:book/account "customer/ACC-001" :side :debit :money/amount 950.00M :money/currency :AED}]
           (:postings entry)))))

(deftest installments-conserve-the-parent-in-three-balanced-pairs
  (let [movement (assoc credit-event :transaction/id "E10" :account/id "ACC-002"
                        :money/currency :BHD :money/amount 10.000M :financial/effect 10.000M
                        :booking-day 5 :value-day 5 :received-day 6 :installment-count 3
                        :installments [3.333M 3.333M 3.334M])
        entry (domain/journal-entry fixtures/config movement 7)
        customer (filterv #(= "customer/ACC-002" (:book/account %)) (:postings entry))]
    (is (= 7 (:journal/position entry)))
    (is (= 6 (count (:postings entry))))
    (is (= [3.333M 3.333M 3.334M] (mapv :money/amount customer)))
    (is (= [1 2 3] (mapv :installment/position customer)))
    (is (= 10.000M (reduce + 0.000M (map :money/amount customer))))
    (doseq [position [1 2 3]]
      (let [pair (filter #(= position (:installment/position %)) (:postings entry))]
        (is (= #{:debit :credit} (set (map :side pair))))
        (is (= 1 (count (set (map :money/amount pair)))))
        (is (= #{:BHD} (set (map :money/currency pair))))))))

(deftest new-movements-must-have-valid-committed-financial-content
  (doseq [movement [(dissoc credit-event :transaction/id)
                    (assoc credit-event :transaction/id "")
                    (dissoc credit-event :event-counter)
                    (assoc credit-event :event-counter 0)
                    (assoc credit-event :account/id "unknown")
                    (assoc credit-event :money/currency :BHD)
                    (assoc credit-event :money/currency :USD)
                    (assoc credit-event :money/amount 1200.001M)
                    (assoc credit-event :money/amount 1200.0)
                    (assoc credit-event :financial/effect 1200.001M)
                    (assoc credit-event :financial/effect -1200.00M)
                    (assoc credit-event :financial/effect 0.00M)
                    (assoc credit-event :money/amount -1200.00M)
                    (dissoc credit-event :money/amount)
                    (assoc credit-event :booking-day 2 :received-day 1)
                    (assoc credit-event :value-day 0)
                    (assoc credit-event :transaction/type :release)
                    (assoc credit-event :transaction/type :authorization)
                    (assoc credit-event :transaction/type :declined)]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (domain/journal-entry fixtures/config movement 1))
        (pr-str movement)))
  (doseq [installments [[3.333M 3.333M 3.333M]
                        [3.334M 3.334M 3.334M]
                        [0.000M 5.000M 5.000M]
                        [-1.000M 5.000M 6.000M]
                        [5.000M 5.000M]
                        [3.3333M 3.3333M 3.3334M]]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (domain/journal-entry fixtures/config
                                       (assoc credit-event :account/id "ACC-002"
                                              :money/currency :BHD :money/amount 10.000M
                                              :financial/effect 10.000M :installment-count 3
                                              :installments installments) 1))))
  (is (thrown? clojure.lang.ExceptionInfo
               (domain/journal-entry fixtures/config
                                     (assoc credit-event :installment-count 3) 1)))
  (is (thrown? clojure.lang.ExceptionInfo
               (domain/journal-entry fixtures/config credit-event 0))))

(deftest pure-recording-preserves-old-history-and-appends-whole-movements
  (let [before (domain/initial-state fixtures/config)
        [after result] (domain/record-movement before credit-event)
        [duplicate duplicate-result] (domain/record-movement after {:transaction/id "E1"})
        [invalid invalid-result] (domain/record-movement after
                                                         (assoc credit-event :transaction/id "bad"
                                                                             :financial/effect 0.00M))]
    (is (= :recorded (:outcome result)))
    (is (= [] (:entries before)))
    (is (= 1 (count (:entries after))))
    (is (= 2 (count (:postings (first (:entries after))))))
    (is (= :duplicate (:outcome duplicate-result)))
    (is (= after duplicate))
    (is (= :invalid (:outcome invalid-result)))
    (is (= after invalid))))
