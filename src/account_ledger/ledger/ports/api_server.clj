(ns account-ledger.ledger.ports.api-server
  "Ledger accepts committed financial events and exposes immutable accounting views."
  (:require [account-ledger.ledger.logic.core :as logic]
            [account-ledger.ledger.db.memory :as memory]))

(defn create
  "Validate opening configuration and return an opaque in-memory Ledger."
  [config] (memory/create (logic/initial-state config)))

(defn post!
  "Atomically append a committed financial movement, or return duplicate/invalid."
  [module movement] (memory/transact! module #(logic/record-movement % movement)))

(defn balance
  "Return amount, currency and effective temporal boundaries without recording anything."
  [module query] (logic/account-balance (memory/view module) query))

(defn journal
  "Return the account's immutable entries in global local append order."
  [module account-id] (logic/account-journal (memory/view module) account-id))
