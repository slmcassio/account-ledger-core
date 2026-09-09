(ns account-ledger.authorization.api
  "Authorization application boundary. Only this module owns operational state."
  (:require [account-ledger.authorization.memory :as memory]))

(defn create [config] (memory/create config))
(defn submit! [module command] (memory/submit! module command))
(defn snapshot [module account-id] (memory/snapshot module account-id))
(defn history [module account-id] (memory/history module account-id))
(defn pending-deliveries [module] (memory/pending-deliveries module))
(defn ack-delivery! [module delivery-id] (memory/ack-delivery! module delivery-id))
