(ns account-ledger.integration.authorization-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.authorization.api :as authorization]
            [account-ledger.fixtures :as fixtures])
  (:import [java.util.concurrent CountDownLatch TimeUnit]))

(deftest approved-events-are-recorded-with-original-deliveries
  (let [module (authorization/create fixtures/config)
        results (mapv #(authorization/submit! module %) (take 5 fixtures/events))
        deliveries (authorization/pending-deliveries module)
        history (authorization/history module "ACC-001")
        before (authorization/snapshot module "ACC-001")
        first-id (:delivery/id (first deliveries))]
    (is (not (instance? clojure.lang.IDeref module)))
    (is (= (repeat 5 :recorded) (map :outcome results)))
    (is (= 465.00M (:financial-balance before)))
    (is (= 9 (count deliveries)))
    (is (= ["E1" "E1" "E2" "E2" "E3" "E4" "E4" "E5" "E5"]
           (mapv #(get-in % [:event :transaction/id]) deliveries)))
    (is (= [:yield] (mapv :destination (filter #(= "E3" (get-in % [:event :transaction/id])) deliveries))))
    (is (= (take 5 fixtures/events) (map :command history)))
    (is (every? #(not (contains? (:event %) :financial-balance)) deliveries))
    (is (= {:delivery/id first-id :acknowledged? true}
           (authorization/ack-delivery! module first-id)))
    (is (= {:delivery/id first-id :acknowledged? true}
           (authorization/ack-delivery! module first-id)))
    (is (= 8 (count (authorization/pending-deliveries module))))
    (is (= before (authorization/snapshot module "ACC-001")))
    (is (= history (authorization/history module "ACC-001")))
    (is (= {:delivery/id :unknown :acknowledged? false :reason :unknown-delivery}
           (authorization/ack-delivery! module :unknown)))))

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
                                                              :reference-day 1 :component/ids ["day1"]}))]
        (is (= :recorded (:outcome payment)))
        (is (= 3 (get-in payment [:recorded/event :event-counter])))
        (is (= 200.08M (:financial-balance (authorization/snapshot module "ACC-001"))))))))

(deftest competing-financial-proposals-cannot-both-record-from-one-source
  (let [module (authorization/create fixtures/config)
        commands (mapv #(fixtures/command % :credit 0.01M 2
                                          {:purpose :interest :source-event-counter 0
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

(deftest external-boundaries-return-invalid-without-recording
  (let [module (authorization/create fixtures/config)
        original (authorization/snapshot module "ACC-001")]
    (doseq [command [nil :malformed "malformed" {}
                     (fixtures/command "float" :credit 1.1 1)
                     (fixtures/command "precision" :credit 1.001M 1)
                     (fixtures/command "zero" :credit 0.00M 1)
                     (fixtures/command "negative" :credit -1.00M 1)
                     (fixtures/command "unknown" :credit 1.00M 1 {:account/id "missing"})
                     (fixtures/command "currency" :credit 1.00M 1 {:money/currency :BHD})
                     (fixtures/command "future-booking" :credit 1.00M 2 {:received-day 1})
                     (fixtures/command "system/reserved" :credit 1.00M 1)]]
      (is (= :invalid (:outcome (authorization/submit! module command)))))
    (is (= original (authorization/snapshot module "ACC-001")))
    (is (= [] (authorization/history module "ACC-001")))
    (is (= [] (authorization/pending-deliveries module)))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Unknown account"
                          (authorization/snapshot module "missing")))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Unknown account"
                          (authorization/history module "missing")))))
