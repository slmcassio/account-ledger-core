(ns account-ledger.transaction
  "The external entry adapter depends only on Authorization."
  (:require [account-ledger.authorization.api :as authorization]))

(defn submit! [authorization-module command]
  (authorization/submit! authorization-module command))
