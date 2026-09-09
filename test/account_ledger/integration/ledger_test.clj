(ns account-ledger.integration.ledger-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.ledger.api :as ledger]))

(defn event [id type amount effect counter & [extra]]
  (merge (fixtures/command id type amount 1)
         {:financial/effect effect :event-counter counter
          :purpose :principal :occurrences []} extra))

(def current-query
  {:account/id "ACC-001" :value-through-day 6 :booking-through-day 6})

(deftest ordinary-posting-reaches-a-queryable-balanced-journal
  (let [module (ledger/create fixtures/config)]
    (is (= {:outcome :recorded :transaction/id "E1"}
           (ledger/post! module (event "E1" :credit 1200.00M 1200.00M 1))))
    (is (= 1200.00M (:money/amount (ledger/balance module current-query))))
    (is (= :AED (:money/currency (ledger/balance module current-query))))
    (is (= 1 (get-in (ledger/balance module current-query)
                    [:query :as-of-journal-position])))
    (is (= {:outcome :recorded :transaction/id "E2"}
           (ledger/post! module (event "E2" :debit 950.00M -950.00M 2))))
    (is (= 250.00M (:money/amount (ledger/balance module current-query))))
    (is (= 2 (count (ledger/journal module "ACC-001"))))))

(deftest duplicate-id-precedes-payload-validation
  (let [module (ledger/create fixtures/config)
        original (event "E1" :credit 1200.00M 1200.00M 1)]
    (ledger/post! module original)
    (let [before (ledger/journal module "ACC-001")]
      (is (= {:outcome :duplicate :transaction/id "E1"}
             (ledger/post! module {:transaction/id "E1" :money/amount "malformed"})))
      (is (= before (ledger/journal module "ACC-001"))))
    (is (= 1200.00M (:money/amount (ledger/balance module current-query))))))

(deftest opening-state-is-not-a-synthetic-journal
  (let [config (assoc-in fixtures/config [:accounts "ACC-001" :opening-balance] 42.00M)
        module (ledger/create config)]
    (is (= [] (ledger/journal module "ACC-001")))
    (is (= {:account/id "ACC-001" :money/currency :AED :money/amount 42.00M
            :query (assoc current-query :as-of-journal-position 0)}
           (ledger/balance module current-query)))))

(deftest rejected-deliveries-append-no-partial-journal-and-do-not-reserve-identity
  (let [module (ledger/create fixtures/config)]
    (doseq [type [:authorization :release :declined]]
      (is (= :invalid (:outcome (ledger/post! module (event (name type) type 1.00M 0.00M 1))))))
    (is (= :invalid (:outcome (ledger/post! module (event "same-id" :credit 1.00M -1.00M 1)))))
    (is (= [] (ledger/journal module "ACC-001")))
    (is (= :recorded (:outcome (ledger/post! module (event "same-id" :credit 1.00M 1.00M 1)))))
    (is (= 1.00M (:money/amount (ledger/balance module current-query))))))

(deftest new-query-and-configuration-boundaries-are-validated
  (doseq [config [nil {} (assoc fixtures/config :accounts {})
                  (assoc-in fixtures/config [:accounts "ACC-001" :opening-balance] 0.001M)
                  (assoc-in fixtures/config [:accounts "ACC-001" :money/currency] :BHD)]]
    (is (thrown? clojure.lang.ExceptionInfo (ledger/create config))))
  (let [module (ledger/create fixtures/config)]
    (doseq [query [(dissoc current-query :booking-through-day)
                   (assoc current-query :value-through-day 0)
                   (assoc current-query :account/id "unknown")
                   (assoc current-query :as-of-journal-position -1)
                   (assoc current-query :as-of-journal-position 1)]]
      (is (thrown? clojure.lang.ExceptionInfo (ledger/balance module query))))
    (is (thrown? clojure.lang.ExceptionInfo (ledger/journal module "unknown")))))

