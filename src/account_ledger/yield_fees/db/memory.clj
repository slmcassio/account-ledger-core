(ns account-ledger.yield-fees.db.memory
  "Opaque local state cell. External calls never run inside its lock.")

(deftype Module [configuration storage boundary-ports])

(defn create [configuration ports initial-state]
  (Module. configuration (atom initial-state) ports))

(defn configuration [^Module module] (.-configuration module))
(defn ports [^Module module] (.-boundary-ports module))
(defn read-state [^Module module] @(.-storage module))

(defn transact!
  "Apply one pure local transition before returning its result."
  [^Module module transition]
  (let [storage (.-storage module)]
    (locking storage
      (let [[next-state result] (transition @storage)]
        (reset! storage next-state)
        result))))
