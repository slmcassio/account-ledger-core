(ns account-ledger.authorization.db.memory
  "Local state storage. One lock commits a pure transition and its result together.")

(deftype Module [state])

(defn create [initial-state]
  (Module. (atom initial-state)))

(defn read-state [^Module module]
  @(.-state module))

(defn transact!
  "Run one local transition. The callback must not call another module."
  [^Module module transition]
  (let [state (.-state module)]
    (locking state
      (let [[next-state result] (transition @state)]
        (reset! state next-state)
        result))))