(deftest historical-views-retain-economic-booking-and-arrival-boundaries
  (let [module (ledger/create fixtures/config)
        principal-events [(event "E1" :credit 1200.00M 1200.00M 1)
                          (event "E2" :debit 950.00M -950.00M 2)
                          (event "E4" :credit 400.00M 400.00M 4
                                 {:booking-day 3 :value-day 3 :received-day 3})
                          (event "E5" :settlement 185.00M -185.00M 5
                                 {:booking-day 4 :value-day 4 :received-day 4
                                  :authorization/id "Auth-A" :settlement/final? true})
                          (event "E6" :settlement 180.00M -180.00M 6
                                 {:booking-day 4 :value-day 4 :received-day 4
                                  :authorization/id "Auth-Z" :settlement/final? true
                                  :occurrences [{:reason :missing-authorization
                                                 :authorization/id "Auth-Z"}]})]
        day-two-query (assoc current-query :value-through-day 2 :booking-through-day 5)]
    (doseq [movement principal-events] (is (= :recorded (:outcome (ledger/post! module movement)))))
    (let [earlier (ledger/balance module day-two-query)
          day-four-close (ledger/balance module (assoc current-query :value-through-day 4
                                                                   :booking-through-day 4))
          old-journal (ledger/journal module "ACC-001")]
      (is (= 250.00M (:money/amount earlier)))
      (is (= 285.00M (:money/amount day-four-close)))
      (is (= [1 2 3 4 5] (mapv :journal/position old-journal)))
      (is (= [1 2 4 5 6] (mapv :event-counter old-journal)))
      (is (= :recorded (:outcome (ledger/post! module
                                             (event "E7" :debit 620.00M -620.00M 7
                                                    {:booking-day 5 :value-day 2 :received-day 5})))))
      (is (= -370.00M (:money/amount (ledger/balance module day-two-query))))
      (is (= 30.00M (:money/amount (ledger/balance module
                                                (assoc day-two-query :value-through-day 3)))))
      (is (= -335.00M (:money/amount (ledger/balance module
                                                  (assoc day-two-query :value-through-day 4)))))
      (is (= earlier (ledger/balance module (:query earlier))))
      (is (= day-four-close (ledger/balance module (:query day-four-close))))
      (is (= old-journal (subvec (ledger/journal module "ACC-001") 0 5)))
      (let [after-e7 (ledger/balance module (assoc day-two-query :booking-through-day 6))]
        (is (= :recorded (:outcome (ledger/post! module
                                               (dissoc (event "E9" :reversal nil 620.00M 8
                                                               {:booking-day 6 :value-day 2 :received-day 6
                                                                :reversal/of "E7"}) :money/amount)))))
        (is (= -370.00M (:money/amount (ledger/balance module day-two-query))))
        (is (= 250.00M (:money/amount (ledger/balance module
                                                   (assoc day-two-query :booking-through-day 6)))))
        (is (= after-e7 (ledger/balance module (:query after-e7))))
        (is (= 7 (count (ledger/journal module "ACC-001")))))
      (is (= 285.00M (:money/amount day-four-close))))))

