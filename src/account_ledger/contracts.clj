(ns account-ledger.contracts
  "Shared boundary schemas. Check recorded identity before command validation."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [account-ledger.money :as money]))

(defn id? [x] (and (string? x) (not (str/blank? x))))
(defn day? [x] (and (integer? x) (pos? x)))
(defn counter? [x] (and (integer? x) (not (neg? x))))
(defn system-id? [x] (and (id? x) (str/starts-with? x "system/")))
(def computed-event-fields
  [:installments :financial/effect :event-counter :authorization/state
   :hold/remaining :hold/released :occurrences])
(s/def :transaction/id id?)
(s/def :transaction/type #{:credit :debit :authorization :settlement :release :reversal})
(s/def :account/id id?)
(s/def :money/currency #{:AED :BHD})
(s/def :money/amount #(instance? BigDecimal %))
(s/def ::booking-day day?)
(s/def ::value-day day?)
(s/def ::received-day day?)
(s/def ::event-counter pos-int?)
(s/def ::source-event-counter counter?)
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
(s/def ::last-event-counter counter?)
(s/def ::snapshot
  (s/keys :req [:account/id] :req-un [::financial-balance ::held-amount
                                    ::available-balance ::last-event-counter]))
(s/def ::outcome #{:recorded :declined :duplicate :retry-required :invalid})
(s/def ::result (s/keys :req-un [::outcome] :opt [:transaction/id]))
(s/def ::run-day day?)
(s/def ::reference-day day?)
(s/def ::booking-cutoff day?)
(s/def ::mode #{:daily :historical})
(s/def ::from-day day?)
(s/def ::through-day day?)
(s/def :cause/transaction-id id?)
(s/def ::calculation
  (s/and (s/keys :req [:account/id]
                 :req-un [::run-day ::booking-cutoff ::mode])
         #(= (:booking-cutoff %) (dec (:run-day %)))
         #(case (:mode %)
            :daily (= (:reference-day %) (dec (:run-day %)))
            :historical (and (day? (:from-day %)) (day? (:through-day %))
                             (<= (:from-day %) (:through-day %) (:booking-cutoff %))
                             (id? (:cause/transaction-id %))))
         #(or (not (contains? % :input-through-event-counter))
              (and (= :historical (:mode %)) (true? (:interest-only? %))
                   (counter? (:input-through-event-counter %))))
         #(or (not (contains? % :interest-only?))
              (and (true? (:interest-only? %)) (= :historical (:mode %))
                   (contains? % :input-through-event-counter)))))
(s/def :settlement/id id?)
(s/def :period/end-day day?)
(s/def ::settlement
  (s/and (s/keys :req [:account/id :settlement/id :period/end-day]
                 :req-un [::run-day ::booking-cutoff])
         #(= (:booking-cutoff %) (dec (:run-day %)))
         #(<= (:period/end-day %) (:booking-cutoff %))))
(s/def ::value-through-day day?)
(s/def ::booking-through-day day?)
(s/def ::as-of-journal-position counter?)
(s/def ::ledger-query
  (s/keys :req [:account/id] :req-un [::value-through-day ::booking-through-day]
          :opt-un [::as-of-journal-position]))

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
                      (when-not (and (id? id) (= currency (:money/currency type-config)))
                        (throw (ex-info "Invalid account or account type" {:reason :invalid-config})))
                      (let [fee (money/amount currency (:daily-fee type-config))]
                        (when (neg? fee)
                          (throw (ex-info "Negative fee configuration" {:reason :invalid-config})))
                        [id (assoc account :opening-balance (money/amount currency (:opening-balance account))
                                           :daily-fee fee)])))))))

(defn validate-command
  "Return normalized input or an invalid reason. Does not inspect stored identities."
  [config command]
  (let [type (:transaction/type command)
        account (get-in config [:accounts (:account/id command)])
        financial? (contains? command :source-event-counter)
        reason (cond
                 (not (s/valid? ::command (dissoc command :money/amount))) :invalid-command
                 (some #(contains? command %) computed-event-fields) :unexpected-event-fields
                 (nil? account) :unknown-account
                 (not= (:money/currency account) (:money/currency command)) :currency-mismatch
                 (> (:booking-day command) (:received-day command)) :invalid-dates
                 (and (system-id? (:transaction/id command)) (not financial?)) :reserved-id
                 (and (not financial?) (contains? command :purpose)
                      (not= :principal (:purpose command))) :invalid-financial-command
                 (and financial? (not (and (system-id? (:transaction/id command))
                                                         (counter? (:source-event-counter command))
                                                         (#{:credit :debit} type)
                                                         (#{:fee :interest} (:purpose command))
                                                         (day? (:reference-day command))
                                                         (vector? (:component/ids command))
                                                         (seq (:component/ids command))
                                                         (every? id? (:component/ids command))))) :invalid-financial-command
                 (and (#{:authorization :settlement :release} type)
                      (not (id? (:authorization/id command)))) :invalid-reference
                 (and (= :settlement type) (not (boolean? (:settlement/final? command)))) :invalid-settlement
                 (and (= :reversal type) (not (id? (:reversal/of command)))) :invalid-reference
                 (and (contains? command :installment-count)
                      (not (and (= :credit type) (= 3 (:installment-count command))))) :invalid-installments)]
    (if reason {:valid? false :reason reason}
        (try
          (let [normalized (if (= :reversal type) command
                               (update command :money/amount #(money/amount (:money/currency command) %)))]
            (when (:installment-count normalized)
              (money/allocate-three (:money/currency normalized) (:money/amount normalized)))
            (if (and (not= :reversal type) (not (pos? (:money/amount normalized))))
              {:valid? false :reason :invalid-amount}
              {:valid? true :command normalized}))
          (catch clojure.lang.ExceptionInfo e {:valid? false :reason (:reason (ex-data e))})))))

(defn valid-event? [config event]
  (boolean
   (and (s/valid? ::event event)
        (:valid? (validate-command config (apply dissoc event computed-event-fields)))
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
