(ns account-ledger.yield-fees.logic.transitions
  "Pure local transitions and financial command preparation."
  (:require [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.yield-fees.logic.core :as core]))

(defn validate-ports [ports]
  (when-not (and (ifn? (:submit-financial! ports)) (ifn? (:flush-deliveries! ports)))
    (throw (ex-info "Financial submission and delivery ports are required" {:reason :invalid-ports})))
  ports)

(defn initial-state [configuration]
  {:input-ids {} :settlement-ids {}
   :accounts (into {} (for [id (keys (:accounts configuration))]
                        [id {:events (sorted-map) :fees [] :interest/components [] :settlements []
                             :financial-intents [] :pending-financial-commands {}}]))})

(defn- append-unique [records component]
  (if (some #(= (:component/id component) (:component/id %)) records)
    records (conj records component)))

(defn- append-receipt [state receipt]
  (if-let [original (get-in state [:settlement-ids (:settlement/id receipt)])]
    [state {:outcome :duplicate :original/receipt original}]
    [(-> state
         (assoc-in [:settlement-ids (:settlement/id receipt)] receipt)
         (update-in [:accounts (:account/id receipt) :settlements] conj receipt))
     {:outcome :recorded :settlement/receipt receipt}]))

(defn receipt-from-event [event]
  (assoc (select-keys event [:account/id :settlement/id :transaction/id :component/ids
                            :period/end-day :booking-cutoff])
         :money/amount (:financial/effect event) :run-day (:booking-day event)))

(defn confirm-financial [state event]
  (let [account-id (:account/id event)
        state (case (:purpose event)
                :fee (update-in state [:accounts account-id :fees] append-unique
                                (assoc (:fee/assessment event)
                                       :transaction/id (:transaction/id event)
                                       :source-event-counter (:source-event-counter event)))
                :interest (first (append-receipt state (receipt-from-event event)))
                state)]
    (update-in state [:accounts account-id :pending-financial-commands]
               dissoc (:transaction/id event))))

(defn receive [configuration state event]
  (let [id (:transaction/id event)
        account-id (:account/id event)
        counter (:event-counter event)]
    (cond
      (contains? (:input-ids state) id)
      [state {:outcome :duplicate :transaction/id id}]

      (not (contracts/valid-event? configuration event))
      [state {:outcome :invalid :transaction/id id :reason :invalid-event}]

      (contains? (get-in state [:accounts account-id :events]) counter)
      [state {:outcome :invalid :transaction/id id :reason :counter-conflict}]

      :else
      [(-> state
           (assoc-in [:input-ids id] event)
           (assoc-in [:accounts account-id :events counter] event)
           (confirm-financial event))
       {:outcome :recorded :transaction/id id}])))

(defn- identifier [kind parts] (str "system/" (name kind) "/" (pr-str parts)))

(defn original-event [result]
  (case (:outcome result)
    :recorded (:recorded/event result)
    :duplicate (get-in result [:original/result :recorded/event])
    nil))

(defn pending-command [state account-id]
  (first (vals (get-in state [:accounts account-id :pending-financial-commands]))))

(defn pending-result [command]
  (merge {:outcome :retry-required :reason :pending-financial-command :complete? false}
         (select-keys command [:transaction/id :settlement/id])))

(defn save-command [state command]
  (let [path [:accounts (:account/id command)]
        pending (pending-command state (:account/id command))]
    (if pending
      [state (if (= (:transaction/id pending) (:transaction/id command)) pending (pending-result pending))]
      [(-> state
           (update-in (conj path :financial-intents) conj command)
           (assoc-in (conj path :pending-financial-commands (:transaction/id command)) command))
       command])))

(defn record-financial-result
  "Unknown responses retain the saved command; only a definite rejection releases it."
  [state command result]
  [(if-let [event (original-event result)]
     (confirm-financial state event)
     (if (or (= :invalid (:outcome result))
             (and (= :retry-required (:outcome result)) (= :stale-source (:reason result))))
       (update-in state [:accounts (:account/id command) :pending-financial-commands]
                  dissoc (:transaction/id command))
       state))
   nil])

(defn record-fee [state account-id component]
  [(update-in state [:accounts account-id :fees] append-unique component) nil])

(defn prepare-fee [state account request assessment]
  (let [account-id (:account/id request)
        reference (:reference-day assessment)
        difference (:fee/difference assessment)
        previous (:fee/records assessment)
        interrupted? (and (= :daily (:mode request)) (seq previous)
                          (empty? (:interest/components assessment)) (not (zero? difference)))
        cause (or (:cause/transaction-id request)
                  (when interrupted?
                    (:transaction/id
                     (last (filter #(and (= :principal (:purpose %))
                                         (> (:event-counter %) (:source-event-counter (last previous)))
                                         (<= (:booking-day %) (:booking-cutoff request))
                                         (<= (:value-day %) reference))
                                   (vals (get-in state [:accounts account-id :events])))))))
        cause-event (get-in state [:input-ids cause])
        reversal-refund? (and (= :reversal (:transaction/type cause-event)) (neg? difference))
        original-charge (last (filter #(pos? (:money/amount %)) previous))
        value-day (if reversal-refund? (:value-day original-charge) (:run-day request))
        component {:component/id (identifier :fee-component [account-id reference cause (count previous)])
                   :component/type (if (empty? previous) :ordinary :adjustment)
                   :reference-day reference :booking-day (:run-day request) :value-day value-day
                   :money/amount difference :calculation/base (:fee/base assessment)
                   :calculation/target (:fee/target assessment)
                   :booking-cutoff (:booking-cutoff request)
                   :source-event-counter (:source-event-counter assessment)}
        component (cond-> component cause (assoc :cause/transaction-id cause))]
    (cond
      (:interest-only? request) nil
      (and interrupted? (nil? cause)) {:outcome :invalid :reason :historical-review-required}
      (zero? difference) (when (empty? previous) {:component component})
      :else
      (let [command (merge {:transaction/id (identifier :fee [account-id reference cause (count previous)])
                            :transaction/type (if (pos? difference) :debit :credit)
                            :account/id account-id :money/currency (:money/currency account)
                            :money/amount (abs difference) :purpose :fee
                            :reference-day reference :booking-day (:run-day request) :value-day value-day
                            :received-day (:run-day request) :source-event-counter (:source-event-counter assessment)
                            :component/ids [(:component/id component)] :fee/difference difference
                            :fee/assessment component}
                           (when cause {:cause/transaction-id cause})
                           (when reversal-refund? {:fee/original-value-day value-day}))]
        {:command command}))))

(defn record-interest [state account request reference]
  (let [account-id (:account/id request)
        account-state (get-in state [:accounts account-id])
        assessment (core/assessment account account-state request reference)
        previous (:interest/components assessment)
        difference (:interest/difference assessment)
        cause (:cause/transaction-id request)
        component (cond-> {:component/id (identifier :interest-component [account-id reference cause (count previous)])
                            :component/type (if (empty? previous) :ordinary :adjustment)
                            :reference-day reference :booking-day (:run-day request) :value-day (:run-day request)
                            :money/amount difference :calculation/base (:interest/base assessment)
                            :calculation/target (:interest/target assessment)
                            :booking-cutoff (:booking-cutoff request)
                            :input-through-event-counter (:input-through-event-counter assessment)
                            :source-event-counter (:source-event-counter assessment)}
                    cause (assoc :cause/transaction-id cause))
        boundary (select-keys assessment [:booking-cutoff :input-through-event-counter])]
    [(cond-> (assoc-in state [:accounts account-id :review-boundaries reference] boundary)
       (or (empty? previous) (not (zero? difference)))
       (update-in [:accounts account-id :interest/components] conj component))
     nil]))

(defn review-reason [account-state request days]
  (when (= :historical (:mode request))
    (let [assessed (set (map :reference-day (:interest/components account-state)))
          cause (some #(when (= (:cause/transaction-id request) (:transaction/id %)) %)
                      (vals (:events account-state)))]
      (cond
        (not (every? assessed days)) :unassessed-day
        (not (and cause (<= (:booking-day cause) (:booking-cutoff request))
                  (<= (:value-day cause) (:through-day request)))) :ineligible-cause
        (and (:input-through-event-counter request)
             (or (> (:input-through-event-counter request)
                    (:source-event-counter (core/input-view (vals (:events account-state)))))
                 (< (:input-through-event-counter request) (:event-counter cause)))) :invalid-input-boundary))))

(defn calculation-days [request]
  (if (= :daily (:mode request)) [(:reference-day request)]
      (vec (range (:from-day request) (inc (:through-day request))))))

(defn calculation-reason [account-state request assessment]
  (let [previous-boundary (get-in account-state [:review-boundaries (:reference-day assessment)])]
    (cond
      (or (< (:booking-cutoff assessment) (get previous-boundary :booking-cutoff 0))
          (< (:input-through-event-counter assessment)
             (get previous-boundary :input-through-event-counter 0))) :stale-calculation-view
      (and (= :daily (:mode request))
           (seq (:interest/components assessment))
           (or (not (zero? (:interest/difference assessment)))
               (not (zero? (:fee/difference assessment))))) :historical-review-required)))

(defn settlement-selection [account account-state request]
  (let [components (core/eligible-components (:interest/components account-state)
                                             (:settlements account-state) request)]
    {:components components :amount (core/total (:money/currency account) components)}))

(defn settle-zero [state account request]
  (let [{:keys [components amount]}
        (settlement-selection account (get-in state [:accounts (:account/id request)]) request)]
    (when-not (zero? amount)
      (throw (ex-info "Settlement jobs must run serially" {:reason :concurrent-settlement})))
    (append-receipt state (assoc request :money/amount amount :component/ids (mapv :component/id components)))))

(defn payment-command [account view request components amount]
  (merge (select-keys request [:account/id :settlement/id :period/end-day :booking-cutoff])
         {:transaction/id (identifier :interest-payment [(:account/id request) (:settlement/id request)])
          :transaction/type (if (pos? amount) :credit :debit)
          :money/currency (:money/currency account) :money/amount (abs amount)
          :purpose :interest :reference-day (:period/end-day request)
          :booking-day (:run-day request) :value-day (:run-day request) :received-day (:run-day request)
          :component/ids (mapv :component/id components)
          :source-event-counter (:source-event-counter view)}))

(defn duplicate-receipt [receipt]
  (cond-> {:outcome :duplicate :original/receipt receipt :settlement/receipt receipt}
    (:transaction/id receipt) (assoc :recovered-from-event (:transaction/id receipt))))
