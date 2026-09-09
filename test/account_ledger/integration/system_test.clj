(ns account-ledger.integration.system-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.authorization.api :as auth]
            [account-ledger.ledger.api :as ledger]
            [account-ledger.yield-fees.api :as yield]
            [account-ledger.system :as system]
            [account-ledger.fixtures :as fixtures]))

(defn daily [account day]
  {:account/id account :run-day day :reference-day (dec day)
   :booking-cutoff (dec day) :mode :daily})

(defn- fee-command []
  (fixtures/command "system/fee/manual" :debit 25.00M 2
                    {:purpose :fee :reference-day 1 :source-event-counter 1
                     :component/ids ["fee-day-one"] :fee/difference 25.00M
                     :fee/assessment {:component/id "fee-day-one" :component/type :ordinary
                                      :reference-day 1 :booking-day 2 :value-day 2
                                      :money/amount 25.00M}}))

(deftest rejected-fee-metadata-cannot-create-money-missing-from-assessment-history
  (let [app (system/create fixtures/config)]
    (system/submit! app (fixtures/command "principal" :debit 1.00M 1))
    (system/drain! app)
    (let [before (system/report app {:account/id "ACC-001" :day 2})]
      (doseq [command [(dissoc (fee-command) :fee/assessment)
                       (assoc-in (fee-command) [:fee/assessment :reference-day] 2)]]
        (is (= :invalid-fee-assessment (:reason (system/submit! app command))))
        (is (= before (system/report app {:account/id "ACC-001" :day 2})))
        (is (empty? (auth/pending-deliveries (:authorization app)))))
      (is (= :recorded (:outcome (system/run-day! app (daily "ACC-001" 2)))))
      (let [after (system/report app {:account/id "ACC-001" :day 2})]
        (is (= -26.00M (:financial-balance after)))
        (is (= 2 (:last-event-counter after)))
        (is (= [25.00M] (mapv :money/amount (:fees after))))
        (is (= [-1.00M -25.00M] (mapv :financial/effect (:journal after))))
        (system/run-day! app (daily "ACC-001" 2))
        (is (= after (system/report app {:account/id "ACC-001" :day 2})))))))

(deftest an-accepted-fee-can-resume-calculation-with-command-source-metadata
  (let [app (system/create fixtures/config)]
    (system/submit! app (fixtures/command "principal" :debit 1.00M 1))
    (is (= :recorded (:outcome (system/submit! app (fee-command)))))
    (system/drain! app)
    (let [before (system/report app {:account/id "ACC-001" :day 2})
          charge (first (:fees before))]
      (is (= -26.00M (:financial-balance before)))
      (is (= 1 (:source-event-counter charge)))
      (is (= [25.00M] (mapv :money/amount (:fees before))))
      (system/submit! app (fixtures/command "late-credit" :credit 1.00M 1 {:received-day 2}))
      (is (= :recorded (:outcome (system/run-day! app (daily "ACC-001" 2)))))
      (let [after (system/report app {:account/id "ACC-001" :day 2})]
        (is (= [charge] (take 1 (:fees after))))
        (is (= [25.00M -25.00M] (mapv :money/amount (:fees after))))
        (is (= "late-credit" (:cause/transaction-id (second (:fees after)))))
        (is (= 0.00M (:financial-balance after)))
        (is (= [-1.00M -25.00M 1.00M 25.00M] (mapv :financial/effect (:journal after))))
        (system/run-day! app (daily "ACC-001" 2))
        (is (= after (system/report app {:account/id "ACC-001" :day 2})))))))

