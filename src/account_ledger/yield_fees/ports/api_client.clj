(ns account-ledger.yield-fees.ports.api-client
  "Outgoing financial submission and deterministic delivery boundaries.")

(defn submit-financial! [ports command]
  ((:submit-financial! ports) command))

(defn flush-deliveries! [ports]
  ((:flush-deliveries! ports)))
