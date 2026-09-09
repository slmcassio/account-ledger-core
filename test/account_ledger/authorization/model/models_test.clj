(ns account-ledger.authorization.model.models-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is]]
            [account-ledger.authorization.logic.core :as logic]
            [account-ledger.authorization.model.models :as models]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.fixtures :as fixtures]))

(deftest internal-models-describe-every-exercise-transition-and-acknowledgement
  (let [states (reductions (fn [state command] (first (logic/submit state command)))
                           (logic/initial-state (contracts/normalize-config fixtures/config))
                           fixtures/events)
        final-state (last states)
        acknowledged (reduce (fn [state id] (first (logic/ack-delivery state id)))
                              final-state (:delivery-order final-state))]
    (doseq [state states]
      (is (s/valid? ::models/state state) (s/explain-str ::models/state state)))
    (is (s/valid? ::models/state acknowledged))
    (is (= {} (:deliveries acknowledged)))
    (is (= (set (:delivery-order final-state)) (:acknowledged acknowledged)))
    (is (= (:accounts final-state) (:accounts acknowledged)))))

(deftest internal-models-detect-malformed-snapshots-and-saved-envelopes
  (let [[state _] (logic/submit (logic/initial-state (contracts/normalize-config fixtures/config))
                                (first fixtures/events))
        snapshot (get-in state [:accounts "ACC-001" :snapshot])
        delivery (first (logic/pending-deliveries state))]
    (doseq [invalid [(dissoc snapshot :holds)
                     (assoc snapshot :held-amount -1M)
                     (assoc snapshot :holds {"hold" 0M})
                     (assoc snapshot :financial-balance 1.0)
                     (assoc snapshot :last-event-counter -1)]]
      (is (not (s/valid? ::models/snapshot invalid))))
    (doseq [invalid [(dissoc delivery :delivery/id)
                     (assoc delivery :delivery/id ["E1" :unknown])
                     (assoc delivery :delivery/id "E1")]]
      (is (not (s/valid? ::models/delivery invalid))))
    (is (not (s/valid? ::models/delivery (dissoc delivery :event))))
    (is (not (s/valid? ::models/delivery (assoc delivery :destination :unknown))))
    (is (not (s/valid? ::models/state (assoc state :identities {"invalid" {:outcome :retry-required}}))))))

(deftest internal-models-do-not-restrict-unrelated-command-metadata
  (let [configuration (contracts/normalize-config fixtures/config)]
    (doseq [extra [{:delivery/id :external-metadata}]
            :let [command (fixtures/command "external-metadata" :credit 1.00M 1 extra)]]
      (is (= {:valid? true :command command}
             (contracts/validate-command configuration command))
          (pr-str extra)))))
