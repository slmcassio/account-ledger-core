(ns account-ledger.design-challenge
  (:require [clojure.test :as test :refer [deftest is]]
            [account-ledger.authorization.ports.api-server :as authorization]))

(deftest known-id-does-not-detect-content-collision
  (let [module (authorization/create
                {:accounts {"challenge" {:money/currency :AED :opening-balance 0M :account/type :standard}}
                 :account-types {:standard {:money/currency :AED :daily-fee 25M}}})
        original {:transaction/id "producer-id" :transaction/type :credit :account/id "challenge"
                  :money/currency :AED :money/amount 10M :booking-day 1 :value-day 1 :received-day 1}]
    (authorization/submit! module original)
    ;; Deliberate design challenge: the approved API ignores known IDs BEFORE
    ;; inspecting their payload. Producer identity uniqueness is trusted, so it
    ;; cannot detect a content collision. This assertion must actually fail.
    (is (= :payload-conflict
           (:outcome (authorization/submit! module (assoc original :money/amount 100M)))))))

(defn -main [& _]
  (let [result (test/run-tests 'account-ledger.design-challenge)]
    (println "Deliberate challenge:" (pr-str result))
    (shutdown-agents)
    (System/exit (if (and (pos? (:test result)) (zero? (+ (:fail result) (:error result)))) 0 1))))
