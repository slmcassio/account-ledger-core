(ns account-ledger.ledger.logic.core
  "Pure posting transitions and accounting queries over immutable Ledger state."
  (:require [account-ledger.shared.model.contracts :as schemas]
            [clojure.spec.alpha :as s]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.shared.logic.money :as money]))

(defn initial-state [config]
  {:config (contracts/normalize-config config) :entries [] :recorded-ids #{}})

(defn- invalid! [reason]
  (throw (ex-info "Invalid committed Ledger movement" {:reason reason})))

(defn- normalize-movement [config movement]
  (when-not (contracts/valid-event? config movement) (invalid! :invalid-event))
  (when-not (#{:credit :debit :settlement :reversal} (:transaction/type movement))
    (invalid! :nonfinancial-event))
  (let [submitted-fields (apply dissoc movement schemas/computed-event-fields)
        {:keys [valid? command reason]} (contracts/validate-command config submitted-fields)]
    (when-not valid? (invalid! reason))
    (let [command (merge movement command)
          currency (:money/currency command)
          effect (money/amount currency (:financial/effect command))
          type (:transaction/type command)]
      (when (or (zero? effect)
                (and (= :credit type) (neg? effect))
                (and (#{:debit :settlement} type) (pos? effect)))
        (invalid! :invalid-financial-effect))
      (when (or (not= :reversal type) (contains? command :money/amount))
        (when-not (= (abs effect) (money/amount currency (:money/amount command)))
          (invalid! :amount-effect-mismatch)))
      (assoc command :financial/effect effect))))

(defn- posting-pair [movement amount installment-position]
  (let [currency (:money/currency movement)
        incoming? (pos? (:financial/effect movement))
        posting (cond-> {:money/currency currency :money/amount amount}
                  installment-position (assoc :installment/position installment-position))]
    [(assoc posting :book/account (str "clearing/" (name currency))
                    :side (if incoming? :debit :credit))
     (assoc posting :book/account (str "customer/" (:account/id movement))
                    :side (if incoming? :credit :debit))]))

(defn- validate-postings! [postings currency]
  (let [total (fn [side]
                (reduce + 0M (map :money/amount (filter #(= side (:side %)) postings))))]
    (when-not (and (seq postings)
                   (every? #(and (= currency (:money/currency %))
                                 (#{:credit :debit} (:side %))
                                 (pos? (:money/amount %))) postings)
                   (= (total :debit) (total :credit)))
      (throw (ex-info "Journal invariant violated" {:reason :unbalanced-journal :invariant? true})))))

(defn journal-entry
  "Build one complete journal; customer liabilities increase through credits."
  [config movement position]
  (when-not (pos-int? position) (invalid! :invalid-journal-position))
  (let [movement (normalize-movement config movement)
        currency (:money/currency movement)
        amount (abs (:financial/effect movement))
        installments? (or (contains? movement :installments) (contains? movement :installment-count))
        amounts (if installments?
                  (do
                    (when-not (and (= :credit (:transaction/type movement))
                                   (= 3 (:installment-count movement))
                                   (vector? (:installments movement))
                                   (= 3 (count (:installments movement))))
                      (invalid! :invalid-installments))
                    (let [parts (mapv #(money/amount currency %) (:installments movement))]
                      (when-not (and (every? pos? parts) (= amount (reduce + parts)))
                        (invalid! :invalid-installments))
                      parts))
                  [amount])
        postings (vec (mapcat (fn [index part]
                               (posting-pair movement part (when installments? (inc index))))
                             (range) amounts))]
    (validate-postings! postings currency)
    (cond-> (assoc movement :journal/position position :postings postings)
      installments? (assoc :installments amounts))))

(defn record-movement
  "Return [new-state result]; identity and the whole entry are recorded together."
  [state movement]
  (let [id (:transaction/id movement)]
    (if (contains? (:recorded-ids state) id)
      [state {:outcome :duplicate :transaction/id id}]
      (try
        (let [entry (journal-entry (:config state) movement (inc (count (:entries state))))]
          [(-> state (update :entries conj entry) (update :recorded-ids conj id))
           {:outcome :recorded :transaction/id id}])
        (catch clojure.lang.ExceptionInfo e
          (if (:invariant? (ex-data e)) (throw e)
              [state {:outcome :invalid :transaction/id id :reason (:reason (ex-data e))}]))))))

(defn account-journal [state account-id]
  (when-not (get-in state [:config :accounts account-id])
    (throw (ex-info "Unknown account" {:reason :unknown-account :account/id account-id})))
  (filterv #(= account-id (:account/id %)) (:entries state)))

(defn account-balance
  "Read economic date, booking cutoff and observed local append position together."
  [state query]
  (when-not (s/valid? ::schemas/ledger-query query)
    (throw (ex-info "Invalid Ledger query" {:reason :invalid-query})))
  (let [account-id (:account/id query)
        entries (account-journal state account-id)
        {:keys [opening-balance] :money/keys [currency]}
        (get-in state [:config :accounts account-id])
        query (assoc query :as-of-journal-position
                     (get query :as-of-journal-position (count (:entries state))))
        selected (filter #(and (<= (:value-day %) (:value-through-day query))
                               (<= (:booking-day %) (:booking-through-day query))
                               (<= (:journal/position %) (:as-of-journal-position query))) entries)
        customer-book (str "customer/" account-id)
        postings (filter #(= customer-book (:book/account %)) (mapcat :postings selected))]
    (when (> (:as-of-journal-position query) (count (:entries state)))
      (throw (ex-info "Journal position has not been recorded" {:reason :future-journal-position})))
    {:account/id account-id :money/currency currency :query query
     :money/amount (money/amount currency
                                 (reduce (fn [total posting]
                                           ((if (= :credit (:side posting)) + -)
                                            total (:money/amount posting)))
                                         opening-balance postings))}))
