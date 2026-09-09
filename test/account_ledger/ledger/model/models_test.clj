(ns account-ledger.ledger.model.models-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is]]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.ledger.logic.core :as logic]
            [account-ledger.ledger.model.models :as models]))

(deftest models-describe-recorded-journals-and-immutable-state
  (let [opening (logic/initial-state fixtures/config)
        movement (assoc (fixtures/command "E10" :credit 10.000M 5
                                         {:account/id "ACC-002" :money/currency :BHD
                                          :received-day 6 :installment-count 3})
                        :financial/effect 10.000M :event-counter 1
                        :purpose :principal :occurrences []
                        :installments [3.333M 3.333M 3.334M])
        [recorded result] (logic/record-movement opening movement)
        entry (first (:entries recorded))]
    (is (= :recorded (:outcome result)))
    (is (s/valid? ::models/state opening))
    (is (s/valid? ::models/state recorded))
    (is (s/valid? ::models/journal-entry entry))
    (doseq [posting (:postings entry)]
      (is (s/valid? ::models/posting posting)))
    (doseq [entry [(dissoc entry :journal/position)
                   (assoc entry :journal/position 0)
                   (assoc entry :postings [])
                   (update-in entry [:postings 0] dissoc :book/account)
                   (assoc-in entry [:postings 0 :book/account] "")
                   (assoc-in entry [:postings 0 :side] :incoming)
                   (assoc-in entry [:postings 0 :money/amount] 3.333)
                   (assoc-in entry [:postings 0 :money/currency] :USD)
                   (assoc-in entry [:postings 0 :installment/position] 0)]]
      (is (not (s/valid? ::models/journal-entry entry)) (pr-str entry)))
    (doseq [state [(dissoc recorded :recorded-ids)
                   (assoc recorded :entries {})
                   (assoc recorded :recorded-ids #{""})
                   (assoc recorded :config nil)]]
      (is (not (s/valid? ::models/state state)) (pr-str state)))))

(deftest internal-models-do-not-restrict-unrelated-command-metadata
  (let [configuration (contracts/normalize-config fixtures/config)]
    (doseq [extra [{:book/account 17} {:installment/position 0} {:journal/position 0}]
            :let [command (fixtures/command "external-metadata" :credit 1.00M 1 extra)]]
      (is (= {:valid? true :command command}
             (contracts/validate-command configuration command))
          (pr-str extra)))))
