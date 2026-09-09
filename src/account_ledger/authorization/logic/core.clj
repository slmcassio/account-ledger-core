(ns account-ledger.authorization.logic.core
  "Pure operational transitions. Nothing is sent or mutated here."
  (:require [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.shared.logic.money :as money]))

(defn initial-state [config]
  {:config config :identities {} :delivery-order [] :deliveries {} :acknowledged #{}
   :accounts
   (into {}
         (for [[id account] (:accounts config)
               :let [currency (:money/currency account)
                     opening (:opening-balance account)]]
           [id {:snapshot {:account/id id :money/currency currency
                           :financial-balance opening :held-amount (money/amount currency 0M)
                           :available-balance opening :last-event-counter 0 :holds {}}
                :history []}]))})

(defn- result-for [command outcome]
  {:outcome outcome :transaction/id (:transaction/id command) :account/id (:account/id command)})

(defn- record-approved [state original command changes]
  (let [id (:transaction/id command)
        account-id (:account/id command)
        previous (get-in state [:accounts account-id :snapshot])
        counter (inc (:last-event-counter previous))
        holds (:holds changes)
        held (money/amount (:money/currency command) (reduce + 0M (vals holds)))
        balance (+ (:financial-balance previous) (:financial/effect changes))
        snapshot (assoc previous :holds holds :held-amount held :financial-balance balance
                        :available-balance (- balance held) :last-event-counter counter)
        event (merge {:purpose :principal :occurrences []} command (dissoc changes :holds)
                     {:event-counter counter})
        result (merge (result-for command :recorded)
                      {:occurrences (:occurrences event) :recorded/event event
                       :recorded/snapshot snapshot})
        destinations (if (zero? (:financial/effect event)) [:yield] [:ledger :yield])
        envelopes (mapv (fn [destination]
                          {:delivery/id [id destination] :destination destination :event event})
                        destinations)
        next-state (-> state
                       (assoc-in [:accounts account-id :snapshot] snapshot)
                       (update-in [:accounts account-id :history] conj (assoc result :command original))
                       (assoc-in [:identities id] result)
                       (update :delivery-order into (map :delivery/id envelopes))
                       (update :deliveries into (map (juxt :delivery/id identity) envelopes)))]
    [next-state result]))

(defn- invalid [state command reason]
  [state (assoc (result-for command :invalid) :reason reason)])

(defn- authorization-record [state account-id auth-id]
  (some (fn [record]
          (when (and (= :authorization (get-in record [:command :transaction/type]))
                     (= auth-id (get-in record [:command :authorization/id])))
            record))
        (get-in state [:accounts account-id :history])))

(defn- record-decline [state original command]
  (let [result (assoc (result-for command :declined)
                      :reason :insufficient-available-balance
                      :authorization/state :declined :occurrences [])]
    [(-> state
         (assoc-in [:identities (:transaction/id command)] result)
         (update-in [:accounts (:account/id command) :history] conj (assoc result :command original)))
     result]))

(defn- remaining-hold [holds auth-id remaining]
  (if (pos? remaining) (assoc holds auth-id remaining) (dissoc holds auth-id)))

(defn- submit-authorization [state original command]
  (let [account-id (:account/id command)
        auth-id (:authorization/id command)
        snapshot (get-in state [:accounts account-id :snapshot])
        amount (:money/amount command)]
    (cond
      (authorization-record state account-id auth-id) (invalid state command :authorization-exists)
      (> amount (:available-balance snapshot)) (record-decline state original command)
      :else (record-approved state original command
                             {:financial/effect (money/amount (:money/currency command) 0M)
                              :holds (assoc (:holds snapshot) auth-id amount)
                              :authorization/state :approved :hold/remaining amount}))))

(defn- submit-settlement [state original command]
  (let [account-id (:account/id command)
        auth-id (:authorization/id command)
        holds (get-in state [:accounts account-id :snapshot :holds])
        zero (money/amount (:money/currency command) 0M)
        held (get holds auth-id zero)
        amount (:money/amount command)
        remaining (if (:settlement/final? command) zero (max zero (- held amount)))
        occurrence (when-not (pos? held)
                     {:type (if (authorization-record state account-id auth-id)
                              :inactive-authorization :missing-authorization)
                      :authorization/id auth-id})]
    (record-approved state original command
                     (cond-> {:financial/effect (- amount)
                              :holds (remaining-hold holds auth-id remaining)
                              :hold/remaining remaining
                              :hold/released (if (:settlement/final? command)
                                               (max zero (- held amount)) zero)
                              :occurrences (if occurrence [occurrence] [])}
                       (pos? held) (assoc :authorization/state (if (pos? remaining)
                                                                :partially-settled :settled))))))

