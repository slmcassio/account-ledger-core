(ns account-ledger.system
  "Composition, deterministic delivery and reporting for tests and replay."
  (:require [account-ledger.shared.logic.identifiers :as identifiers]
            [account-ledger.authorization.ports.api-server :as authorization]
            [account-ledger.authorization.ports.api-client :as authorization-client]
            [account-ledger.shared.logic.reporting :as reporting]
            [account-ledger.ledger.ports.api-server :as ledger]
            [account-ledger.yield-fees.ports.api-server :as yield-fees]
            [account-ledger.transaction :as transaction]
            [account-ledger.shared.logic.contracts :as contracts]))

(defn- drain-modules! [authorization ledger yield]
  (let [recipients {:ledger #(ledger/post! ledger %) :yield #(yield-fees/receive! yield %)}
        errors
        (reduce
         (fn [errors envelope]
           (let [id (:delivery/id envelope)]
             (try
               (let [result (authorization-client/deliver! recipients envelope)]
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

(defn report
  "A read-only current operational report with an explicitly bounded accounting view."
  [system {:keys [day] :as query}]
  (let [id (:account/id query)]
    (when-not (and (identifiers/day? day) (get-in system [:config :accounts id]))
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
                                :source-event-counter :financial-intents :pending-financial-commands])
             {:day day :view :operational-at-capture
              :complete? (and (empty? pending) (:complete? yield))
              :pending-delivery-count (count pending)
              :authorization-states (reporting/authorization-states history)
              :occurrences (vec (mapcat #(or (:occurrences %) (get-in % [:recorded/event :occurrences]) []) history))
              :accounting (ledger/balance (:ledger system) accounting-query)
              :journal journal}))))
