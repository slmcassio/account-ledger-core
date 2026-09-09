(ns account-ledger.ledger.memory
  "Local storage port; callers never receive its mutable cell."
  (:require [account-ledger.ledger.domain :as domain]))

(defprotocol Store
  (view [store])
  (post-movement! [store movement]))

(defn create [config]
  (let [state (atom (domain/initial-state config))]
    (reify Store
      (view [_] @state)
      (post-movement! [_ movement]
        (locking state
          (let [[next-state result] (domain/record-movement @state movement)]
            (reset! state next-state)
            result))))))