(defn- submit-release [state original command]
  (let [account-id (:account/id command)
        auth-id (:authorization/id command)
        holds (get-in state [:accounts account-id :snapshot :holds])
        held (get holds auth-id)
        amount (:money/amount command)]
    (cond
      (nil? (authorization-record state account-id auth-id)) (invalid state command :unknown-authorization)
      (nil? held) (invalid state command :inactive-authorization)
      (> amount held) (invalid state command :insufficient-hold)
      :else (let [remaining (- held amount)]
              (record-approved state original command
                               {:financial/effect (money/amount (:money/currency command) 0M)
                                :holds (remaining-hold holds auth-id remaining)
                                :hold/remaining remaining :hold/released amount
                                :authorization/state (if (pos? remaining) :partially-released :released)})))))

(defn- submit-reversal [state original command]
  (let [reference (:reversal/of command)
        previous (get-in state [:identities reference :recorded/event])
        already-reversed? (some #(and (= :reversal (get-in % [:recorded/event :transaction/type]))
                                     (= reference (get-in % [:recorded/event :reversal/of])))
                                (vals (:identities state)))
        principal? (and (= :principal (:purpose previous))
                        (#{:credit :debit :settlement} (:transaction/type previous)))
        currency (:money/currency command)
        original-amount (when previous (abs (:financial/effect previous)))
        amount-validation (when (contains? command :money/amount)
                            (try
                              (if (= original-amount (money/amount currency (:money/amount command)))
                                nil :reversal-amount-mismatch)
                              (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))
        reason (cond
                 (nil? previous) :unknown-reversal-reference
                 (not= (:account/id command) (:account/id previous)) :reversal-account-mismatch
                 (not principal?) :not-principal-movement
                 already-reversed? :already-reversed
                 amount-validation amount-validation)]
    (if reason (invalid state command reason)
        (record-approved state original (assoc command :money/amount original-amount)
                         {:financial/effect (- (:financial/effect previous))
                          :holds (get-in state [:accounts (:account/id command) :snapshot :holds])}))))

(defn- submit-new [state original command]
  (let [snapshot (get-in state [:accounts (:account/id command) :snapshot])
        amount (:money/amount command)
        type (:transaction/type command)]
    (if (and (contains? command :source-event-counter)
             (not= (:source-event-counter command) (:last-event-counter snapshot)))
      [state (assoc (result-for command :retry-required) :reason :stale-source)]
      (case type
        :authorization (submit-authorization state original command)
        :settlement (submit-settlement state original command)
        :release (submit-release state original command)
        :reversal (submit-reversal state original command)
        (:credit :debit)
        (record-approved state original command
                         (cond-> {:financial/effect (if (= :credit type) amount (- amount))
                                  :holds (:holds snapshot)}
                           (:installment-count command)
                           (assoc :installments (money/allocate-three (:money/currency command) amount))))))))

(defn submit
  "Calculate a command from one immutable latest state and return [state result]."
  [state command]
  (if-not (map? command)
    [state {:outcome :invalid :reason :invalid-command}]
    (if-let [original (get-in state [:identities (:transaction/id command)])]
      [state (assoc (select-keys original [:transaction/id :account/id])
                    :outcome :duplicate :original/result original)]
      (let [validation (contracts/validate-command (:config state) command)]
        (if (:valid? validation)
          (submit-new state command (:command validation))
          (invalid state command (:reason validation)))))))

(defn snapshot [state account-id]
  (or (get-in state [:accounts account-id :snapshot])
      (throw (ex-info "Unknown account" {:reason :unknown-account :account/id account-id}))))

(defn history [state account-id]
  (snapshot state account-id)
  (get-in state [:accounts account-id :history]))

(defn pending-deliveries [state]
  (into [] (keep (:deliveries state)) (:delivery-order state)))

(defn ack-delivery
  "Acknowledge a saved delivery without changing its financial outcome."
  [state delivery-id]
  (cond
    (get-in state [:deliveries delivery-id])
    [(-> state
         (update :deliveries dissoc delivery-id)
         (update :acknowledged conj delivery-id))
     {:delivery/id delivery-id :acknowledged? true}]
    (contains? (:acknowledged state) delivery-id)
    [state {:delivery/id delivery-id :acknowledged? true}]
    :else
    [state {:delivery/id delivery-id :acknowledged? false :reason :unknown-delivery}]))
