(ns account-ledger.shared.logic.reporting
  "Pure composition of immutable operational history into report fields.")

(defn authorization-states [history]
  (reduce (fn [states record]
            (let [command (:command record)
                  event (:recorded/event record)
                  id (:authorization/id command)]
              (cond
                (and id (= :declined (:outcome record))) (assoc states id :declined)
                (and id (:authorization/state event)) (assoc states id (:authorization/state event))
                :else states))) {} history))