(deftest accepted-interest-always-carries-the-receipt-that-prevents-another-payment
  (let [app (system/create fixtures/config)]
    (system/submit! app (fixtures/command "principal" :credit 100.00M 1))
    (system/run-day! app (daily "ACC-001" 2))
    (let [before (system/report app {:account/id "ACC-001" :day 3})
          ids (mapv :component/id (:interest/components before))
          command (fixtures/command "system/interest/manual" :credit 0.04M 3
                                    {:purpose :interest :reference-day 1 :source-event-counter 1
                                     :component/ids ids :settlement/id "manual"
                                     :period/end-day 1 :booking-cutoff 2})]
      (doseq [key [:settlement/id :period/end-day :booking-cutoff]]
        (is (= :invalid (:outcome (system/submit! app (dissoc command key)))))
        (is (= before (system/report app {:account/id "ACC-001" :day 3})))
        (is (empty? (auth/pending-deliveries (:authorization app)))))
      (is (= :recorded (:outcome (system/submit! app command))))
      (system/drain! app)
      (let [paid (system/report app {:account/id "ACC-001" :day 3})]
        (is (= 100.04M (:financial-balance paid)))
        (is (= 0.04M (:interest-paid paid)))
        (is (= 0.00M (:pending-interest paid)))
        (is (= [ids] (mapv :component/ids (:settlements paid))))
        (system/settle! app {:account/id "ACC-001" :settlement/id "later"
                            :run-day 4 :period/end-day 3 :booking-cutoff 3})
        (let [later (system/report app {:account/id "ACC-001" :day 4})]
          (is (= 100.04M (:financial-balance later)))
          (is (= (:journal paid) (:journal later)))
          (is (= 2 (:last-event-counter later))))))))

(deftest real-credit-accrual-and-payment-path
  (let [app (system/create fixtures/config)]
    (is (= :recorded (:outcome (system/submit! app (fixtures/command "credit" :credit 250M 1)))))
    (is (= {:pending-count 0 :errors []} (system/drain! app)))
    (is (= 250M (:money/amount (ledger/balance (:ledger app)
                                 {:account/id "ACC-001" :value-through-day 1 :booking-through-day 1}))))
    (is (= :recorded (:outcome (system/run-day! app (daily "ACC-001" 2)))))
    (is (= 0.10M (:pending-interest (yield/report (:yield-fees app) "ACC-001"))))
    (is (= :recorded (:outcome (system/settle! app {:account/id "ACC-001" :settlement/id "simple"
                                                  :run-day 2 :period/end-day 1 :booking-cutoff 1}))))
    (let [report (system/report app {:account/id "ACC-001" :day 2})]
      (is (:complete? report))
      (is (= 250.10M (:financial-balance report)))
      (is (= 0.10M (:interest-paid report)))
      (is (= 0.00M (:pending-interest report)))
      (is (= 2 (count (ledger/journal (:ledger app) "ACC-001")))))))

(deftest a-different-settlement-after-response-loss-cannot-pay-the-same-interest-again
  (let [app (system/create fixtures/config)
        submit-original auth/submit!]
    (system/submit! app (fixtures/command "principal" :credit 100.00M 1))
    (system/run-day! app (daily "ACC-001" 2))
    (let [component-ids (mapv :component/id (:interest/components
                                            (system/report app {:account/id "ACC-001" :day 2})))]
      (with-redefs [auth/submit! (fn [module command]
                                  (submit-original module command)
                                  (throw (ex-info "Controlled loss after payment" {})))]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled loss after payment"
                              (system/settle! app {:account/id "ACC-001" :settlement/id "first-period"
                                                   :run-day 3 :period/end-day 2 :booking-cutoff 2}))))
      (is (= 100.04M (:financial-balance (auth/snapshot (:authorization app) "ACC-001"))))
      (is (= 2 (count (auth/pending-deliveries (:authorization app)))))
      (let [next-result (system/settle! app {:account/id "ACC-001" :settlement/id "next-period"
                                            :run-day 4 :period/end-day 3 :booking-cutoff 3})
            report (system/report app {:account/id "ACC-001" :day 4})
            payments (filterv #(= :interest (:purpose %)) (:journal report))]
        (is (= :recorded (:outcome next-result)))
        (is (= 0.00M (get-in next-result [:settlement/receipt :money/amount])))
        (is (true? (:complete? report)))
        (is (= 100.04M (:financial-balance report) (get-in report [:accounting :money/amount])))
        (is (= 0.04M (:interest-paid report)))
        (is (= 0.00M (:pending-interest report)))
        (is (= 2 (:last-event-counter report)))
        (is (empty? (auth/pending-deliveries (:authorization app))))
        (is (= [0.04M] (mapv :financial/effect payments)))
        (is (= component-ids (vec (mapcat :component/ids (:settlements report)))))
        (is (= [component-ids] (mapv :component/ids payments)))
        (let [retry (system/settle! app {:account/id "ACC-001" :settlement/id "first-period"
                                        :run-day 5 :period/end-day 4 :booking-cutoff 4})]
          (is (= :duplicate (:outcome retry)))
          (is (= {:component/ids component-ids :money/amount 0.04M :run-day 3}
                 (select-keys (:original/receipt retry) [:component/ids :money/amount :run-day])))
          (is (= report (system/report app {:account/id "ACC-001" :day 4}))))))))

