(ns account-ledger.yield-fees.model.models-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is]]
            [account-ledger.shared.logic.contracts :as contracts]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.yield-fees.logic.core :as core]
            [account-ledger.yield-fees.logic.transitions :as transitions]
            [account-ledger.yield-fees.model.models :as models]))

(deftest lifecycle-records-fit-internal-models
  (let [configuration (contracts/normalize-config fixtures/config)
        account (get-in configuration [:accounts "ACC-001"])
        initial (transitions/initial-state configuration)
        principal (assoc (fixtures/command "principal" :credit 100.00M 1)
                         :financial/effect 100.00M :event-counter 1 :purpose :principal)
        [received _] (transitions/receive configuration initial principal)
        [accrued _] (transitions/record-interest received account
                                                {:account/id "ACC-001" :mode :daily :run-day 2
                                                 :reference-day 1 :booking-cutoff 1} 1)
        view (core/report account (get-in accrued [:accounts "ACC-001"]) "ACC-001")
        request {:account/id "ACC-001" :settlement/id "month" :run-day 3 :booking-cutoff 2 :period/end-day 2}
        command (transitions/payment-command account view request (:interest/components view) 0.04M)
        [pending _] (transitions/save-command accrued command)
        event (assoc command :event-counter 2 :financial/effect 0.04M)
        [confirmed _] (transitions/receive configuration pending event)]
    (doseq [state [initial received accrued pending confirmed]]
      (is (s/valid? ::models/state state) (s/explain-str ::models/state state)))
    (is (s/valid? ::models/interest-component (first (:interest/components view))))
    (is (s/valid? ::models/financial-intent command))
    (is (s/valid? ::models/settlement-receipt (get-in confirmed [:settlement-ids "month"])))
    (doseq [invalid [(dissoc command :component/ids)
                     (assoc command :component/ids "component")
                     (assoc command :component/ids [])
                     (assoc command :component/ids [""])]]
      (is (not (s/valid? ::models/financial-intent invalid))))
    (doseq [invalid [(dissoc (first (:interest/components view)) :calculation/target)
                     (assoc (first (:interest/components view)) :calculation/base 1.0)
                     (assoc (first (:interest/components view)) :calculation/target nil)]]
      (is (not (s/valid? ::models/interest-component invalid))))
    (is (not (s/valid? ::models/account-state
                       (dissoc (get-in accrued [:accounts "ACC-001"]) :interest/components))))
    (is (not (s/valid? ::models/account-state
                       (assoc (get-in accrued [:accounts "ACC-001"]) :interest/components {}))))
    (is (not (s/valid? ::models/interest-component
                       (dissoc (first (:interest/components view)) :calculation/base))))
    (is (not (s/valid? ::models/settlement-receipt
                       (dissoc (get-in confirmed [:settlement-ids "month"]) :money/amount))))))

(deftest internal-models-do-not-restrict-unrelated-command-metadata
  (let [configuration (contracts/normalize-config fixtures/config)]
    (doseq [extra [{:calculation/base "external"} {:calculation/target "external"}
                   {:component/ids "external"} {:interest/components "external"}]
            :let [command (fixtures/command "external-metadata" :credit 1.00M 1 extra)]]
      (is (= {:valid? true :command command}
             (contracts/validate-command configuration command))
          (pr-str extra)))))
