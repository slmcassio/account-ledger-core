(ns account-ledger.e2e.replay-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.replay :as replay]
            [account-ledger.system :as system]
            [account-ledger.ledger.api :as ledger]
            [account-ledger.fixtures :as fixtures]))

(defn- interest-minor-units
  "Independent oracle: 0.04% = 1/2500; HALF_UP in integer minor units."
  [balance-minor]
  (quot (+ (max 0 balance-minor) 1250) 2500))

(deftest independent-financial-oracle
  (let [before-late [25000 25000 65000 28500 -33500]
        corrected-after-e7 [-37000 3000 -33500]
        ordinary (mapv interest-minor-units before-late)
        correction (mapv - (mapv interest-minor-units corrected-after-e7) (subvec ordinary 1 4))
        fee-days (filter neg? [-37000 3000 -33500 -33500])
        fees (* (count fee-days) 2500)
        paid (reduce + ordinary)
        day-six (+ -33500 (- fees) 62000 paid)]
    (is (= [10 10 26 11 0] ordinary))
    (is (= [-10 -25 -11] correction))
    (is (= -46 (reduce + correction)))
    (is (= 7500 fees))
    (is (= 57 paid))
    (is (= 21057 day-six))
    (is (= 28557 (+ day-six fees)))
    (is (= [10 25 11 11]
           (mapv - (mapv interest-minor-units [25000 65000 28500 28500]) [0 1 0 0])))
    (is (= 8 (interest-minor-units day-six)))
    (is (= 4 (interest-minor-units 10000)))
    (is (= 10000 (+ 3333 3333 3334)))
    (is (= 38 (reduce + (map interest-minor-units [46500 46500]))))
    (is (= 1 (interest-minor-units 1250)))
    (is (= 0 (interest-minor-units 1249)))))

(deftest complete-six-day-replay
  (let [{:keys [system daily-reports summary]} (replay/run)
        aed (filterv #(= "ACC-001" (:account/id %)) daily-reports)
        bhd (filterv #(= "ACC-002" (:account/id %)) daily-reports)]
    (is (= fixtures/events replay/events))
    (is (= 12 (count daily-reports)))
    (is (every? :complete? daily-reports))
    (is (= [1 2 3 4 5 6] (mapv :day aed)))
    (is (= [250M 250M 650M 285M -335M 210.57M] (mapv :financial-balance aed)))
    (is (= [0M 200M 200M 0M 0M 0M] (mapv :held-amount aed)))
    (is (= [250M 50M 450M 285M -335M 210.57M] (mapv :available-balance aed)))
    (is (= [2 3 4 6 7 12] (mapv :last-event-counter aed)))
    (is (= [0M 0.10M 0.20M 0.46M 0.57M -0.46M] (mapv :pending-interest aed)))
    (is (= [0M 0M 0M 0M 0M 10.000M] (mapv :financial-balance bhd)))
    (is (= 0.57M (get-in summary ["ACC-001" :interest-paid])))
    (is (= -0.46M (get-in summary ["ACC-001" :pending-interest])))
    (is (= 0.000M (get-in summary ["ACC-002" :interest-paid])))
    (is (= 0.004M (get-in summary ["ACC-002" :pending-interest])))
    (is (= :declined (get-in summary ["ACC-001" :authorization-states "Auth-B"])))
    (is (= :settled (get-in summary ["ACC-001" :authorization-states "Auth-A"])))
    (is (seq (get-in summary ["ACC-001" :occurrences])))
    (let [components (get-in summary ["ACC-001" :interest/components])
          originals (filter #(= :ordinary (:component/type %)) components)
          adjustments (filter #(= :adjustment (:component/type %)) components)
          fees (filter #(not (zero? (:money/amount %))) (get-in summary ["ACC-001" :fees]))]
      (is (= [0.10M 0.10M 0.26M 0.11M 0M] (mapv :money/amount originals)))
      (is (= [-0.10M -0.25M -0.11M] (mapv :money/amount adjustments)))
      (is (= [2 4 5] (mapv :reference-day fees)))
      (is (= [25M 25M 25M] (mapv :money/amount fees)))
      (is (every? #(= [6 6] ((juxt :booking-day :value-day) %)) fees))
      (is (every? #(= [6 6] ((juxt :booking-day :value-day) %)) adjustments)))
    (is (= 11 (count (ledger/journal (:ledger system) "ACC-001"))))
    (let [entry (first (ledger/journal (:ledger system) "ACC-002"))
          customer (filter #(= "customer/ACC-002" (:book/account %)) (:postings entry))]
      (is (= 6 (count (:postings entry))))
      (is (= [3.333M 3.333M 3.334M] (mapv :money/amount customer))))
    (is (= daily-reports (:daily-reports (replay/run))))))

(deftest separate-day-seven-and-accounting-boundaries
  (let [{:keys [system daily-reports summary]} (replay/run)
        original-reports daily-reports
        prior-journal (ledger/journal (:ledger system) "ACC-001")
        position (:journal/position (last prior-journal))
        continuation (replay/continue-day-seven system)
        aed (get-in continuation [:summary "ACC-001"])
        bhd (get-in continuation [:summary "ACC-002"])]
    (is (= 285.57M (:financial-balance aed)))
    (is (= 15 (:last-event-counter aed)))
    (is (= 0.19M (:pending-interest aed)))
    (is (= 0.008M (:pending-interest bhd)))
    (is (= 0.11M (reduce + 0M (map :money/amount
                                (filter #(and (= :adjustment (:component/type %))
                                              (<= (:reference-day %) 5)) (:interest/components aed))))))
    (is (= 0.08M (:money/amount (first (filter #(and (= :ordinary (:component/type %))
                                                                    (= 6 (:reference-day %)))
                                                            (:interest/components aed))))))
    (is (= prior-journal (subvec (ledger/journal (:ledger system) "ACC-001") 0 (count prior-journal))))
    (is (= original-reports daily-reports))
    (is (= 210.57M (get-in summary ["ACC-001" :financial-balance])))
    (doseq [[cutoff as-of expected] [[6 nil 210.57M] [7 position 210.57M] [7 nil 285.57M]]]
      (is (= expected (:money/amount
                       (ledger/balance (:ledger system)
                                       (cond-> {:account/id "ACC-001" :value-through-day 6 :booking-through-day cutoff}
                                         as-of (assoc :as-of-journal-position as-of)))))))
    (let [refunds (filter #(neg? (:money/amount %)) (:fees aed))]
      (is (= [-25M -25M -25M] (mapv :money/amount refunds)))
      (is (every? #(= [7 6] ((juxt :booking-day :value-day) %)) refunds)))))
