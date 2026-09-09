(ns account-ledger.authorization.db.memory-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.authorization.db.memory :as memory]
            [account-ledger.authorization.ports.api-server :as authorization]
            [account-ledger.fixtures :as fixtures])
  (:import [java.util.concurrent CountDownLatch TimeUnit]))

(defn competing-submissions [module commands]
  (let [ready (CountDownLatch. (count commands))
        start (CountDownLatch. 1)
        attempts (mapv (fn [command]
                         (future
                           (.countDown ready)
                           (when-not (.await start 5 TimeUnit/SECONDS)
                             (throw (ex-info "Start barrier timed out" {})))
                           (authorization/submit! module command)))
                       commands)]
    (try
      (when-not (.await ready 5 TimeUnit/SECONDS)
        (throw (ex-info "Submission preparation timed out" {})))
      (.countDown start)
      (mapv #(deref % 5000 ::timeout) attempts)
      (finally (.countDown start)))))

(deftest competing-holds-cannot-spend-the-same-availability
  (let [module (authorization/create fixtures/config)
        _ (authorization/submit! module (fixtures/command "fund" :credit 100.00M 1))
        commands [(fixtures/command "left" :authorization 60.00M 1 {:authorization/id "left-hold"})
                  (fixtures/command "right" :authorization 60.00M 1 {:authorization/id "right-hold"})]
        results (competing-submissions module commands)
        snapshot (authorization/snapshot module "ACC-001")
        history (authorization/history module "ACC-001")]
    (is (= {:recorded 1 :declined 1} (frequencies (map :outcome results))))
    (is (= [100.00M 60.00M 40.00M 2]
           ((juxt :financial-balance :held-amount :available-balance :last-event-counter) snapshot)))
    (is (= 3 (count history)))
    (is (= 2 (count (filter :recorded/snapshot history))))
    (is (= 3 (count (authorization/pending-deliveries module))))
    (let [original (first (filter #(= :declined (:outcome %)) results))
          _ (authorization/submit! module (fixtures/command "new-funds" :credit 100.00M 2))
          duplicate (authorization/submit! module {:transaction/id (:transaction/id original)})]
      (is (= original (:original/result duplicate)))
      (is (= 60.00M (:held-amount (authorization/snapshot module "ACC-001")))))))

(deftest concurrent-reused-identity-records-only-one-entire-outcome
  (let [module (authorization/create fixtures/config)
        results (competing-submissions module [(fixtures/command "same" :credit 10.00M 1)
                                                (fixtures/command "same" :credit 99.00M 1)])
        recorded (first (filter #(= :recorded (:outcome %)) results))
        duplicate (first (filter #(= :duplicate (:outcome %)) results))
        event (:recorded/event recorded)]
    (is (= {:recorded 1 :duplicate 1} (frequencies (map :outcome results))))
    (is (= recorded (:original/result duplicate)))
    (is (= (:money/amount event) (:financial-balance (authorization/snapshot module "ACC-001"))))
    (is (= 1 (count (authorization/history module "ACC-001"))))
    (is (= 2 (count (authorization/pending-deliveries module))))
    (is (= [event event] (mapv :event (authorization/pending-deliveries module))))))

(deftest source-movement-between-calculation-and-submission-requires-new-calculation
  (let [module (authorization/create fixtures/config)
        _ (authorization/submit! module (fixtures/command "fund" :credit 100.00M 1))
        prepared (promise)
        resume (promise)
        attempt (future
                  (let [source (:last-event-counter (authorization/snapshot module "ACC-001"))]
                    (deliver prepared source)
                    (when (= ::timeout (deref resume 5000 ::timeout))
                      (throw (ex-info "Submission release timed out" {})))
                    (authorization/submit! module
                                           (fixtures/command "system/interest" :credit 0.04M 2
                                                             {:purpose :interest :source-event-counter source
                                                              :settlement/id "month" :period/end-day 1 :booking-cutoff 1
                                                              :reference-day 1 :component/ids ["day1"]}))))]
    (is (= 1 (deref prepared 5000 ::timeout)))
    (authorization/submit! module (fixtures/command "later" :credit 100.00M 2))
    (let [before (authorization/snapshot module "ACC-001")
          history (authorization/history module "ACC-001")
          pending (authorization/pending-deliveries module)]
      (deliver resume true)
      (is (= :retry-required (:outcome (deref attempt 5000 ::timeout))))
      (is (= before (authorization/snapshot module "ACC-001")))
      (is (= history (authorization/history module "ACC-001")))
      (is (= pending (authorization/pending-deliveries module)))
      (let [payment (authorization/submit! module
                                           (fixtures/command "system/interest" :credit 0.08M 2
                                                             {:purpose :interest :source-event-counter 2
                                                              :settlement/id "month" :period/end-day 1 :booking-cutoff 1
                                                              :reference-day 1 :component/ids ["day1"]}))]
        (is (= :recorded (:outcome payment)))
        (is (= 3 (get-in payment [:recorded/event :event-counter])))
        (is (= 200.08M (:financial-balance (authorization/snapshot module "ACC-001"))))))))

(deftest competing-financial-proposals-cannot-both-record-from-one-source
  (let [module (authorization/create fixtures/config)
        commands (mapv #(fixtures/command % :credit 0.01M 2
                                          {:purpose :interest :source-event-counter 0
                                           :settlement/id % :period/end-day 1 :booking-cutoff 1
                                           :reference-day 1 :component/ids ["day1"]})
                       ["system/left" "system/right"])
        results (competing-submissions module commands)
        recorded (first (filter #(= :recorded (:outcome %)) results))
        stale (first (filter #(= :retry-required (:outcome %)) results))]
    (is (= {:recorded 1 :retry-required 1} (frequencies (map :outcome results))))
    (is (= :stale-source (:reason stale)))
    (is (= [0.01M 1]
           ((juxt :financial-balance :last-event-counter) (authorization/snapshot module "ACC-001"))))
    (is (= [(:transaction/id recorded)] (mapv :transaction/id (authorization/history module "ACC-001"))))
    (is (= 2 (count (authorization/pending-deliveries module))))))

(deftest snapshots-and-history-remain-immutable-after-later-events
  (let [module (authorization/create fixtures/config)
        original (authorization/submit! module (fixtures/command "first" :credit 10.00M 1))
        snapshot (authorization/snapshot module "ACC-001")
        history (authorization/history module "ACC-001")
        envelopes (authorization/pending-deliveries module)]
    (authorization/submit! module (fixtures/command "second" :debit 3.00M 2))
    (is (= 10.00M (:financial-balance snapshot)))
    (is (= ["first"] (mapv :transaction/id history)))
    (is (= 2 (count envelopes)))
    (is (= (first history) (first (authorization/history module "ACC-001"))))
    (is (= original (:original/result (authorization/submit! module {:transaction/id "first"}))))
    (is (= 7.00M (:financial-balance (authorization/snapshot module "ACC-001"))))))


(deftest failed-transition-preserves-the-entire-stored-value
  (let [initial {:counter 1 :records ["saved"]}
        module (memory/create initial)]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Calculation failed"
                          (memory/transact! module
                                            (fn [_] (throw (ex-info "Calculation failed" {}))))))
    (is (= initial (memory/read-state module)))
    (is (= :recorded
           (memory/transact! module
                             (fn [state]
                               [(-> state (update :counter inc) (update :records conj "next"))
                                :recorded]))))
    (is (= {:counter 2 :records ["saved" "next"]} (memory/read-state module)))
    (is (= {:counter 1 :records ["saved"]} initial))))
