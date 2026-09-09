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
