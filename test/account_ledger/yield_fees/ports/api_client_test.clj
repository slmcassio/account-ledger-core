(ns account-ledger.yield-fees.ports.api-client-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.authorization.ports.api-server :as authorization]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.yield-fees.ports.api-server-test :refer [daily historical real-fixture settlement]]
            [account-ledger.ledger.ports.api-server :as ledger]
            [account-ledger.yield-fees.ports.api-server :as yield]))

(defn- interest-journal [book]
  (filterv #(= :interest (:purpose %)) (ledger/journal book "ACC-001")))

(deftest an-unknown-payment-also-blocks-a-new-zero-settlement
  (let [fail? (atom true)
        {:keys [module submit! authorization]}
        (real-fixture (fn [auth command]
                        (if (compare-and-set! fail? true false)
                          (throw (ex-info "Controlled failure before recording" {}))
                          (authorization/submit! auth command))))]
    (submit! (fixtures/command "principal" :credit 100.00M 1))
    (yield/calculate! module (daily 3))
    (is (thrown? clojure.lang.ExceptionInfo (yield/settle! module (settlement "original" 4 3))))
    (let [waiting (yield/report module "ACC-001")
          result (yield/settle! module (settlement "empty-period" 4 1))]
      (is (= :retry-required (:outcome result)))
      (is (= :pending-financial-command (:reason result)))
      (is (= waiting (yield/report module "ACC-001")))
      (is (empty? (:settlements waiting)))
      (is (= 100.00M (:financial-balance (authorization/snapshot authorization "ACC-001")))))
    (is (= :recorded (:outcome (yield/settle! module (settlement "original" 4 3)))))
    (is (= 100.04M (:financial-balance (authorization/snapshot authorization "ACC-001"))))))

(deftest a-reversal-fee-refund-is-saved-before-its-response-can-be-lost
  (let [module-ref (atom nil)
        observed (atom nil)
        lose? (atom true)
        {:keys [module submit! authorization ledger flush submissions]}
        (real-fixture
         (fn [auth command]
           (if (and (= :fee (:purpose command)) (= :credit (:transaction/type command))
                    (compare-and-set! lose? true false))
             (do (reset! observed (yield/report @module-ref "ACC-001"))
                 (authorization/submit! auth command)
                 (throw (ex-info "Controlled lost refund response" {})))
             (authorization/submit! auth command))))]
    (reset! module-ref module)
    (submit! (fixtures/command "principal" :debit 1.00M 1))
    (yield/calculate! module (daily 2))
    (submit! (fixtures/command "reversal" :reversal 1.00M 3 {:value-day 1 :reversal/of "principal"}))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled lost refund"
                          (yield/calculate! module (historical 4 1 1 "reversal"))))
    (let [refund (last @submissions)]
      (is (= refund (last (:financial-intents @observed))))
      (is (= [refund] (:pending-financial-commands @observed)))
      (is (= {:booking-day 4 :value-day 2 :money/amount 25.00M :fee/difference -25.00M}
             (select-keys refund [:booking-day :value-day :money/amount :fee/difference])))
      (is (= [25.00M] (mapv :money/amount (:fees @observed))))
      (flush)
      (let [confirmed (yield/report module "ACC-001")
            journal (ledger/journal ledger "ACC-001")]
        (is (= [25.00M -25.00M] (mapv :money/amount (:fees confirmed))))
        (is (empty? (:pending-financial-commands confirmed)))
        (is (= 0.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
        (is (= [-1.00M -25.00M 1.00M 25.00M] (mapv :financial/effect journal)))
        (is (= :recorded (:outcome (yield/calculate! module (historical 4 1 1 "reversal")))))
        (is (= @submissions (:financial-intents (yield/report module "ACC-001"))))
        (is (= journal (ledger/journal ledger "ACC-001")))))))

(deftest financial-command-is-saved-before-the-submission-can-fail
  (doseq [purpose [:fee :interest]]
    (testing (name purpose)
      (let [module-ref (atom nil)
            observed (atom nil)
            fail? (atom true)
            {:keys [module submit! authorization ledger submissions]}
            (real-fixture
             (fn [auth command]
               (reset! observed (yield/report @module-ref "ACC-001"))
               (if (compare-and-set! fail? true false)
                 (throw (ex-info "Controlled failure before recording" {}))
                 (authorization/submit! auth command))))
            fee? (= :fee purpose)
            action #(if fee? (yield/calculate! module (daily 2))
                         (yield/settle! module (settlement "original" 3 2)))]
        (reset! module-ref module)
        (submit! (fixtures/command "principal" (if fee? :debit :credit)
                                   (if fee? 1.00M 100.00M) 1))
        (when-not fee? (yield/calculate! module (daily 2)))
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled failure" (action)))
        (let [command (first @submissions)
              before-effect @observed]
          (is (= [command] (:financial-intents before-effect)))
          (is (= [command] (:pending-financial-commands before-effect)))
          (is (= purpose (:purpose command)))
          (is (= (if fee? 25.00M 0.04M) (:money/amount command)))
          (when fee?
            (is (= {:reference-day 1 :booking-day 2 :value-day 2
                    :money/amount 25.00M :calculation/base -1.00M
                    :calculation/target 25.00M :booking-cutoff 1 :source-event-counter 1}
                   (select-keys (:fee/assessment command)
                                [:reference-day :booking-day :value-day :money/amount
                                 :calculation/base :calculation/target :booking-cutoff :source-event-counter]))))
          (is (= 0.00M (:interest-paid before-effect)))
          (is (= (if fee? -1.00M 100.00M)
                 (:financial-balance (authorization/snapshot authorization "ACC-001"))))
          (is (= 1 (count (ledger/journal ledger "ACC-001"))))
          (is (= :recorded (:outcome (action))))
          (let [after (yield/report module "ACC-001")]
            (is (= [command] (:financial-intents after)))
            (is (empty? (:pending-financial-commands after)))
            (is (= (if fee? -26.00M 100.04M)
                   (:financial-balance (authorization/snapshot authorization "ACC-001"))))))))))

(deftest an-unknown-payment-keeps-its-original-command-when-the-request-changes
  (let [fail? (atom true)
        {:keys [module submit! authorization ledger submissions]}
        (real-fixture
         (fn [auth command]
           (if (compare-and-set! fail? true false)
             (throw (ex-info "Controlled failure before recording" {}))
             (authorization/submit! auth command))))]
    (submit! (fixtures/command "principal" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled failure"
                          (yield/settle! module (settlement "original" 3 2))))
    (let [original (first @submissions)]
      (yield/calculate! module (daily 3))
      (let [waiting (yield/report module "ACC-001")]
        (is (= [0.04M 0.04M] (mapv :money/amount (:interest/components waiting))))
        (is (= [original] (:pending-financial-commands waiting)))
        (is (false? (:complete? waiting)))
        (is (= 0.00M (:interest-paid waiting)))
        (is (= 0.08M (:pending-interest waiting))))
      (is (= :retry-required (:outcome (yield/settle! module (settlement "different" 4 3)))))
      (is (= 100.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (empty? (interest-journal ledger)))
      (let [result (yield/settle! module (settlement "original" 5 4))
            view (yield/report module "ACC-001")]
        (is (= :recorded (:outcome result)))
        (is (= original (last @submissions)))
        (is (= {:settlement/id "original" :run-day 3 :period/end-day 2
                :booking-cutoff 2 :money/amount 0.04M :component/ids (:component/ids original)}
               (select-keys (:settlement/receipt result)
                            [:settlement/id :run-day :period/end-day :booking-cutoff :money/amount :component/ids])))
        (is (= [original] (:financial-intents view)))
        (is (= 0.04M (:interest-paid view)))
        (is (= 0.04M (:pending-interest view)))
        (is (empty? (:pending-financial-commands view))))
      (is (= :recorded (:outcome (yield/settle! module (settlement "different" 5 4)))))
      (is (= [0.04M 0.04M] (mapv :financial/effect (interest-journal ledger))))
      (is (= 100.08M (:financial-balance (authorization/snapshot authorization "ACC-001")))))))

(deftest delivered-payment-confirmation-prevents-a-different-settlement-from-paying-it-again
  (let [lose? (atom true)
        {:keys [module submit! authorization ledger flush submissions]}
        (real-fixture
         (fn [auth command]
           (let [result (authorization/submit! auth command)]
             (if (compare-and-set! lose? true false)
               (throw (ex-info "Controlled lost response" {}))
               result))))]
    (submit! (fixtures/command "principal" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled lost response"
                          (yield/settle! module (settlement "original" 3 2))))
    (let [original (first @submissions)]
      (is (= 100.04M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (empty? (interest-journal ledger)))
      (is (= 0.00M (:interest-paid (yield/report module "ACC-001"))))
      (flush)
      (let [confirmed (yield/report module "ACC-001")]
        (is (= 0.04M (:interest-paid confirmed)))
        (is (= 0.00M (:pending-interest confirmed)))
        (is (= [original] (:financial-intents confirmed)))
        (is (empty? (:pending-financial-commands confirmed)))
        (is (= [(:component/ids original)] (mapv :component/ids (:settlements confirmed)))))
      (yield/calculate! module (daily 3))
      (is (= :recorded (:outcome (yield/settle! module (settlement "different" 4 3)))))
      (let [view (yield/report module "ACC-001")
            journal (interest-journal ledger)]
        (is (= 0.08M (:interest-paid view)))
        (is (= 0.00M (:pending-interest view)))
        (is (= [0.04M 0.04M] (mapv :financial/effect journal)))
        (is (= 100.08M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
        (is (= 2 (count (set (mapcat :component/ids (:settlements view))))))
        (is (= 2 (count (mapcat :component/ids (:settlements view)))))
        (is (= :duplicate (:outcome (yield/settle! module (settlement "original" 5 4)))))
        (is (= view (yield/report module "ACC-001")))
        (is (= journal (interest-journal ledger)))))))

(deftest a-direct-confirmed-duplicate-uses-the-original-financial-receipt
  (let [{:keys [module submit! authorization ledger submissions]}
        (real-fixture
         (fn [auth command]
           (authorization/submit! auth command)
           (authorization/submit! auth {:transaction/id (:transaction/id command)
                                        :money/amount "changed duplicate payload"})))]
    (submit! (fixtures/command "principal" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (let [component-ids (mapv :component/id (:interest/components (yield/report module "ACC-001")))
          result (yield/settle! module (settlement "original" 3 2))
          view (yield/report module "ACC-001")]
      (is (= :recorded (:outcome result)))
      (is (true? (:complete? result)))
      (is (= :duplicate (get-in result [:financial/result :outcome])))
      (is (= component-ids (get-in result [:settlement/receipt :component/ids])))
      (is (= 0.04M (get-in result [:settlement/receipt :money/amount])))
      (is (= @submissions (:financial-intents view)))
      (is (empty? (:pending-financial-commands view)))
      (is (= 0.04M (:interest-paid view)))
      (is (= 0.00M (:pending-interest view)))
      (is (= 1 (count (:settlements view))))
      (is (= 2 (:last-event-counter (authorization/snapshot authorization "ACC-001"))))
      (is (empty? (authorization/pending-deliveries authorization)))
      (is (= 100.04M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (= 100.04M (:money/amount (ledger/balance ledger {:account/id "ACC-001"
                                                          :value-through-day 3 :booking-through-day 3}))))
      (is (= [0.04M] (mapv :financial/effect (interest-journal ledger)))))))

(deftest known-financial-rejections-release-pending-commands-and-retain-proposals
  (doseq [outcome [:retry-required :invalid]]
    (testing (name outcome)
      (let [reject? (atom true)
            {:keys [module submit! authorization ledger submissions]}
            (real-fixture
             (fn [auth command]
               (if (compare-and-set! reject? true false)
                 (if (= :retry-required outcome)
                   (do (authorization/submit! auth (fixtures/command "future" :credit 10.00M 4))
                       (authorization/submit! auth command))
                   ;; Simulate a malformed transport payload; the real receiver rejects it.
                   (authorization/submit! auth (assoc command :money/amount 0.00M)))
                 (authorization/submit! auth command))))]
        (submit! (fixtures/command "principal" :credit 100.00M 1))
        (yield/calculate! module (daily 2))
        (let [result (yield/settle! module (settlement "original" 3 2))]
          (is (= outcome (:outcome result)))
          (when (= :retry-required outcome) (is (= :stale-source (:reason result)))))
        (let [original (first @submissions)
              rejected (yield/report module "ACC-001")]
          (is (= [original] (:financial-intents rejected)))
          (is (empty? (:pending-financial-commands rejected)))
          (is (= 0.00M (:interest-paid rejected)))
          (is (= 0.04M (:pending-interest rejected)))
          (is (empty? (interest-journal ledger)))
          (is (= :recorded (:outcome (yield/settle! module (settlement "original" 3 2)))))
          (let [view (yield/report module "ACC-001")]
            (is (= original (first (:financial-intents view))))
            (is (= (last @submissions) (last (:financial-intents view))))
            (is (empty? (:pending-financial-commands view)))
            (is (= 0.04M (:interest-paid view)))
            (is (= 0.00M (:pending-interest view)))
            (is (= 1 (count (set (map :transaction/id @submissions)))))
            (is (= (if (= :retry-required outcome) 110.04M 100.04M)
                   (:financial-balance (authorization/snapshot authorization "ACC-001"))))
            (is (= [0.04M] (mapv :financial/effect (interest-journal ledger))))))))))

(deftest delivered-negative-payment-confirms-its-signed-amount-without-another-request
  (let [lose? (atom true)
        {:keys [module submit! authorization ledger flush]}
        (real-fixture
         (fn [auth command]
           (let [result (authorization/submit! auth command)]
             (if (and (= :interest (:purpose command)) (= :debit (:transaction/type command))
                      (compare-and-set! lose? true false))
               (throw (ex-info "Controlled lost debit response" {}))
               result))))]
    (submit! (fixtures/command "principal" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (yield/settle! module (settlement "paid-earlier" 3 2))
    (submit! (fixtures/command "correction" :debit 100.00M 3 {:value-day 1}))
    (yield/calculate! module (historical 4 1 1 "correction"))
    (is (= -0.04M (:pending-interest (yield/report module "ACC-001"))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled lost debit response"
                          (yield/settle! module (settlement "negative" 5 2))))
    (is (= 0.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
    (flush)
    (let [confirmed (yield/report module "ACC-001")
          journal (interest-journal ledger)]
      (is (= [0.04M -0.04M] (mapv :money/amount (:settlements confirmed))))
      (is (= 0.00M (:interest-paid confirmed)))
      (is (= 0.00M (:pending-interest confirmed)))
      (is (empty? (:pending-financial-commands confirmed)))
      (is (= [0.04M -0.04M] (mapv :financial/effect journal)))
      (yield/settle! module (settlement "later" 6 2))
      (is (= journal (interest-journal ledger)))
      (is (= 0.00M (:financial-balance (authorization/snapshot authorization "ACC-001")))))))
