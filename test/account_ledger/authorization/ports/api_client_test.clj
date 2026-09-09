(ns account-ledger.authorization.ports.api-client-test
  (:require [clojure.test :refer [deftest is]]
            [account-ledger.authorization.ports.api-client :as client]
            [account-ledger.authorization.ports.api-server :as authorization]
            [account-ledger.fixtures :as fixtures]))

(deftest delivery-uses-the-original-saved-event-without-acknowledging-it
  (let [module (authorization/create fixtures/config)
        recorded (authorization/submit! module (first fixtures/events))
        envelopes (authorization/pending-deliveries module)
        observed (atom [])
        recipients {:ledger (fn [event]
                              (swap! observed conj [:ledger event])
                              {:outcome :recorded})
                    :yield (fn [event]
                             (swap! observed conj [:yield event])
                             {:outcome :duplicate})}]
    (is (= [{:outcome :recorded} {:outcome :duplicate}]
           (mapv #(client/deliver! recipients %) envelopes)))
    (is (= [[:ledger (:recorded/event recorded)] [:yield (:recorded/event recorded)]]
           @observed))
    (is (every? #(identical? (:recorded/event recorded) (second %)) @observed))
    (is (= envelopes (authorization/pending-deliveries module)))))

(deftest failed-delivery-keeps-the-original-envelope-available-for-retry
  (let [module (authorization/create fixtures/config)
        _ (authorization/submit! module (first fixtures/events))
        before (authorization/pending-deliveries module)
        envelope (first before)
        history (authorization/history module "ACC-001")]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Recipient unavailable"
                          (client/deliver! {:ledger (fn [_]
                                                       (throw (ex-info "Recipient unavailable" {})))}
                                           envelope)))
    (is (= before (authorization/pending-deliveries module)))
    (is (= history (authorization/history module "ACC-001")))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Unknown delivery recipient"
                          (client/deliver! {} envelope)))
    (is (= before (authorization/pending-deliveries module)))))
