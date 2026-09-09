(ns account-ledger.yield-fees.model.models
  "Internal immutable records retained by Yield and Fees."
  (:require [account-ledger.shared.logic.identifiers :as identifiers]
            [account-ledger.shared.model.contracts :as schemas]
            [clojure.spec.alpha :as s]))

(s/def ::component
  (s/keys :req [:component/id :component/type :money/amount]
          :req-un [::schemas/reference-day ::schemas/booking-day ::schemas/value-day]
          :opt [:cause/transaction-id]
          :opt-un [::schemas/source-event-counter ::schemas/booking-cutoff]))
(s/def ::calculation-base #(instance? BigDecimal %))
(s/def ::calculation-target #(instance? BigDecimal %))
(s/def ::input-through-event-counter identifiers/counter?)
(s/def ::interest-component
  (s/and ::component
         (s/keys :req-un [::schemas/booking-cutoff ::schemas/source-event-counter
                          ::input-through-event-counter])
         #(s/valid? ::calculation-base (:calculation/base %))
         #(s/valid? ::calculation-target (:calculation/target %))))
(s/def ::component-ids (s/coll-of identifiers/id? :kind vector?))
(s/def ::settlement-receipt
  (s/and ::schemas/settlement
         (s/keys :req [:money/amount] :opt [:transaction/id])
         #(s/valid? ::component-ids (:component/ids %))))
(s/def ::purpose #{:fee :interest})
(s/def ::financial-intent
  (s/and ::schemas/command
         (s/keys :req [:money/amount]
                 :req-un [::purpose ::schemas/source-event-counter])
         #(s/valid? ::component-ids (:component/ids %))
         #(seq (:component/ids %))))
(s/def ::events (s/map-of pos-int? ::schemas/event))
(s/def ::fees (s/coll-of ::component :kind vector?))
(s/def ::interest-components (s/coll-of ::interest-component :kind vector?))
(s/def ::settlements (s/coll-of ::settlement-receipt :kind vector?))
(s/def ::financial-intents (s/coll-of ::financial-intent :kind vector?))
(s/def ::pending-financial-commands (s/map-of identifiers/id? ::financial-intent))
(s/def ::review-boundary
  (s/keys :req-un [::schemas/booking-cutoff ::input-through-event-counter]))
(s/def ::review-boundaries (s/map-of identifiers/day? ::review-boundary))
(s/def ::account-state
  (s/and (s/keys :req-un [::events ::fees ::settlements ::financial-intents
                                 ::pending-financial-commands]
                 :opt-un [::review-boundaries])
         #(s/valid? ::interest-components (:interest/components %))))
(s/def ::input-ids (s/map-of identifiers/id? ::schemas/event))
(s/def ::settlement-ids (s/map-of identifiers/id? ::settlement-receipt))
(s/def ::accounts (s/map-of identifiers/id? ::account-state))
(s/def ::state (s/keys :req-un [::input-ids ::settlement-ids ::accounts]))
