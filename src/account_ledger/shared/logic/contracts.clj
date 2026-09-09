(ns account-ledger.shared.logic.contracts
  "Pure configuration, command and event validation. Check recorded identity first."
  (:require [clojure.spec.alpha :as s]
            [account-ledger.shared.logic.identifiers :as identifiers]
            [account-ledger.shared.logic.money :as money]
            [account-ledger.shared.model.contracts :as schemas]))

(defn normalize-config [config]
  (when-not (and (map? (:accounts config)) (seq (:accounts config))
                 (map? (:account-types config)))
    (throw (ex-info "Accounts and account types are required" {:reason :invalid-config})))
  (update config :accounts
          (fn [accounts]
            (into {}
                  (for [[id account] accounts]
                    (let [currency (:money/currency account)
                          type-config (get-in config [:account-types (:account/type account)])]
                      (when-not (and (identifiers/id? id) (= currency (:money/currency type-config)))
                        (throw (ex-info "Invalid account or account type" {:reason :invalid-config})))
                      (let [fee (money/amount currency (:daily-fee type-config))]
                        (when (neg? fee)
                          (throw (ex-info "Negative fee configuration" {:reason :invalid-config})))
                        [id (assoc account :opening-balance (money/amount currency (:opening-balance account))
                                           :daily-fee fee)])))))))

(defn- valid-fee-assessment? [command]
  (let [assessment (:fee/assessment command)
        currency (:money/currency command)]
    (and (s/valid? ::schemas/fee-assessment assessment)
         (= [(:component/id assessment)] (:component/ids command))
         (= (select-keys assessment [:reference-day :booking-day :value-day :cause/transaction-id])
            (select-keys command [:reference-day :booking-day :value-day :cause/transaction-id]))
         (or (not (contains? assessment :source-event-counter))
             (= (:source-event-counter command) (:source-event-counter assessment)))
         (or (not (contains? assessment :booking-cutoff))
             (= (dec (:booking-day command)) (:booking-cutoff assessment)))
         (try
           (= (if (= :debit (:transaction/type command))
                (:money/amount command) (- (:money/amount command)))
              (money/amount currency (:money/amount assessment))
              (money/amount currency (:fee/difference command)))
           (catch clojure.lang.ExceptionInfo _ false)))))

(defn- valid-interest-settlement? [command]
  (and (s/valid? ::schemas/settlement (assoc command :run-day (:booking-day command)))
       (= (:reference-day command) (:period/end-day command))
       (= (:value-day command) (:booking-day command))))

(defn validate-command
  "Return normalized input or an invalid reason. Does not inspect stored identities."
  [config command]
  (let [type (:transaction/type command)
        account (get-in config [:accounts (:account/id command)])
        financial? (contains? command :source-event-counter)
        reason (cond
                 (not (s/valid? ::schemas/command (dissoc command :money/amount))) :invalid-command
                 (some #(contains? command %) schemas/computed-event-fields) :unexpected-event-fields
                 (nil? account) :unknown-account
                 (not= (:money/currency account) (:money/currency command)) :currency-mismatch
                 (> (:booking-day command) (:received-day command)) :invalid-dates
                 (and (identifiers/system-id? (:transaction/id command)) (not financial?)) :reserved-id
                 (and (not financial?) (contains? command :purpose)
                      (not= :principal (:purpose command))) :invalid-financial-command
                 (and financial? (not (and (identifiers/system-id? (:transaction/id command))
                                                         (identifiers/counter? (:source-event-counter command))
                                                         (#{:credit :debit} type)
                                                         (#{:fee :interest} (:purpose command))
                                                         (identifiers/day? (:reference-day command))
                                                         (vector? (:component/ids command))
                                                         (seq (:component/ids command))
                                                         (every? identifiers/id? (:component/ids command))))) :invalid-financial-command
                 (and (#{:authorization :settlement :release} type)
                      (not (identifiers/id? (:authorization/id command)))) :invalid-reference
                 (and (= :settlement type) (not (boolean? (:settlement/final? command)))) :invalid-settlement
                 (and (= :reversal type) (not (identifiers/id? (:reversal/of command)))) :invalid-reference
                 (and (contains? command :installment-count)
                      (not (and (= :credit type) (= 3 (:installment-count command))))) :invalid-installments)]
    (if reason {:valid? false :reason reason}
        (try
          (let [normalized (if (= :reversal type) command
                               (update command :money/amount #(money/amount (:money/currency command) %)))]
            (when (:installment-count normalized)
              (money/allocate-three (:money/currency normalized) (:money/amount normalized)))
            (cond
              (and (not= :reversal type) (not (pos? (:money/amount normalized))))
              {:valid? false :reason :invalid-amount}

              (and (= :fee (:purpose normalized)) (not (valid-fee-assessment? normalized)))
              {:valid? false :reason :invalid-fee-assessment}

              (and (= :interest (:purpose normalized)) (not (valid-interest-settlement? normalized)))
              {:valid? false :reason :invalid-interest-settlement}

              :else {:valid? true :command normalized}))
          (catch clojure.lang.ExceptionInfo e {:valid? false :reason (:reason (ex-data e))})))))

(defn valid-event? [config event]
  (boolean
   (and (s/valid? ::schemas/event event)
        (:valid? (validate-command config (apply dissoc event schemas/computed-event-fields)))
        (try
          (let [currency (:money/currency event)
                effect (money/amount currency (:financial/effect event))
                type (:transaction/type event)]
            (if (#{:authorization :release} type)
              (zero? effect)
              (and (not (zero? effect))
                   (case type :credit (pos? effect) :debit (neg? effect)
                         :settlement (neg? effect) :reversal true false)
                   (or (and (= :reversal type) (not (contains? event :money/amount)))
                       (= (abs effect) (money/amount currency (:money/amount event)))))))
          (catch clojure.lang.ExceptionInfo _ false)))))
