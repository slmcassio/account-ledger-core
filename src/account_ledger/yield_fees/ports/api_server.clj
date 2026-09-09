(ns account-ledger.yield-fees.ports.api-server
  "Incoming boundary: save local transitions before invoking another module."
  (:require [account-ledger.shared.model.contracts :as schemas]
            [clojure.spec.alpha :as s]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.yield-fees.db.memory :as memory]
            [account-ledger.yield-fees.logic.core :as core]
            [account-ledger.yield-fees.logic.transitions :as transitions]
            [account-ledger.yield-fees.ports.api-client :as client]))

(defn create [config ports]
  (transitions/validate-ports ports)
  (let [configuration (contracts/normalize-config config)]
    (memory/create configuration ports (transitions/initial-state configuration))))

(defn receive! [module event]
  (let [configuration (memory/configuration module)]
    (memory/transact! module #(transitions/receive configuration % event))))

(defn report [module account-id]
  (if-let [account (get-in (memory/configuration module) [:accounts account-id])]
    (core/report account (get-in (memory/read-state module) [:accounts account-id]) account-id)
    {:outcome :invalid :reason :unknown-account :account/id account-id}))

(defn- ready [module account-id]
  (let [delivery (client/flush-deliveries! (memory/ports module))]
    (cond
      (or (pos? (:pending-count delivery)) (seq (:errors delivery)))
      {:outcome :retry-required :reason :incomplete-delivery :delivery delivery}

      (not (:complete? (core/input-view
                       (vals (get-in (memory/read-state module) [:accounts account-id :events])))))
      {:outcome :retry-required :reason :incomplete-input}

      :else nil)))

(defn- submit-command! [module command]
  (let [saved (memory/transact! module #(transitions/save-command % command))]
    (if (:outcome saved)
      saved
      ;; The local write finishes before the call. Exceptions keep the exact command pending.
      (let [result (client/submit-financial! (memory/ports module) saved)]
        (memory/transact! module #(transitions/record-financial-result % saved result))
        result))))

(defn- resume-fee! [module account-id]
  (when-let [command (transitions/pending-command (memory/read-state module) account-id)]
    (when (= :fee (:purpose command))
      (let [result (submit-command! module command)]
        (if (transitions/original-event result) (ready module account-id) result)))))

(defn- assess-fee! [module account request assessment]
  (let [{:keys [component command outcome] :as prepared}
        (transitions/prepare-fee (memory/read-state module) account request assessment)]
    (cond
      outcome prepared
      component (memory/transact! module #(transitions/record-fee % (:account/id request) component))
      command (let [result (submit-command! module command)]
                (if (transitions/original-event result) (ready module (:account/id request)) result)))))

(defn calculate! [module request]
  (let [account-id (:account/id request)
        account (get-in (memory/configuration module) [:accounts account-id])]
    (cond
      (not (s/valid? ::schemas/calculation request)) {:outcome :invalid :reason :invalid-calculation}
      (nil? account) {:outcome :invalid :reason :unknown-account}
      :else
      (or (ready module account-id)
          (resume-fee! module account-id)
          (let [days (transitions/calculation-days request)
                account-state (get-in (memory/read-state module) [:accounts account-id])]
            (if-let [reason (transitions/review-reason account-state request days)]
              {:outcome :invalid :reason reason}
              (loop [[reference & remaining] days, finished []]
                (if (nil? reference)
                  {:outcome :recorded :account/id account-id :assessed-days finished
                   :complete? (:complete? (report module account-id))}
                  (let [current (get-in (memory/read-state module) [:accounts account-id])
                        assessment (core/assessment account current request reference)]
                    (if-let [reason (transitions/calculation-reason current request assessment)]
                      {:outcome :invalid :reason reason :assessed-days finished}
                      (if-let [failure (assess-fee! module account request assessment)]
                        (assoc failure :assessed-days finished :complete? false)
                        (do (memory/transact! module #(transitions/record-interest % account request reference))
                            (recur remaining (conj finished reference))))))))))))))

(defn- pay-command! [module command]
  (let [result (submit-command! module command)]
    (if-let [event (transitions/original-event result)]
      (let [delivery (client/flush-deliveries! (memory/ports module))]
        {:outcome :recorded :settlement/receipt (transitions/receipt-from-event event)
         :financial/result result :delivery delivery
         :complete? (and (zero? (:pending-count delivery)) (empty? (:errors delivery))
                         (:complete? (report module (:account/id command))))})
      result)))

(defn settle! [module request]
  (let [account-id (:account/id request)
        account (get-in (memory/configuration module) [:accounts account-id])
        receipt #(get-in (memory/read-state module) [:settlement-ids (:settlement/id request)])]
    (cond
      (receipt) (transitions/duplicate-receipt (receipt))
      (not (s/valid? ::schemas/settlement request)) {:outcome :invalid :reason :invalid-settlement}
      (nil? account) {:outcome :invalid :reason :unknown-account}
      :else
      (or (ready module account-id)
          (when-let [confirmed (receipt)] (transitions/duplicate-receipt confirmed))
          (if-let [pending (transitions/pending-command (memory/read-state module) account-id)]
            (if (and (= :interest (:purpose pending))
                     (= (:settlement/id pending) (:settlement/id request)))
              (pay-command! module pending)
              (transitions/pending-result pending))
            (let [view (report module account-id)
                  {:keys [components amount]} (transitions/settlement-selection account view request)]
              (if (zero? amount)
                (assoc (memory/transact! module #(transitions/settle-zero % account request)) :complete? true)
                (pay-command! module (transitions/payment-command account view request components amount)))))))))
