(ns account-ledger.authorization.ports.api-server-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.authorization.ports.api-server :as authorization]
            [account-ledger.fixtures :as fixtures]))

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
