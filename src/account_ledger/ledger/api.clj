(ns account-ledger.ledger.api
  "Ledger accepts committed financial events and exposes immutable accounting views."
  (:require [account-ledger.ledger.domain :as domain]
            [account-ledger.ledger.memory :as memory]))

(defn create
  "Validate opening configuration and return an opaque in-memory Ledger."
  [config] (memory/create config))

(defn post!
  "Atomically append a committed financial movement, or return duplicate/invalid."
  [module movement] (memory/post-movement! module movement))

(defn balance
  "Return amount, currency and effective temporal boundaries without recording anything."
  [module query] (domain/account-balance (memory/view module) query))

(defn journal
  "Return the account's immutable entries in global local append order."
  [module account-id] (domain/account-journal (memory/view module) account-id))
