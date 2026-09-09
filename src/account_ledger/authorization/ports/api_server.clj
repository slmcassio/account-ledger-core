(ns account-ledger.authorization.ports.api-server
  "Inbound application boundary for commands, queries and delivery acknowledgements."
  (:require [account-ledger.authorization.db.memory :as memory]
            [account-ledger.authorization.logic.core :as logic]
            [account-ledger.shared.logic.contracts :as contracts]))

(defn create [config]
  (memory/create (logic/initial-state (contracts/normalize-config config))))

(defn submit! [module command]
  (memory/transact! module #(logic/submit % command)))

(defn snapshot [module account-id]
  (logic/snapshot (memory/read-state module) account-id))

(defn history [module account-id]
  (logic/history (memory/read-state module) account-id))

(defn pending-deliveries [module]
  (logic/pending-deliveries (memory/read-state module)))

(defn ack-delivery! [module delivery-id]
  (memory/transact! module #(logic/ack-delivery % delivery-id)))
