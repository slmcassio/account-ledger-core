(ns account-ledger.yield-fees.logic.transitions-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.yield-fees.logic.core :as core]
            [account-ledger.yield-fees.logic.transitions :as transitions]))

(def configuration (contracts/normalize-config fixtures/config))
(def account (get-in configuration [:accounts "ACC-001"]))
(def daily {:account/id "ACC-001" :mode :daily :run-day 2 :reference-day 1 :booking-cutoff 1})
(def settlement {:account/id "ACC-001" :settlement/id "month" :run-day 3
                 :period/end-day 2 :booking-cutoff 2})
(def principal (assoc (fixtures/command "principal" :credit 100.00M 1)
                      :financial/effect 100.00M :event-counter 1 :purpose :principal))

(defn accrued-state []
  (let [[received _] (transitions/receive configuration (transitions/initial-state configuration) principal)]
    (first (transitions/record-interest received account daily 1))))

(defn command-for [state]
  (let [view (core/report account (get-in state [:accounts "ACC-001"]) "ACC-001")
        {:keys [components amount]} (transitions/settlement-selection account view settlement)]
    (transitions/payment-command account view settlement components amount)))

(deftest saved-payment-intent-keeps-original-components-dates-and-amount
  (let [initial (accrued-state)
        command (command-for initial)
        [saved sent] (transitions/save-command initial command)
        [retried retry-command] (transitions/save-command saved (assoc command :money/amount 5.00M :booking-day 8))
        [blocked result] (transitions/save-command saved (assoc command :transaction/id "system/another"))]
    (is (= 0.04M (:money/amount command)))
    (is (= command sent retry-command))
    (is (= saved retried blocked))
    (is (= :pending-financial-command (:reason result)))
    (is (= [command] (get-in saved [:accounts "ACC-001" :financial-intents])))
    (is (empty? (get-in initial [:accounts "ACC-001" :financial-intents])))
    (is (empty? (get-in saved [:accounts "ACC-001" :settlements])))))

(deftest uncertainty-and-definite-rejection-have-different-local-results
  (let [initial (accrued-state)
        command (command-for initial)
        [saved _] (transitions/save-command initial command)]
    (doseq [result [nil {:outcome :retry-required :reason :unknown-response}]]
      (testing (pr-str result)
        (is (= saved (first (transitions/record-financial-result saved command result))))))
    (doseq [result [{:outcome :invalid} {:outcome :retry-required :reason :stale-source}]]
      (let [[rejected _] (transitions/record-financial-result saved command result)]
        (is (nil? (transitions/pending-command rejected "ACC-001")))
        (is (= [command] (get-in rejected [:accounts "ACC-001" :financial-intents])))
        (is (= 0.04M (:pending-interest (core/report account (get-in rejected [:accounts "ACC-001"]) "ACC-001"))))))))

(deftest confirmation-reconciles-payment-before-a-new-selection
  (let [initial (accrued-state)
        command (command-for initial)
        [saved _] (transitions/save-command initial command)
        event (assoc command :event-counter 2 :financial/effect 0.04M)
        [confirmed result] (transitions/receive configuration saved event)
        receipt (get-in confirmed [:settlement-ids "month"])
        [repeated duplicate] (transitions/receive configuration confirmed {:transaction/id (:transaction/id event)})
        next-selection (transitions/settlement-selection account (get-in confirmed [:accounts "ACC-001"])
                                                         (assoc settlement :settlement/id "different"))]
    (is (= :recorded (:outcome result)))
    (is (= {:account/id "ACC-001" :settlement/id "month" :transaction/id (:transaction/id command)
            :component/ids (:component/ids command) :period/end-day 2 :booking-cutoff 2
            :money/amount 0.04M :run-day 3} receipt))
    (is (= {:components [] :amount 0.00M} next-selection))
    (is (nil? (transitions/pending-command confirmed "ACC-001")))
    (is (= confirmed repeated))
    (is (= :duplicate (:outcome duplicate)))
    (is (= event (transitions/original-event {:outcome :duplicate :original/result {:recorded/event event}})))
    (is (= event (transitions/original-event {:outcome :recorded :recorded/event event})))
    (is (nil? (transitions/original-event {:outcome :invalid})))) )

(deftest fee-preparation-preserves-reversal-refund-economic-date
  (let [charge {:component/id "charged" :component/type :ordinary :reference-day 1
                :booking-day 2 :value-day 2 :money/amount 25.00M :source-event-counter 1}
        reversal {:transaction/id "reversal" :transaction/type :reversal}
        state (-> (transitions/initial-state configuration)
                  (assoc-in [:input-ids "reversal"] reversal)
                  (assoc-in [:accounts "ACC-001" :fees] [charge]))
        request {:account/id "ACC-001" :mode :historical :run-day 4 :booking-cutoff 3
                 :from-day 1 :through-day 1 :cause/transaction-id "reversal"}
        assessment {:reference-day 1 :fee/difference -25.00M :fee/records [charge]
                    :fee/base 0.00M :fee/target 0.00M :source-event-counter 3}
        {:keys [command]} (transitions/prepare-fee state account request assessment)]
    (is (= {:transaction/type :credit :money/amount 25.00M :fee/difference -25.00M
            :booking-day 4 :value-day 2 :fee/original-value-day 2 :cause/transaction-id "reversal"}
           (select-keys command [:transaction/type :money/amount :fee/difference :booking-day
                                 :value-day :fee/original-value-day :cause/transaction-id])))
    (is (= -25.00M (get-in command [:fee/assessment :money/amount])))
    (is (= [charge] (get-in state [:accounts "ACC-001" :fees])))
    (is (nil? (transitions/prepare-fee state account (assoc request :interest-only? true) assessment)))))

(deftest zero-settlement-records-consumed-zero-components-without-a-command
  (let [initial (transitions/initial-state configuration)
        [accrued _] (transitions/record-interest initial account daily 1)
        [settled result] (transitions/settle-zero accrued account settlement)
        components (get-in accrued [:accounts "ACC-001" :interest/components])]
    (is (= [0.00M] (mapv :money/amount components)))
    (is (= 0.00M (get-in result [:settlement/receipt :money/amount])))
    (is (= (mapv :component/id components) (get-in result [:settlement/receipt :component/ids])))
    (is (empty? (get-in settled [:accounts "ACC-001" :financial-intents])))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"serially"
                          (transitions/settle-zero (accrued-state) account settlement)))))
