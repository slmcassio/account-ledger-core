(ns account-ledger.authorization.memory
  "Local storage adapter. A lock protects one pure transition and its replacement."
  (:require [account-ledger.authorization.domain :as domain]
            [account-ledger.contracts :as contracts]))

(deftype Module [state])

(defn create [config]
  (Module. (atom (domain/initial-state (contracts/normalize-config config)))))

(defn submit! [^Module module command]
  (let [state (.-state module)]
    (locking state
      (let [[next-state result] (domain/submit @state command)]
        (reset! state next-state)
        result))))

(defn snapshot [^Module module account-id]
  (or (get-in @(.-state module) [:accounts account-id :snapshot])
      (throw (ex-info "Unknown account" {:reason :unknown-account :account/id account-id}))))

(defn history [^Module module account-id]
  (snapshot module account-id)
  (get-in @(.-state module) [:accounts account-id :history]))

(defn pending-deliveries [^Module module]
  (let [state @(.-state module)]
    (into [] (keep (:deliveries state)) (:delivery-order state))))

(defn ack-delivery! [^Module module delivery-id]
  (let [state (.-state module)]
    (locking state
      (cond
        (get-in @state [:deliveries delivery-id])
        (do (swap! state #(-> % (update :deliveries dissoc delivery-id)
                             (update :acknowledged conj delivery-id)))
            {:delivery/id delivery-id :acknowledged? true})
        (contains? (:acknowledged @state) delivery-id)
        {:delivery/id delivery-id :acknowledged? true}
        :else {:delivery/id delivery-id :acknowledged? false :reason :unknown-delivery}))))
