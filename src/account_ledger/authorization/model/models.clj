(ns account-ledger.authorization.model.models
  "Internal immutable state shapes. These schemas do not add boundary validation."
  (:require [account-ledger.shared.logic.identifiers :as identifiers]
            [account-ledger.shared.model.contracts :as schemas]
            [clojure.spec.alpha :as s]))

(s/def ::financial-balance decimal?)
(s/def ::held-amount (s/and decimal? #(not (neg? %))))
(s/def ::available-balance decimal?)
(s/def ::last-event-counter identifiers/counter?)
(s/def ::holds (s/map-of identifiers/id? (s/and decimal? pos?)))
(s/def ::snapshot
  (s/keys :req [:account/id :money/currency]
          :req-un [::financial-balance ::held-amount ::available-balance
                   ::last-event-counter ::holds]))
(s/def ::outcome #{:recorded :declined})
(s/def ::decision
  (s/keys :req [:transaction/id :account/id] :req-un [::outcome]))
(s/def ::command ::schemas/command)
(s/def ::history-record (s/and ::decision (s/keys :req-un [::command])))
(s/def ::history (s/coll-of ::history-record :kind vector?))
(s/def ::account-state (s/keys :req-un [::snapshot ::history]))
(s/def ::accounts (s/map-of identifiers/id? ::account-state))
(s/def ::destination #{:ledger :yield})
(s/def ::delivery-id (s/tuple identifiers/id? ::destination))
(s/def ::event ::schemas/event)
(s/def ::delivery
  (s/and (s/keys :req-un [::destination ::event])
         #(s/valid? ::delivery-id (:delivery/id %))))
(s/def ::config map?)
(s/def ::identities (s/map-of identifiers/id? ::decision))
(s/def ::delivery-order (s/coll-of ::delivery-id :kind vector?))
(s/def ::deliveries (s/map-of ::delivery-id ::delivery))
(s/def ::acknowledged (s/coll-of ::delivery-id :kind set?))
(s/def ::state
  (s/keys :req-un [::config ::accounts ::identities ::delivery-order
                   ::deliveries ::acknowledged]))
