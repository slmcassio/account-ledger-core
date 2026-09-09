(ns account-ledger.authorization.ports.api-client
  "Outbound delivery of saved envelopes through injected recipient handlers.")

(defn deliver!
  "Send the original event. The caller acknowledges the saved envelope separately."
  [recipients envelope]
  (if-let [receive! (get recipients (:destination envelope))]
    (receive! (:event envelope))
    (throw (ex-info "Unknown delivery recipient"
                    {:reason :unknown-recipient :destination (:destination envelope)}))))
