(ns account-ledger.yield-fees.db.memory-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.yield-fees.db.memory :as memory]
            [account-ledger.yield-fees.logic.transitions :as transitions]))

(deftest memory-publishes-a-transition-before-returning-and-preserves-read-snapshots
  (let [configuration (contracts/normalize-config fixtures/config)
        initial (transitions/initial-state configuration)
        module (memory/create configuration {} initial)
        before (memory/read-state module)
        command {:transaction/id "system/pending" :account/id "ACC-001" :money/amount 0.04M}]
    (is (= command (memory/transact! module #(transitions/save-command % command))))
    (is (= command (transitions/pending-command (memory/read-state module) "ACC-001")))
    (is (= initial before))
    (is (empty? (get-in before [:accounts "ACC-001" :financial-intents])))
    (let [saved (memory/read-state module)]
      (is (thrown? clojure.lang.ExceptionInfo
                   (memory/transact! module (fn [_] (throw (ex-info "Local transition failed" {}))))))
      (is (= saved (memory/read-state module))))))
