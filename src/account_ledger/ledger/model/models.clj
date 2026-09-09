(ns account-ledger.ledger.model.models
  "Internal Ledger data shapes. Business invariants belong to logic.core."
  (:require [account-ledger.shared.logic.identifiers :as identifiers]
            [account-ledger.shared.model.contracts :as schemas]
            [clojure.spec.alpha :as s]))

(s/def ::book-account identifiers/id?)
(s/def ::side #{:debit :credit})
(s/def ::installment-position pos-int?)
(s/def ::posting
  (s/and (s/keys :req [:money/amount :money/currency] :req-un [::side])
         #(s/valid? ::book-account (:book/account %))
         #(or (not (contains? % :installment/position))
              (s/valid? ::installment-position (:installment/position %)))))
(s/def ::journal-position pos-int?)
(s/def ::postings (s/coll-of ::posting :kind vector? :min-count 1))
(s/def ::journal-entry
  (s/and ::schemas/event
         (s/keys :req-un [::postings])
         #(s/valid? ::journal-position (:journal/position %))))
(s/def ::entries (s/coll-of ::journal-entry :kind vector?))
(s/def ::recorded-ids (s/coll-of :transaction/id :kind set?))
(s/def ::config map?)
(s/def ::state (s/keys :req-un [::config ::entries ::recorded-ids]))
