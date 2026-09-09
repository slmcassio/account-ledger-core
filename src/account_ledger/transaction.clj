(ns account-ledger.transaction
  "The external entry adapter depends only on Authorization."
  (:require [account-ledger.authorization.ports.api-server :as authorization]))

(defn submit! [authorization-module command]
  (authorization/submit! authorization-module command))
