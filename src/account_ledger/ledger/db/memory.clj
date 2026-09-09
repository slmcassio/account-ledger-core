(ns account-ledger.ledger.db.memory
  "Opaque local storage. Transitions run atomically and call no external module.")

(defprotocol Store
  (view [store] "Read the immutable current state.")
  (transact! [store transition] "Apply a pure state -> [next-state result] transition."))

(defn create [initial-state]
  (let [state (atom initial-state)]
    (reify Store
      (view [_] @state)
      (transact! [_ transition]
        (locking state
          (let [[next-state result] (transition @state)]
            (reset! state next-state)
            result))))))
