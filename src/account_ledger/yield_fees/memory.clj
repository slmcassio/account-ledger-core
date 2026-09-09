(ns account-ledger.yield-fees.memory
  "In-memory storage owned by Yield and Fees.")

(deftype Module [configuration storage boundary-ports])

(defn create [configuration ports]
  (Module. configuration
           (atom {:input-ids {} :settlement-ids {}
                  :accounts (into {} (for [id (keys (:accounts configuration))]
                                       [id {:events (sorted-map) :fees []
                                            :interest/components [] :settlements []}]))})
           ports))

(defn configuration [^Module module] (.-configuration module))
(defn ports [^Module module] (.-boundary-ports module))
(defn read-state [^Module module] @(.-storage module))

(defn transact!
  "Apply one local transition. The callback must not invoke any external port."
  [^Module module transition]
  (let [storage (.-storage module)]
    (locking storage
      (let [[next-state result] (transition @storage)]
        (reset! storage next-state)
        result))))