(deftest actual-hold-and-final-settlement
  (let [app (system/create fixtures/config)]
    (doseq [command (take 5 fixtures/events)] (system/submit! app command))
    (system/drain! app)
    (let [report (system/report app {:account/id "ACC-001" :day 4})]
      (is (= [465M 0M 465M 5]
             ((juxt :financial-balance :held-amount :available-balance :last-event-counter) report)))
      (is (= :settled (get (:authorization-states report) "Auth-A")))
      (is (= 4 (count (ledger/journal (:ledger app) "ACC-001")))))))

(deftest receiving-a-hold-cannot-invent-financial-interest
  (let [app (system/create fixtures/config)]
    (system/submit! app (fixtures/command "credit" :credit 100M 1))
    (system/drain! app)
    (let [result (system/submit! app (fixtures/command "hold" :authorization 50M 1 {:authorization/id "hold"}))
          malformed (assoc (:recorded/event result) :financial/effect 100M)]
      (is (= :invalid (:outcome (yield/receive! (:yield-fees app) malformed))))
      (is (= 1 (:source-event-counter (yield/report (:yield-fees app) "ACC-001")))))
    (system/drain! app)
    (system/run-day! app (daily "ACC-001" 2))
    (is (= 0.04M (:pending-interest (system/report app {:account/id "ACC-001" :day 2}))))))

(deftest failed-delivery-retries-recorded-movement
  (let [app (system/create fixtures/config)
        original-post ledger/post!
        first-delivery? (atom true)]
    (system/submit! app (fixtures/command "once" :credit 10M 1))
    (with-redefs [ledger/post! (fn [module event]
                                ;; The recipient records, but its acknowledgement is lost.
                                (let [result (original-post module event)]
                                  (if (compare-and-set! first-delivery? true false)
                                    (throw (ex-info "Controlled acknowledgement failure" {}))
                                    result)))]
      (let [failed (system/drain! app)]
        (is (= 1 (:pending-count failed)))
        (is (= 1 (count (:errors failed))))
        (is (false? (:complete? (system/report app {:account/id "ACC-001" :day 1})))))
      (is (= {:pending-count 0 :errors []} (system/drain! app))))
    (is (= 10M (:financial-balance (auth/snapshot (:authorization app) "ACC-001"))))
    (is (= 1 (:last-event-counter (auth/snapshot (:authorization app) "ACC-001"))))
    (is (= 1 (count (ledger/journal (:ledger app) "ACC-001")))))
  (testing "A complete-report query cannot flush or create a correction"
    (let [app (system/create fixtures/config)]
      (system/submit! app (fixtures/command "pending" :credit 1M 1))
      (let [pending (auth/pending-deliveries (:authorization app))]
        (is (false? (:complete? (system/report app {:account/id "ACC-001" :day 1})))))
      (is (= 2 (count (auth/pending-deliveries (:authorization app))))))))
