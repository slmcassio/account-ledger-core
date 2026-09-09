(ns account-ledger.contracts-test
  (:require [clojure.test :refer [deftest is testing]]
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
                        :source-event-counter 0 :settlement/id "one"
                        :period/end-day 1 :booking-cutoff 1})]
    (is (:valid? (contracts/validate-command config payment)))
    (is (false? (:valid? (contracts/validate-command config (dissoc payment :component/ids)))))
    (is (false? (:valid? (contracts/validate-command config (assoc payment :source-event-counter -1)))))
    (is (false? (:valid? (contracts/validate-command config
                          (-> payment (dissoc :source-event-counter) (assoc :transaction/id "fake-fee"))))))
    (is (false? (:valid? (contracts/validate-command config
                          (assoc (first fixtures/events) :purpose :invented)))))
    (is (false? (:valid? (contracts/validate-command config
                          (assoc (first fixtures/events) :installment-count 3 :money/amount 0.02M)))))))

(defn- interest-command []
  (fixtures/command "system/payment/period-one" :credit 0.10M 6
                    {:purpose :interest :reference-day 5 :component/ids ["interest/day5"]
                     :source-event-counter 0 :settlement/id "period-one"
                     :period/end-day 5 :booking-cutoff 5}))

(deftest interest-commands-must-describe-a-recoverable-settlement
  (let [config (contracts/normalize-config fixtures/config)
        payment (interest-command)]
    (is (= {:valid? true :command payment} (contracts/validate-command config payment)))
    (is (:valid? (contracts/validate-command config (assoc payment :transaction/type :debit))))
    (is (:valid? (contracts/validate-command config
                                            (assoc payment :reference-day 2 :period/end-day 2))))
    (doseq [key [:settlement/id :period/end-day :booking-cutoff]]
      (testing (str "Missing settlement metadata " key)
        (is (= :invalid-interest-settlement
               (:reason (contracts/validate-command config (dissoc payment key)))))))
    (doseq [[key value] [[:settlement/id ""] [:settlement/id nil]
                         [:period/end-day 0] [:period/end-day nil]
                         [:booking-cutoff 0] [:booking-cutoff nil]
                         [:booking-cutoff 4] [:booking-cutoff 6]
                         [:reference-day 4] [:period/end-day 4]
                         [:value-day 5]]]
      (testing (str "Invalid settlement metadata " key " " value)
        (is (false? (:valid? (contracts/validate-command config (assoc payment key value)))))))
    (is (= :invalid-interest-settlement
           (:reason (contracts/validate-command config
                                                (assoc payment :reference-day 6 :period/end-day 6)))))))

(deftest committed-interest-needs-the-same-settlement-metadata
  (let [config (contracts/normalize-config fixtures/config)
        event (assoc (interest-command) :event-counter 1 :financial/effect 0.10M)]
    (is (contracts/valid-event? config event))
    (doseq [key [:settlement/id :period/end-day :booking-cutoff]]
      (is (false? (contracts/valid-event? config (dissoc event key)))))))

(defn- fee-command []
  (fixtures/command "system/fee/day1" :debit 25.00M 2
                    {:purpose :fee :reference-day 1 :source-event-counter 0
                     :component/ids ["fee/day1"] :fee/difference 25.00M
                     :fee/assessment {:component/id "fee/day1" :component/type :ordinary
                                      :reference-day 1 :booking-day 2 :value-day 2
                                      :money/amount 25.00M :source-event-counter 0
                                      :booking-cutoff 1}}))

(deftest fee-assessment-must-describe-the-financial-command
  (let [config (contracts/normalize-config fixtures/config)
        fee (fee-command)]
    (is (= {:valid? true :command fee} (contracts/validate-command config fee)))
    (doseq [key [:fee/assessment :fee/difference]]
      (testing (str "Missing " key)
        (is (= :invalid-fee-assessment
               (:reason (contracts/validate-command config (dissoc fee key)))))))
    (doseq [key [:component/id :component/type :reference-day :booking-day :value-day :money/amount]]
      (testing (str "Missing assessment " key)
        (is (= :invalid-fee-assessment
               (:reason (contracts/validate-command config (update fee :fee/assessment dissoc key)))))))
    (doseq [[path value] [[[:fee/assessment] nil]
                          [[:fee/assessment] []]
                          [[:fee/assessment :component/id] "other-component"]
                          [[:fee/assessment :component/id] ""]
                          [[:fee/assessment :component/type] :invented]
                          [[:fee/assessment :reference-day] 2]
                          [[:fee/assessment :booking-day] 1]
                          [[:fee/assessment :value-day] 1]
                          [[:fee/assessment :money/amount] 24.00M]
                          [[:fee/assessment :money/amount] -25.00M]
                          [[:fee/assessment :money/amount] 25.001M]
                          [[:fee/assessment :money/amount] 25.0]
                          [[:fee/assessment :source-event-counter] 1]
                          [[:fee/assessment :source-event-counter] nil]
                          [[:fee/assessment :booking-cutoff] 2]
                          [[:fee/assessment :booking-cutoff] 0]
                          [[:fee/assessment :booking-cutoff] nil]
                          [[:fee/assessment :cause/transaction-id] "cause"]
                          [[:cause/transaction-id] "cause"]
                          [[:component/ids] ["fee/day1" "unrepresented-component"]]
                          [[:fee/difference] -25.00M]
                          [[:fee/difference] 25.001M]
                          [[:fee/difference] 25.0]
                          [[:transaction/type] :credit]]]
      (testing (str "Inconsistent " path " " value)
        (is (= :invalid-fee-assessment
               (:reason (contracts/validate-command config (assoc-in fee path value)))))))))

(deftest fee-adjustments-keep-signed-money-and-original-value-dates
  (let [config (contracts/normalize-config fixtures/config)
        refund (-> (fee-command)
                   (assoc :transaction/type :credit :fee/difference -25.00M
                          :booking-day 7 :received-day 7 :value-day 2
                          :fee/original-value-day 2 :cause/transaction-id "reversal")
                   (update :fee/assessment assoc :component/type :adjustment
                           :money/amount -25.00M :booking-day 7 :booking-cutoff 6
                           :cause/transaction-id "reversal"))]
    (is (= {:valid? true :command refund} (contracts/validate-command config refund)))
    (is (:valid? (contracts/validate-command config
                                            (update refund :fee/assessment dissoc
                                                    :source-event-counter :booking-cutoff))))
    (is (= :invalid-fee-assessment
           (:reason (contracts/validate-command config
                                                (assoc-in refund [:fee/assessment :cause/transaction-id] "other")))))
    (is (false? (:valid? (contracts/validate-command config
                                                    (-> refund
                                                        (assoc :cause/transaction-id "")
                                                        (assoc-in [:fee/assessment :cause/transaction-id] ""))))))))

(deftest committed-fees-require-the-same-assessment-contract
  (let [config (contracts/normalize-config fixtures/config)
        event (assoc (fee-command) :event-counter 1 :financial/effect -25.00M)]
    (is (contracts/valid-event? config event))
    (is (false? (contracts/valid-event? config (dissoc event :fee/assessment))))
    (is (false? (contracts/valid-event? config
                                      (assoc-in event [:fee/assessment :money/amount] -25.00M))))))

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