(deftest installment-arrival-has-one-global-position-and-no-extra-parent-credit
  (let [module (ledger/create fixtures/config)
        bhd-query (assoc current-query :account/id "ACC-002" :value-through-day 5
                                        :booking-through-day 5)]
    (ledger/post! module (event "E1" :credit 1200.00M 1200.00M 1))
    (let [before-arrival (ledger/balance module bhd-query)
          movement (event "E10" :credit 10.000M 10.000M 1
                          {:account/id "ACC-002" :money/currency :BHD :booking-day 5
                           :value-day 5 :received-day 6 :installment-count 3
                           :installments [3.333M 3.333M 3.334M]})]
      (is (= 0.000M (:money/amount before-arrival)))
      (is (= 1 (get-in before-arrival [:query :as-of-journal-position])))
      (is (= :recorded (:outcome (ledger/post! module movement))))
      (let [entries (ledger/journal module "ACC-002")
            entry (first entries)
            total (fn [side] (reduce + 0.000M (map :money/amount
                                                 (filter #(= side (:side %)) (:postings entry)))))]
        (is (= 1 (count entries)))
        (is (= 2 (:journal/position entry)))
        (is (= 1 (:event-counter entry)))
        (is (= 6 (count (:postings entry))))
        (is (= [3.333M 3.333M 3.334M] (:installments entry)))
        (is (= 10.000M (total :debit) (total :credit)))
        (is (= 10.000M (:money/amount (ledger/balance module bhd-query))))
        (is (= before-arrival (ledger/balance module (:query before-arrival))))
        (is (= 0.000M (:money/amount (ledger/balance module
                                                   (assoc bhd-query :value-through-day 4)))))
        (is (= :duplicate (:outcome (ledger/post! module movement))))
        (is (= entries (ledger/journal module "ACC-002")))))))

(deftest pure-reports-do-not-post-even-with-backdated-refund-metadata
  (let [module (ledger/create fixtures/config)
        refund (event "system/ACC-001/fee/refund-H2" :credit 25.00M 25.00M 1
                      {:booking-day 7 :value-day 6 :received-day 7 :purpose :fee
                       :source-event-counter 0 :reference-day 2 :component/ids ["fee-H2-refund"]
                       :fee/difference -25.00M :fee/original-value-day 6
                       :cause/transaction-id "E9"
                       :fee/assessment {:component/id "fee-H2-refund" :component/type :adjustment
                                        :reference-day 2 :booking-day 7 :value-day 6
                                        :money/amount -25.00M :source-event-counter 0
                                        :booking-cutoff 6 :cause/transaction-id "E9"}})]
    (is (= :recorded (:outcome (ledger/post! module refund))))
    (let [before (ledger/journal module "ACC-001")
          entry (first before)]
      (is (= refund (dissoc entry :journal/position :postings)))
      (is (= 0.00M (:money/amount (ledger/balance module current-query))))
      (is (= 25.00M (:money/amount (ledger/balance module
                                                (assoc current-query :booking-through-day 7)))))
      (is (= 0.00M (:money/amount (ledger/balance module
                                               (assoc current-query :booking-through-day 7
                                                                    :value-through-day 5)))))
      (is (= before (ledger/journal module "ACC-001"))))))

(deftest concurrent-redelivery-atomically-records-one-whole-entry
  (let [module (ledger/create fixtures/config)
        ready (java.util.concurrent.CountDownLatch. 2)
        start (promise)
        post (fn [amount]
               (future (.countDown ready) @start
                       (ledger/post! module (event "race" :credit amount amount 1))))
        first-attempt (post 10.00M)
        second-attempt (post 20.00M)]
    (is (.await ready 5 java.util.concurrent.TimeUnit/SECONDS))
    (deliver start true)
    (let [results [(deref first-attempt 5000 ::timeout) (deref second-attempt 5000 ::timeout)]
          entries (ledger/journal module "ACC-001")
          amount (:money/amount (ledger/balance module current-query))]
      (is (= {:recorded 1 :duplicate 1} (frequencies (map :outcome results))))
      (is (= 1 (count entries)))
      (is (= 2 (count (:postings (first entries)))))
      (is (contains? #{10.00M 20.00M} amount)))))

(deftest invalid-last-installment-cannot-leave-the-earlier-pairs-recorded
  (let [module (ledger/create fixtures/config)
        movement (event "E10" :credit 10.000M 10.000M 1
                        {:account/id "ACC-002" :money/currency :BHD
                         :booking-day 5 :value-day 5 :received-day 6
                         :installment-count 3 :installments [3.333M 3.333M 3.3341M]})
        query (assoc current-query :account/id "ACC-002")]
    (is (= :invalid (:outcome (ledger/post! module movement))))
    (is (= [] (ledger/journal module "ACC-002")))
    (is (= 0.000M (:money/amount (ledger/balance module query))))
    (is (= 0 (get-in (ledger/balance module query) [:query :as-of-journal-position])))
    (is (= :recorded (:outcome (ledger/post! module
                                           (assoc movement :installments [3.333M 3.333M 3.334M])))))
    (is (= 10.000M (:money/amount (ledger/balance module query))))
    (is (= 1 (count (ledger/journal module "ACC-002"))))))
