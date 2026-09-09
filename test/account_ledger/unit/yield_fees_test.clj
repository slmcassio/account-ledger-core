(ns account-ledger.unit.yield-fees-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.yield-fees.domain :as domain]))

(def account {:money/currency :AED :opening-balance 0.00M :daily-fee 25.00M})

(defn event [id counter effect booking value & [extra]]
  (merge {:transaction/id id :event-counter counter :financial/effect effect
          :booking-day booking :value-day value :purpose :principal} extra))

(deftest contiguous-prefix-does-not-confuse-highest-observed-with-complete
  (is (= {:source-event-counter 0 :complete? true} (domain/input-view [])))
  (is (= {:source-event-counter 1 :complete? false}
         (domain/input-view [(event "one" 1 0M 1 1) (event "three" 3 0M 1 1)]))))

(deftest closing-base-uses-both-dates-and-opening-state
  (let [events [(event "A" 1 100.00M 1 1)
                (event "B" 2 50.00M 2 1)
                (event "C" 3 -40.00M 1 3)
                (event "hold" 4 0.00M 1 1)]]
    (is (= 110.00M (domain/closing-base (assoc account :opening-balance 10.00M) events 1 1)))
    (is (= 150.00M (domain/closing-base account events 1 2)))
    (is (= 110.00M (domain/closing-base account events 3 2)))))

(deftest fee-base-excludes-only-its-own-included-components
  (let [events [(event "principal" 1 10.00M 1 1)
                (event "own-fee" 2 -25.00M 2 2 {:purpose :fee :reference-day 2})
                (event "other-fee" 3 -25.00M 2 2 {:purpose :fee :reference-day 1})
                (event "other-refund" 4 25.00M 4 2 {:purpose :fee :reference-day 1})]]
    (is (= -15.00M (domain/fee-base account events 2 3)))
    (is (= 10.00M (domain/fee-base account events 2 4)))
    (is (= -40.00M (domain/closing-base account events 2 3)))))

(deftest fee-target-is-negative-only-and-respects-explicit-zero-configuration
  (is (= 25.00M (domain/fee-target account -0.01M)))
  (is (= 0.00M (domain/fee-target account 0.00M)))
  (is (= 0.00M (domain/fee-target account 1.00M)))
  (is (= 0.000M (domain/fee-target {:money/currency :BHD :daily-fee 0.000M} -10.000M))))

(deftest settlement-selects-unpaid-components-with-period-and-booking-boundaries
  (let [components [{:component/id "ordinary" :component/type :ordinary :reference-day 5 :booking-day 6 :money/amount 0.10M}
                    {:component/id "adjustment" :component/type :adjustment :reference-day 2 :booking-day 5 :money/amount -0.30M}
                    {:component/id "today" :component/type :adjustment :reference-day 2 :booking-day 6 :money/amount 0.20M}
                    {:component/id "new-month" :component/type :ordinary :reference-day 6 :booking-day 7 :money/amount 0.10M}]
        request {:period/end-day 5 :booking-cutoff 5}]
    (is (= ["ordinary" "adjustment"]
           (mapv :component/id (domain/eligible-components components [] request))))
    (is (= -0.20M (domain/total :AED (domain/eligible-components components [] request))))
    (is (= ["adjustment"]
           (mapv :component/id
                 (domain/eligible-components components [{:component/ids ["ordinary"]}] request))))))

(deftest bounded-review-keeps-derived-financial-events-after-the-principal-bound
  (let [events [(event "A" 1 12.00M 1 1)
                (event "B" 2 0.49M 2 1)
                (event "interest-payment" 3 0.01M 2 2 {:purpose :interest})
                (event "C" 4 100.00M 2 1)]
        state {:events (into (sorted-map) (map (juxt :event-counter identity) events))
               :fees [] :interest/components []}
        request {:booking-cutoff 2 :input-through-event-counter 2 :interest-only? true}
        assessment (domain/assessment account state request 2)]
    (is (= 12.50M (:interest/base assessment)))
    (is (= 0.01M (:interest/target assessment)))
    (is (= 4 (:source-event-counter assessment)))
    (is (= 112.50M (:fee/base assessment)))))
