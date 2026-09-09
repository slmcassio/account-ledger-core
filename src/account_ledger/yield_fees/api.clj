(ns account-ledger.yield-fees.api
  "Public application boundary for event ingestion, calculation and settlement."
  (:require [clojure.spec.alpha :as s]
            [account-ledger.contracts :as contracts]
            [account-ledger.money :as money]
            [account-ledger.yield-fees.domain :as domain]
            [account-ledger.yield-fees.memory :as memory]))

(defn create [config ports]
  (when-not (and (ifn? (:submit-financial! ports)) (ifn? (:flush-deliveries! ports)))
    (throw (ex-info "Financial submission and delivery ports are required" {:reason :invalid-ports})))
  (memory/create (contracts/normalize-config config) ports))

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

(defn- receipt-from-event [event]
  (assoc (select-keys event [:account/id :settlement/id :transaction/id :component/ids
                            :period/end-day :booking-cutoff])
         :money/amount (:financial/effect event) :run-day (:booking-day event)))

(defn- confirm-financial [state event]
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

(defn receive! [module event]
  (memory/transact!
   module
   (fn [state]
     (let [id (:transaction/id event)
           account-id (:account/id event)
           counter (:event-counter event)]
       (cond
         (contains? (:input-ids state) id)
         [state {:outcome :duplicate :transaction/id id}]

         (not (contracts/valid-event? (memory/configuration module) event))
         [state {:outcome :invalid :transaction/id id :reason :invalid-event}]

         (contains? (get-in state [:accounts account-id :events]) counter)
         [state {:outcome :invalid :transaction/id id :reason :counter-conflict}]

         :else
         [(-> state
              (assoc-in [:input-ids id] event)
              (assoc-in [:accounts account-id :events counter] event)
              (confirm-financial event))
          {:outcome :recorded :transaction/id id}])))))

(defn report [module account-id]
  (if-let [account (get-in (memory/configuration module) [:accounts account-id])]
    (domain/report account (get-in (memory/read-state module) [:accounts account-id]) account-id)
    {:outcome :invalid :reason :unknown-account :account/id account-id}))

(defn- ready [module account-id]
  (let [delivery ((:flush-deliveries! (memory/ports module)))]
    (cond
      (or (pos? (:pending-count delivery)) (seq (:errors delivery)))
      {:outcome :retry-required :reason :incomplete-delivery :delivery delivery}

      (not (:complete? (domain/input-view
                         (vals (get-in (memory/read-state module) [:accounts account-id :events])))))
      {:outcome :retry-required :reason :incomplete-input}

      :else nil)))

(defn- identifier [kind parts] (str "system/" (name kind) "/" (pr-str parts)))

(defn- original-event [result]
  (case (:outcome result)
    :recorded (:recorded/event result)
    :duplicate (get-in result [:original/result :recorded/event])
    nil))

(defn- pending-command [module account-id]
  (first (vals (get-in (memory/read-state module) [:accounts account-id :pending-financial-commands]))))

(defn- pending-result [command]
  (merge {:outcome :retry-required :reason :pending-financial-command :complete? false}
         (select-keys command [:transaction/id :settlement/id])))

(defn- save-command! [module command]
  (memory/transact!
   module
   (fn [state]
     (let [path [:accounts (:account/id command)]
           pending (first (vals (:pending-financial-commands (get-in state path))))]
       (if pending
         [state (if (= (:transaction/id pending) (:transaction/id command)) pending (pending-result pending))]
         [(-> state
              (update-in (conj path :financial-intents) conj command)
              (assoc-in (conj path :pending-financial-commands (:transaction/id command)) command))
          command])))))

(defn- submit-command! [module command]
  (let [saved (save-command! module command)]
    (if (:outcome saved)
      saved
      ;; The exact command is already recorded locally. Exceptions leave it pending.
      (let [result ((:submit-financial! (memory/ports module)) saved)]
        (if-let [event (original-event result)]
          (memory/transact! module (fn [state] [(confirm-financial state event) nil]))
          (when (or (= :invalid (:outcome result))
                    (and (= :retry-required (:outcome result)) (= :stale-source (:reason result))))
            (memory/transact! module
                              (fn [state]
                                [(update-in state [:accounts (:account/id saved) :pending-financial-commands]
                                            dissoc (:transaction/id saved)) nil]))))
        result))))

(defn- resume-fee! [module account-id]
  (when-let [command (pending-command module account-id)]
    (when (= :fee (:purpose command))
      (let [result (submit-command! module command)]
        (if (original-event result) (ready module account-id) result)))))

(defn- record-fee! [module account-id component]
  (memory/transact! module
                    (fn [state]
                      [(update-in state [:accounts account-id :fees] append-unique component) nil])))

(defn- assess-fee! [module account request assessment]
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
                                   (vals (get-in (memory/read-state module) [:accounts account-id :events])))))))
        cause-event (get-in (memory/read-state module) [:input-ids cause])
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
      (zero? difference) (when (empty? previous) (record-fee! module account-id component))
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
                           (when reversal-refund? {:fee/original-value-day value-day}))
            result (submit-command! module command)]
        (if (original-event result) (ready module account-id) result)))))

