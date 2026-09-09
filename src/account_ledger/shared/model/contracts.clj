(ns account-ledger.shared.model.contracts
  "Shared boundary data shapes; command validation lives in shared.logic.contracts."
  (:require [clojure.spec.alpha :as s]
            [account-ledger.shared.logic.identifiers :as identifiers]))

(def computed-event-fields
  [:installments :financial/effect :event-counter :authorization/state
   :hold/remaining :hold/released :occurrences])
(s/def :transaction/id identifiers/id?)
(s/def :transaction/type #{:credit :debit :authorization :settlement :release :reversal})
(s/def :account/id identifiers/id?)
(s/def :money/currency #{:AED :BHD})
(s/def :money/amount #(instance? BigDecimal %))
(s/def ::booking-day identifiers/day?)
(s/def ::value-day identifiers/day?)
(s/def ::received-day identifiers/day?)
(s/def ::event-counter pos-int?)
(s/def ::source-event-counter identifiers/counter?)
(s/def :financial/effect #(instance? BigDecimal %))
(s/def ::command
  (s/keys :req [:transaction/id :transaction/type :account/id :money/currency]
          :req-un [::booking-day ::value-day ::received-day]))
(s/def ::event
  (s/and ::command
         (s/keys :req [:financial/effect] :req-un [::event-counter])))
(s/def ::financial-balance #(instance? BigDecimal %))
(s/def ::held-amount #(instance? BigDecimal %))
(s/def ::available-balance #(instance? BigDecimal %))
(s/def ::last-event-counter identifiers/counter?)
(s/def ::snapshot
  (s/keys :req [:account/id] :req-un [::financial-balance ::held-amount
                                    ::available-balance ::last-event-counter]))
(s/def ::outcome #{:recorded :declined :duplicate :retry-required :invalid})
(s/def ::result (s/keys :req-un [::outcome] :opt [:transaction/id]))
(s/def ::run-day identifiers/day?)
(s/def ::reference-day identifiers/day?)
(s/def ::booking-cutoff identifiers/day?)
(s/def ::mode #{:daily :historical})
(s/def ::from-day identifiers/day?)
(s/def ::through-day identifiers/day?)
(s/def :cause/transaction-id identifiers/id?)
(s/def :component/id identifiers/id?)
(s/def :component/type #{:ordinary :adjustment})
(s/def ::fee-assessment
  (s/keys :req [:component/id :component/type :money/amount]
          :req-un [::reference-day ::booking-day ::value-day]
          :opt [:cause/transaction-id]
          :opt-un [::source-event-counter ::booking-cutoff]))
(s/def ::calculation
  (s/and (s/keys :req [:account/id]
                 :req-un [::run-day ::booking-cutoff ::mode])
         #(= (:booking-cutoff %) (dec (:run-day %)))
         #(case (:mode %)
            :daily (= (:reference-day %) (dec (:run-day %)))
            :historical (and (identifiers/day? (:from-day %)) (identifiers/day? (:through-day %))
                             (<= (:from-day %) (:through-day %) (:booking-cutoff %))
                             (identifiers/id? (:cause/transaction-id %))))
         #(or (not (contains? % :input-through-event-counter))
              (and (= :historical (:mode %)) (true? (:interest-only? %))
                   (identifiers/counter? (:input-through-event-counter %))))
         #(or (not (contains? % :interest-only?))
              (and (true? (:interest-only? %)) (= :historical (:mode %))
                   (contains? % :input-through-event-counter)))))
(s/def :settlement/id identifiers/id?)
(s/def :period/end-day identifiers/day?)
(s/def ::settlement
  (s/and (s/keys :req [:account/id :settlement/id :period/end-day]
                 :req-un [::run-day ::booking-cutoff])
         #(= (:booking-cutoff %) (dec (:run-day %)))
         #(<= (:period/end-day %) (:booking-cutoff %))))
(s/def ::value-through-day identifiers/day?)
(s/def ::booking-through-day identifiers/day?)
(s/def ::as-of-journal-position identifiers/counter?)
(s/def ::ledger-query
  (s/keys :req [:account/id] :req-un [::value-through-day ::booking-through-day]
          :opt-un [::as-of-journal-position]))
