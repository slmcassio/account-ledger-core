(ns account-ledger.ledger.db.memory-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.ledger.db.memory :as memory]
            [account-ledger.ledger.logic.core :as logic]))

(defn event [id type amount effect counter]
  (assoc (fixtures/command id type amount 1)
         :financial/effect effect :event-counter counter
         :purpose :principal :occurrences []))

(def current-query
  {:account/id "ACC-001" :value-through-day 6 :booking-through-day 6})

(deftest concurrent-redelivery-atomically-records-one-whole-entry
  (let [module (memory/create (logic/initial-state fixtures/config))
        ready (java.util.concurrent.CountDownLatch. 2)
        start (promise)
        post (fn [amount]
               (future (.countDown ready) @start
                       (memory/transact! module #(logic/record-movement % (event "race" :credit amount amount 1)))))
        first-attempt (post 10.00M)
        second-attempt (post 20.00M)]
    (is (.await ready 5 java.util.concurrent.TimeUnit/SECONDS))
    (deliver start true)
    (let [results [(deref first-attempt 5000 ::timeout) (deref second-attempt 5000 ::timeout)]
          entries (logic/account-journal (memory/view module) "ACC-001")
          amount (:money/amount (logic/account-balance (memory/view module) current-query))]
      (is (= {:recorded 1 :duplicate 1} (frequencies (map :outcome results))))
      (is (= 1 (count entries)))
      (is (= 2 (count (:postings (first entries)))))
      (is (contains? #{10.00M 20.00M} amount)))))

(deftest immutable-views-and-failed-transitions-preserve-recorded-state
  (let [store (memory/create (logic/initial-state fixtures/config))
        before (memory/view store)
        movement (event "E1" :credit 10.00M 10.00M 1)]
    (is (= :recorded (:outcome (memory/transact! store #(logic/record-movement % movement)))))
    (let [recorded (memory/view store)]
      (is (= [] (:entries before)))
      (is (= #{} (:recorded-ids before)))
      (is (= ["E1"] (mapv :transaction/id (:entries recorded))))
      (is (= #{"E1"} (:recorded-ids recorded)))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Interrupted transition"
                            (memory/transact! store (fn [_]
                                                      (throw (ex-info "Interrupted transition" {}))))))
      (is (= recorded (memory/view store)))
      (is (= :duplicate (:outcome (memory/transact! store
                                                   #(logic/record-movement % {:transaction/id "E1"})))))
      (is (= recorded (memory/view store))))))