(defn- record-interest! [module account request reference]
  (memory/transact!
   module
   (fn [state]
     (let [account-id (:account/id request)
           account-state (get-in state [:accounts account-id])
           assessment (domain/assessment account account-state request reference)
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
        nil]))))

(defn- review-reason [account-state request days]
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
                    (:source-event-counter (domain/input-view (vals (:events account-state)))))
                 (< (:input-through-event-counter request) (:event-counter cause)))) :invalid-input-boundary))))

(defn calculate! [module request]
  (let [account-id (:account/id request)
        account (get-in (memory/configuration module) [:accounts account-id])]
    (cond
      (not (s/valid? ::contracts/calculation request)) {:outcome :invalid :reason :invalid-calculation}
      (nil? account) {:outcome :invalid :reason :unknown-account}
      :else
      (or (ready module account-id)
          (resume-fee! module account-id)
          (let [days (if (= :daily (:mode request)) [(:reference-day request)]
                         (vec (range (:from-day request) (inc (:through-day request)))))
                account-state (get-in (memory/read-state module) [:accounts account-id])]
            (if-let [reason (review-reason account-state request days)]
              {:outcome :invalid :reason reason}
              (loop [[reference & remaining] days, finished []]
                (if (nil? reference)
                  {:outcome :recorded :account/id account-id :assessed-days finished
                   :complete? (:complete? (report module account-id))}
                  (let [current (get-in (memory/read-state module) [:accounts account-id])
                        assessment (domain/assessment account current request reference)
                        previous-boundary (get-in current [:review-boundaries reference])
                        stale? (or (< (:booking-cutoff assessment) (get previous-boundary :booking-cutoff 0))
                                   (< (:input-through-event-counter assessment)
                                      (get previous-boundary :input-through-event-counter 0)))
                        daily-change? (and (= :daily (:mode request))
                                           (seq (:interest/components assessment))
                                           (or (not (zero? (:interest/difference assessment)))
                                               (not (zero? (:fee/difference assessment)))))]
                    (cond
                      stale? {:outcome :invalid :reason :stale-calculation-view :assessed-days finished}
                      daily-change? {:outcome :invalid :reason :historical-review-required :assessed-days finished}
                      :else
                      (if-let [failure (assess-fee! module account request assessment)]
                        (assoc failure :assessed-days finished :complete? false)
                        (do (record-interest! module account request reference)
                            (recur remaining (conj finished reference))))))))))))))

(defn- settle-zero! [module account request]
  ;; Selection and the zero receipt are one local transition, without any port call.
  (memory/transact!
   module
   (fn [state]
     (let [view (get-in state [:accounts (:account/id request)])
           components (domain/eligible-components (:interest/components view) (:settlements view) request)
           amount (domain/total (:money/currency account) components)]
       (when-not (zero? amount)
         (throw (ex-info "Settlement jobs must run serially" {:reason :concurrent-settlement})))
       (append-receipt state (assoc request :money/amount amount :component/ids (mapv :component/id components)))))))

(defn- duplicate-receipt [receipt]
  (cond-> {:outcome :duplicate :original/receipt receipt :settlement/receipt receipt}
    (:transaction/id receipt) (assoc :recovered-from-event (:transaction/id receipt))))

(defn- pay-command! [module command]
  (let [result (submit-command! module command)]
    (if-let [event (original-event result)]
      (let [delivery ((:flush-deliveries! (memory/ports module)))]
        {:outcome :recorded :settlement/receipt (receipt-from-event event)
         :financial/result result :delivery delivery
         :complete? (and (zero? (:pending-count delivery)) (empty? (:errors delivery))
                         (:complete? (report module (:account/id command))))})
      result)))

(defn settle! [module request]
  (let [account-id (:account/id request)
        account (get-in (memory/configuration module) [:accounts account-id])
        receipt #(get-in (memory/read-state module) [:settlement-ids (:settlement/id request)])]
    (cond
      (receipt) (duplicate-receipt (receipt))
      (not (s/valid? ::contracts/settlement request)) {:outcome :invalid :reason :invalid-settlement}
      (nil? account) {:outcome :invalid :reason :unknown-account}
      :else
      (or (ready module account-id)
          (when-let [confirmed (receipt)] (duplicate-receipt confirmed))
          (if-let [pending (pending-command module account-id)]
            (if (and (= :interest (:purpose pending))
                     (= (:settlement/id pending) (:settlement/id request)))
              (pay-command! module pending)
              (pending-result pending))
            (let [view (report module account-id)
                  components (domain/eligible-components (:interest/components view) (:settlements view) request)
                  amount (domain/total (:money/currency account) components)]
              (if (zero? amount)
                (assoc (settle-zero! module account request) :complete? true)
                (pay-command!
                 module
                 (merge (select-keys request [:account/id :settlement/id :period/end-day :booking-cutoff])
                        {:transaction/id (identifier :interest-payment [account-id (:settlement/id request)])
                         :transaction/type (if (pos? amount) :credit :debit)
                         :money/currency (:money/currency account) :money/amount (abs amount)
                         :purpose :interest :reference-day (:period/end-day request)
                         :booking-day (:run-day request) :value-day (:run-day request) :received-day (:run-day request)
                         :component/ids (mapv :component/id components)
                         :source-event-counter (:source-event-counter view)})))))))))
