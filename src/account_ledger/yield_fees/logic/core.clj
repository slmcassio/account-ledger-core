(ns account-ledger.yield-fees.logic.core
  "Pure dated balances, assessments and component selection."
  (:require [account-ledger.shared.logic.money :as money]))

(defn input-view
  "An observed higher counter does not close a missing position in the feed."
  [events]
  (let [counters (set (map :event-counter events))
        prefix (loop [n 0] (if (contains? counters (inc n)) (recur (inc n)) n))]
    {:source-event-counter prefix :complete? (= prefix (count counters))}))

(defn total [currency records]
  (money/amount currency (reduce + 0M (map :money/amount records))))

(defn closing-base [account events reference-day booking-cutoff]
  (money/amount (:money/currency account)
                (reduce + (:opening-balance account)
                        (for [event events
                              :when (and (<= (:booking-day event) booking-cutoff)
                                         (<= (:value-day event) reference-day))]
                          (:financial/effect event)))))

(defn fee-base [account events reference-day booking-cutoff]
  (closing-base account
                (remove #(and (= :fee (:purpose %))
                              (= reference-day (:reference-day %))) events)
                reference-day booking-cutoff))

(defn fee-target [account base]
  (if (neg? base) (:daily-fee account) (money/amount (:money/currency account) 0M)))

(defn assessment
  "Compare rounded targets with every recorded component, whether settled or not."
  [account account-state request reference-day]
  (let [events (vals (:events account-state))
        cutoff (:booking-cutoff request)
        bound (or (:input-through-event-counter request)
                  (reduce max 0 (for [event events
                                      :when (and (= :principal (:purpose event))
                                                 (<= (:booking-day event) cutoff)
                                                 (<= (:value-day event) reference-day))]
                                  (:event-counter event))))
        interest-events (if (:input-through-event-counter request)
                          (remove #(and (= :principal (:purpose %))
                                        (> (:event-counter %) bound)) events)
                          events)
        interest-base (closing-base account interest-events reference-day cutoff)
        fee-base (fee-base account events reference-day cutoff)
        currency (:money/currency account)
        fees (filterv #(= reference-day (:reference-day %)) (:fees account-state))
        components (filterv #(= reference-day (:reference-day %)) (:interest/components account-state))
        fee-target (fee-target account fee-base)
        interest-target (money/daily-interest currency interest-base)]
    {:reference-day reference-day :booking-cutoff cutoff
     :source-event-counter (:source-event-counter (input-view events))
     :input-through-event-counter bound
     :fee/base fee-base :fee/target fee-target :fee/difference (- fee-target (total currency fees))
     :fee/records fees
     :interest/base interest-base :interest/target interest-target
     :interest/difference (- interest-target (total currency components))
     :interest/components components}))

(defn eligible-components [components receipts request]
  (let [settled (set (mapcat :component/ids receipts))]
    (filterv #(and (not (contains? settled (:component/id %)))
                   (<= (:reference-day %) (:period/end-day request))
                   (or (= :ordinary (:component/type %))
                       (<= (:booking-day %) (:booking-cutoff request))))
             components)))

(defn report [account account-state account-id]
  (let [components (:interest/components account-state)
        receipts (:settlements account-state)
        pending (vec (vals (:pending-financial-commands account-state)))
        settled (set (mapcat :component/ids receipts))
        currency (:money/currency account)]
    (merge {:account/id account-id :money/currency currency
            :interest/components components :fees (:fees account-state) :settlements receipts
            :financial-intents (vec (:financial-intents account-state))
            :pending-financial-commands pending
            :interest-paid (total currency receipts)
            :pending-interest (total currency (remove #(contains? settled (:component/id %)) components))}
           (update (input-view (vals (:events account-state))) :complete?
                   #(and % (empty? pending))))))
