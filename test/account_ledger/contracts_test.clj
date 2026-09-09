(ns account-ledger.contracts-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.spec.alpha :as s]
            [account-ledger.contracts :as contracts]
            [account-ledger.fixtures :as fixtures]))

(deftest command-boundaries
  (let [config (contracts/normalize-config fixtures/config)
        credit (first fixtures/events)]
    (is (= {:valid? true :command credit} (contracts/validate-command config credit)))
    (doseq [[key value expected]
            [[:account/id "unknown" :unknown-account]
             [:money/currency :BHD :currency-mismatch]
             [:money/amount 1.001M :excess-precision]
             [:money/amount 0M :invalid-amount]
             [:money/amount 1.2 :invalid-amount]
             [:booking-day 0 :invalid-command]
             [:booking-day 2 :invalid-dates]
             [:transaction/type :invented :invalid-command]
             [:transaction/id "system/fake" :reserved-id]]]
      (is (= expected (:reason (contracts/validate-command config (assoc credit key value)))))))
  (is (= 0.000M (get-in (contracts/normalize-config fixtures/config) [:accounts "ACC-002" :daily-fee])))
  (doseq [config [{} (assoc-in fixtures/config [:accounts "ACC-001" :account/type] :missing)
                  (assoc-in fixtures/config [:account-types :aed-standard :daily-fee] -25M)]]
    (is (thrown? clojure.lang.ExceptionInfo (contracts/normalize-config config)))))

(deftest financial-path-requires-version-and-links
  (let [config (contracts/normalize-config fixtures/config)
        payment (merge (fixtures/command "system/payment/one" :credit 0.10M 2)
                       {:purpose :interest :reference-day 1 :component/ids ["interest/1"]
                        :source-event-counter 0})]
    (is (:valid? (contracts/validate-command config payment)))
    (is (false? (:valid? (contracts/validate-command config (dissoc payment :component/ids)))))
    (is (false? (:valid? (contracts/validate-command config (assoc payment :source-event-counter -1)))))
    (is (false? (:valid? (contracts/validate-command config
                          (-> payment (dissoc :source-event-counter) (assoc :transaction/id "fake-fee"))))))
    (is (false? (:valid? (contracts/validate-command config
                          (assoc (first fixtures/events) :purpose :invented)))))
    (is (false? (:valid? (contracts/validate-command config
                          (assoc (first fixtures/events) :installment-count 3 :money/amount 0.02M)))))))

(deftest computed-event-fields-cannot-be-injected-by-a-new-command
  (let [config (contracts/normalize-config fixtures/config)]
    (doseq [[key value] [[:installments [1M 1M 1M]] [:financial/effect 9999M]
                         [:event-counter 100] [:authorization/state :approved]
                         [:hold/remaining 100M] [:hold/released 100M] [:occurrences []]]]
      (is (= :unexpected-event-fields
             (:reason (contracts/validate-command config (assoc (first fixtures/events) key value))))))))

(deftest committed-events-preserve-operation-effects
  (let [config (contracts/normalize-config fixtures/config)
        credit (assoc (first fixtures/events) :event-counter 1 :financial/effect 1200M :purpose :principal)
        hold (assoc (nth fixtures/events 2) :event-counter 2 :financial/effect 0M :purpose :principal)]
    (is (contracts/valid-event? config credit))
    (is (contracts/valid-event? config hold))
    (is (false? (contracts/valid-event? config (assoc hold :financial/effect 100M))))
    (is (false? (contracts/valid-event? config (assoc credit :financial/effect -1200M))))
    (is (false? (contracts/valid-event? config (assoc credit :financial/effect 1201M))))
    (is (false? (contracts/valid-event? config (assoc credit :transaction/type :debit))))
    (is (false? (contracts/valid-event? config (assoc hold :transaction/type :release :financial/effect 1M))))))

(deftest dates-and-query-schemas
  (let [daily {:account/id "ACC-001" :mode :daily :run-day 2 :reference-day 1 :booking-cutoff 1}
        review {:account/id "ACC-001" :mode :historical :run-day 6 :booking-cutoff 5
                :from-day 2 :through-day 4 :cause/transaction-id "E7"}
        settlement {:account/id "ACC-001" :settlement/id "period-one" :run-day 6
                    :booking-cutoff 5 :period/end-day 5}]
    (is (s/valid? ::contracts/calculation daily))
    (is (not (s/valid? ::contracts/calculation (assoc daily :interest-only? true))))
    (is (not (s/valid? ::contracts/calculation (assoc review :interest-only? true))))
    (is (not (s/valid? ::contracts/calculation
                       (assoc review :input-through-event-counter 2 :interest-only? :yes))))
    (is (not (s/valid? ::contracts/calculation (assoc daily :booking-cutoff 2))))
    (is (s/valid? ::contracts/calculation review))
    (is (not (s/valid? ::contracts/calculation (assoc review :input-through-event-counter 2))))
    (is (s/valid? ::contracts/calculation (assoc review :input-through-event-counter 2 :interest-only? true)))
    (is (not (s/valid? ::contracts/calculation (assoc review :through-day 6))))
    (is (s/valid? ::contracts/settlement settlement))
    (is (not (s/valid? ::contracts/settlement (assoc settlement :period/end-day 6))))
    (is (s/valid? ::contracts/ledger-query {:account/id "ACC-001" :value-through-day 2
                                           :booking-through-day 5 :as-of-journal-position 0}))))
