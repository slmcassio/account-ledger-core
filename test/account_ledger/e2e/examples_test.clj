(ns account-ledger.e2e.examples-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.system :as system]
            [account-ledger.money :as money]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.authorization.api :as authorization]
            [account-ledger.ledger.api :as ledger]))

(defn- create-demo [id]
  (system/create (assoc fixtures/config :accounts
                       {id {:money/currency :AED :opening-balance 0M :account/type :aed-standard}})))

(defn- submit! [app id transaction-id type amount day extra]
  (system/submit! app (fixtures/command transaction-id type amount day (assoc extra :account/id id))))

(defn- daily! [app id run-day]
  (system/run-day! app {:account/id id :run-day run-day :reference-day (dec run-day)
                       :booking-cutoff (dec run-day) :mode :daily}))

(defn- historical! [app id day from through cause extra]
  (system/run-day! app (merge {:account/id id :run-day day :booking-cutoff (dec day)
                              :mode :historical :from-day from :through-day through
                              :cause/transaction-id cause} extra)))

(defn- report [app id day]
  (system/drain! app)
  (system/report app {:account/id id :day day}))

(deftest example-04-backdated-adjustment
  (let [id "EXAMPLE-04" app (create-demo id)]
    (submit! app id "A" :credit 1000M 1 {})
    (doseq [day (range 2 6)] (daily! app id day))
    (submit! app id "B" :credit 500M 5 {})
    (submit! app id "C" :debit 1200M 5 {:value-day 2})
    (historical! app id 6 2 4 "C" {})
    (daily! app id 6)
    (is (= 225M (:financial-balance (report app id 6))))
    (system/settle! app {:account/id id :settlement/id "example-04-month"
                        :run-day 6 :booking-cutoff 5 :period/end-day 5})
    (let [closed (report app id 6)]
      (is (= 226.72M (:financial-balance closed)))
      (is (= 1.72M (:interest-paid closed)))
      (is (= -1.20M (:pending-interest closed)))
      (is (= [25M 25M 25M] (mapv :money/amount (filter #(pos? (:money/amount %)) (:fees closed)))))
      (is (= 7 (count (:journal closed))))
      (historical! app id 6 2 4 "C" {})
      (is (= closed (report app id 6))))
    (daily! app id 7)
    (is (= 0.09M (:money/amount (first (filter #(= 6 (:reference-day %))
                                             (:interest/components (report app id 7)))))))))

(deftest example-05-unmatched-settlement
  (let [id "DEMO-001" app (create-demo id)]
    (submit! app id "C" :credit 500M 1 {})
    (let [result (submit! app id "S" :settlement 180M 1
                          {:authorization/id "Auth-Missing" :settlement/final? true})
          state (report app id 1)]
      (is (= :recorded (:outcome result)))
      (is (= [320M 0M 320M] ((juxt :financial-balance :held-amount :available-balance) state)))
      (is (= 2 (count (:journal state))))
      (is (= [{:type :missing-authorization :authorization/id "Auth-Missing"}]
             (:occurrences state)))
      (is (empty? (:interest/components state))))))

(deftest example-06-authorization-decisions
  (let [id "DEMO-002" app (create-demo id)]
    (submit! app id "D" :debit 10M 1 {})
    (submit! app id "C" :credit 50M 1 {})
    (is (= :recorded (:outcome (submit! app id "A" :authorization 40M 1 {:authorization/id "A"}))))
    (is (= :declined (:outcome (submit! app id "B" :authorization 10M 1 {:authorization/id "B"}))))
    (let [before (report app id 1)]
      (is (= [40M 40M 0M 3] ((juxt :financial-balance :held-amount :available-balance :last-event-counter) before))))
    (submit! app id "R" :reversal 10M 1 {:reversal/of "D"})
    (is (= :duplicate (:outcome (system/submit! app {:transaction/id "B" :malformed true}))))
    (is (= :recorded (:outcome (submit! app id "N" :authorization 10M 1 {:authorization/id "N"}))))
    (let [state (report app id 1)]
      (is (= [50M 50M 0M 5] ((juxt :financial-balance :held-amount :available-balance :last-event-counter) state)))
      (is (= :declined (get (:authorization-states state) "B")))
      (is (= 3 (count (:journal state)))))))

(deftest example-07-final-partial-and-release
  (doseq [final? [true false]]
    (let [id "DEMO-003" app (create-demo id)]
      (submit! app id "C" :credit 50M 1 {})
      (submit! app id "A" :authorization 20M 1 {:authorization/id "A"})
      (is (= 30M (:available-balance (report app id 1))))
      (submit! app id (if final? "F" "P") :settlement 15M 1
               {:authorization/id "A" :settlement/final? final?})
      (let [state (report app id 1)]
        (is (= 35M (:financial-balance state)))
        (is (= (if final? 0M 5M) (:held-amount state)))
        (is (= (if final? 35M 30M) (:available-balance state)))
        (is (= 2 (count (:journal state)))))
      (when-not final?
        (submit! app id "R" :release 5M 1 {:authorization/id "A"})
        (let [state (report app id 1)]
          (is (= [35M 0M 35M] ((juxt :financial-balance :held-amount :available-balance) state)))
          (is (= 2 (count (:journal state)))))))))

(deftest example-08-ordered-historical-views
  (let [id "DEMO-003" app (create-demo id)
        started (promise) release (promise)
        original-interest money/daily-interest
        intercepted? (atom false)]
    (submit! app id "A" :credit 100M 1 {})
    (system/drain! app)
    (with-redefs [money/daily-interest (fn [currency base]
                                       (when (compare-and-set! intercepted? false true)
                                         (deliver started true)
                                         @release)
                                       (original-interest currency base))]
      (let [calculation (future (daily! app id 2))]
        (try
          (is (= true (deref started 5000 :timeout)))
          ;; B is recorded while the Day 2 calculation is in flight.
          (submit! app id "B" :credit 50M 2 {:value-day 1})
          (finally (deliver release true)))
        (is (= :recorded (:outcome (deref calculation 5000 {:outcome :timeout}))))))
    (submit! app id "C" :credit 50M 2 {:value-day 1})
    (is (= :duplicate (:outcome (system/submit! app {:transaction/id "C" :bad :payload}))))
    (daily! app id 2)
    (let [day-two (report app id 2)]
      (is (= 200M (:financial-balance day-two)))
      (is (= 0.04M (:pending-interest day-two))))
    ;; Both inputs already exist. Explicit historical views implement the
    ;; document's B-then-C review without altering either receipt day.
    (historical! app id 3 1 1 "B" {:input-through-event-counter 2 :interest-only? true})
    (is (= 0.06M (:pending-interest (report app id 3))))
    (historical! app id 3 1 1 "C" {:input-through-event-counter 3 :interest-only? true})
    (let [state (report app id 3)]
      (is (= [0.04M 0.02M 0.02M] (mapv :money/amount (:interest/components state))))
      (is (= 0.08M (:pending-interest state)))
      (is (= 200M (:financial-balance state)))
      (is (= 3 (count (:journal state))))
      (historical! app id 3 1 1 "C" {:input-through-event-counter 3 :interest-only? true})
      (is (= state (report app id 3))))))
