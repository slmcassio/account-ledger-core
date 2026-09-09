(ns account-ledger.system
  "Composition, deterministic delivery and reporting for tests and replay."
  (:require [account-ledger.authorization.api :as authorization]
            [account-ledger.ledger.api :as ledger]
            [account-ledger.yield-fees.api :as yield-fees]
            [account-ledger.transaction :as transaction]
            [account-ledger.contracts :as contracts]))

(defn- drain-modules! [authorization ledger yield]
  (let [errors
        (reduce
         (fn [errors {:keys [destination event] :as envelope}]
           (let [id (:delivery/id envelope)]
             (try
               (let [result (case destination
                              :ledger (ledger/post! ledger event)
                              :yield (yield-fees/receive! yield event))]
                 (if (#{:recorded :duplicate} (:outcome result))
                   (do (authorization/ack-delivery! authorization id) errors)
                   (conj errors {:delivery/id id :reason (or (:reason result) :recipient-rejected)})))
               (catch Exception e
                 (conj errors {:delivery/id id :reason :delivery-failed :message (.getMessage e)})))))
         [] (authorization/pending-deliveries authorization))]
    {:pending-count (count (authorization/pending-deliveries authorization)) :errors errors}))

(defn create [config]
  (let [config (contracts/normalize-config config)
        auth (authorization/create config)
        ledger (ledger/create config)
        yield-ready (promise)
        yield (yield-fees/create config {:submit-financial! #(authorization/submit! auth %)
                                         :flush-deliveries! #(drain-modules! auth ledger @yield-ready)})]
    (deliver yield-ready yield)
    {:config config :authorization auth :ledger ledger :yield-fees yield}))

(defn submit! [system command]
  (transaction/submit! (:authorization system) command))

(defn drain! [system]
  (drain-modules! (:authorization system) (:ledger system) (:yield-fees system)))

(defn- after-drain! [system action]
  (let [delivery (drain! system)]
    (if (and (zero? (:pending-count delivery)) (empty? (:errors delivery)))
      (action)
      {:outcome :retry-required :reason :incomplete-delivery :delivery delivery})))

(defn run-day! [system request]
  (after-drain! system #(yield-fees/calculate! (:yield-fees system) request)))

(defn settle! [system request]
  (after-drain! system #(yield-fees/settle! (:yield-fees system) request)))

(defn- authorization-states [history]
  (reduce (fn [states record]
            (let [command (:command record)
                  event (:recorded/event record)
                  id (:authorization/id command)]
              (cond
                (and id (= :declined (:outcome record))) (assoc states id :declined)
                (and id (:authorization/state event)) (assoc states id (:authorization/state event))
                :else states))) {} history))

(defn report
  "A pure current operational report with an explicitly bounded accounting view."
  [system {:keys [day] :as query}]
  (let [id (:account/id query)]
    (when-not (and (contracts/day? day) (get-in system [:config :accounts id]))
      (throw (ex-info "Report requires known account and positive day" {:reason :invalid-report-query})))
    (let [snapshot (authorization/snapshot (:authorization system) id)
          history (authorization/history (:authorization system) id)
          yield (yield-fees/report (:yield-fees system) id)
          pending (authorization/pending-deliveries (:authorization system))
          journal (ledger/journal (:ledger system) id)
          accounting-query (merge {:account/id id :value-through-day day :booking-through-day day}
                                  (select-keys query [:value-through-day :booking-through-day :as-of-journal-position]))]
      (merge snapshot
             (select-keys yield [:interest-paid :pending-interest :interest/components :fees :settlements
                                :source-event-counter])
             {:day day :view :operational-at-capture
              :complete? (and (empty? pending) (:complete? yield))
              :pending-delivery-count (count pending)
              :authorization-states (authorization-states history)
              :occurrences (vec (mapcat #(or (:occurrences %) (get-in % [:recorded/event :occurrences]) []) history))
              :accounting (ledger/balance (:ledger system) accounting-query)
              :journal journal}))))
