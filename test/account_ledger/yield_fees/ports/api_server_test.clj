(ns account-ledger.yield-fees.ports.api-server-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.fixtures :as fixtures]
            [account-ledger.authorization.ports.api-server :as authorization]
            [account-ledger.ledger.ports.api-server :as ledger]
            [account-ledger.yield-fees.ports.api-server :as yield]))

(defn approved [id counter effect day & [extra]]
  (merge (fixtures/command id (if (neg? effect) :debit :credit) (abs effect) day)
         {:financial/effect effect :event-counter counter :purpose :principal}
         extra))

(defn daily [day]
  {:account/id "ACC-001" :run-day day :reference-day (dec day)
   :booking-cutoff (dec day) :mode :daily})

(defn settlement [id day end]
  {:account/id "ACC-001" :settlement/id id :run-day day
   :period/end-day end :booking-cutoff (dec day)})

(defn historical [day from through cause & [extra]]
  (merge {:account/id "ACC-001" :run-day day :booking-cutoff (dec day)
          :mode :historical :from-day from :through-day through :cause/transaction-id cause} extra))

(defn real-fixture
  ([] (real-fixture nil))
  ([submit-wrapper]
   (let [auth (authorization/create fixtures/config)
         book (ledger/create fixtures/config)
         module (atom nil)
         submitted (atom [])
         flush (fn []
                 (doseq [{:keys [destination event] :as envelope} (authorization/pending-deliveries auth)]
                   (let [result (if (= :ledger destination) (ledger/post! book event)
                                    (yield/receive! @module event))]
                     (when (#{:recorded :duplicate} (:outcome result))
                       (authorization/ack-delivery! auth (:delivery/id envelope)))))
                 {:pending-count (count (authorization/pending-deliveries auth)) :errors []})
         created (yield/create fixtures/config
                               {:submit-financial! (fn [command]
                                                     (swap! submitted conj command)
                                                     (if submit-wrapper (submit-wrapper auth command)
                                                         (authorization/submit! auth command)))
                                :flush-deliveries! flush})]
     (reset! module created)
     {:module created :authorization auth :ledger book :flush flush :submissions submitted
      :submit! (fn [command] (let [result (authorization/submit! auth command)] (flush) result))})))

(defn port-fixture []
  (let [module (atom nil)
        submissions (atom [])
        queue (atom [])
        ports {:submit-financial!
               (fn [command]
                 (swap! submissions conj command)
                 (let [event (assoc command :event-counter (inc (:source-event-counter command))
                                    :financial/effect (if (= :credit (:transaction/type command))
                                                        (:money/amount command) (- (:money/amount command))))]
                   (swap! queue conj event)
                   {:outcome :recorded :transaction/id (:transaction/id command) :recorded/event event}))
               :flush-deliveries!
               (fn []
                 (doseq [event @queue] (yield/receive! @module event))
                 (reset! queue [])
                 {:pending-count 0 :errors []})}
        created (yield/create fixtures/config ports)]
    (reset! module created)
    {:module created :submissions submissions}))

(deftest positive-accrual-reaches-financial-port-and-settles-original-components
  (let [{:keys [module submissions]} (port-fixture)]
    (is (= :recorded (:outcome (yield/receive! module (approved "A" 1 465.00M 1)))))
    (is (= :recorded (:outcome (yield/calculate! module (daily 2)))))
    (is (= :recorded (:outcome (yield/calculate! module (daily 3)))))
    (let [before (yield/report module "ACC-001")
          result (yield/settle! module (settlement "month" 4 3))
          after (yield/report module "ACC-001")]
      (is (= 0.38M (:pending-interest before)))
      (is (= :recorded (:outcome result)))
      (is (= 0.38M (:interest-paid after)))
      (is (= 0.00M (:pending-interest after)))
      (is (= 2 (:source-event-counter after)))
      (is (= (:interest/components before) (:interest/components after)))
      (is (= 0.38M (:money/amount (first @submissions))))
      (is (= (mapv :component/id (:interest/components before))
             (:component/ids (first (:settlements after)))))
      (is (= :duplicate (:outcome (yield/settle! module (settlement "month" 4 3)))))
      (is (= 1 (count @submissions))))))

(deftest gap-blocks-calculation-until-hold-only-event-arrives
  (let [{:keys [module]} (port-fixture)]
    (yield/receive! module (approved "opening-credit" 1 100.00M 1))
    (yield/receive! module (approved "credit" 3 100.00M 1))
    (is (= 1 (:source-event-counter (yield/report module "ACC-001"))))
    (is (= :incomplete-input (:reason (yield/calculate! module (daily 2)))))
    (yield/receive! module (approved "hold" 2 0.00M 1 {:transaction/type :authorization
                                                       :money/amount 100.00M :authorization/id "hold"}))
    (is (= :recorded (:outcome (yield/calculate! module (daily 2)))))
    (is (= 0.08M (:pending-interest (yield/report module "ACC-001"))))
    (is (= :duplicate (:outcome (yield/receive! module {:transaction/id "credit" :money/amount "malformed"}))))
    (is (= :counter-conflict (:reason (yield/receive! module (approved "collision" 3 1.00M 1)))))))

(deftest ordinary-fees-use-next-day-dates-and-refreshed-source-counters
  (let [{:keys [module submit! authorization submissions]} (real-fixture)]
    (submit! (fixtures/command "debit" :debit 0.01M 1))
    (yield/calculate! module (daily 2))
    (yield/calculate! module (daily 3))
    (let [view (yield/report module "ACC-001")]
      (is (= [25.00M 25.00M] (mapv :money/amount (:fees view))))
      (is (= [2 3] (mapv :booking-day (:fees view)) (mapv :value-day (:fees view))))
      (is (= [1 2] (mapv :source-event-counter @submissions)))
      (is (= -50.01M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (= 0.00M (:pending-interest view)))
      (yield/calculate! module (daily 3))
      (is (= view (yield/report module "ACC-001"))))))

(deftest late-credit-adjusts-rounded-target-once-and-excludes-current-bookings
  (let [{:keys [module submit!]} (real-fixture)]
    (submit! (fixtures/command "A" :credit 12.49M 1))
    (yield/calculate! module (daily 2))
    (submit! (fixtures/command "B" :credit 0.01M 2 {:value-day 1}))
    (yield/calculate! module (daily 2))
    (is (= 0.00M (:pending-interest (yield/report module "ACC-001"))))
    (is (= :recorded (:outcome (yield/calculate! module (historical 3 1 1 "B")))))
    (let [view (yield/report module "ACC-001")]
      (is (= [0.00M 0.01M] (mapv :money/amount (:interest/components view))))
      (is (= [2 3] (mapv :booking-day (:interest/components view))))
      (yield/calculate! module (historical 3 1 1 "B"))
      (is (= view (yield/report module "ACC-001"))))))

(deftest historical-review-requires-assessed-days-and-eligible-cause
  (let [{:keys [module submit!]} (real-fixture)]
    (submit! (fixtures/command "future" :credit 1.00M 3 {:value-day 1}))
    (is (= :unassessed-day (:reason (yield/calculate! module (historical 3 1 1 "future")))))
    (yield/calculate! module (daily 2))
    (is (= :ineligible-cause (:reason (yield/calculate! module (historical 3 1 1 "future")))))
    (is (= :ineligible-cause (:reason (yield/calculate! module (historical 3 1 1 "missing")))))
    (is (= :invalid-calculation (:reason (yield/calculate! module (assoc (daily 3) :booking-cutoff 3)))))))

(deftest negative-settlement-debits-zero-balance-and-counts-paid-adjustments
  (let [{:keys [module submit! authorization ledger]} (real-fixture)]
    (submit! (fixtures/command "A" :credit 750.00M 1))
    (yield/calculate! module (daily 2))
    (yield/settle! module (settlement "earlier-payment" 2 1))
    (submit! (fixtures/command "B" :debit 500.30M 2))
    (yield/calculate! module (daily 3))
    (submit! (fixtures/command "C" :debit 750.00M 3 {:value-day 1}))
    (submit! (fixtures/command "D" :credit 500.00M 3))
    (yield/calculate! module (historical 4 1 1 "C"))
    (is (= 0.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
    (let [before (yield/report module "ACC-001")
          result (yield/settle! module (settlement "negative" 5 2))
          after (yield/report module "ACC-001")]
      (is (= -0.20M (:pending-interest before)))
      (is (= :recorded (:outcome result)))
      (is (true? (:complete? result)))
      (is (= {:pending-count 0 :errors []} (:delivery result)))
      (is (= -0.20M (get-in result [:settlement/receipt :money/amount])))
      (is (= -0.20M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (= -0.20M (:money/amount (ledger/balance ledger {:account/id "ACC-001"
                                                         :value-through-day 5 :booking-through-day 5}))))
      (is (empty? (authorization/pending-deliveries authorization)))
      (let [debits (filterv #(and (= :interest (:purpose %)) (neg? (:financial/effect %)))
                            (ledger/journal ledger "ACC-001"))]
        (is (= 1 (count debits)))
        (is (= {:financial/effect -0.20M :booking-day 5 :value-day 5}
               (select-keys (first debits) [:financial/effect :booking-day :value-day])))
        (is (= 2 (count (:postings (first debits)))))
        (is (= #{{:book/account "customer/ACC-001" :side :debit :money/amount 0.20M :money/currency :AED}
                 {:book/account "clearing/AED" :side :credit :money/amount 0.20M :money/currency :AED}}
               (set (:postings (first debits))))))
      (is (= (:interest/components before) (:interest/components after)))
      (let [review (yield/calculate! module (historical 6 1 1 "C"))]
        (is (= :recorded (:outcome review)))
        (is (true? (:complete? review))))
      (is (= (:interest/components after) (:interest/components (yield/report module "ACC-001"))))
      (is (= 0.00M (:pending-interest (yield/report module "ACC-001")))))))

(deftest stale-payment-settles-nothing-and-retry-retains-identity
  (let [inject? (atom true)
        {:keys [module submit! authorization submissions]}
        (real-fixture (fn [auth command]
                        (when (compare-and-set! inject? true false)
                          (authorization/submit! auth (fixtures/command "future" :credit 10.00M 3)))
                        (authorization/submit! auth command)))]
    (submit! (fixtures/command "A" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (is (= :retry-required (:outcome (yield/settle! module (settlement "retry" 3 2)))))
    (is (empty? (:settlements (yield/report module "ACC-001"))))
    (is (= 0.04M (:pending-interest (yield/report module "ACC-001"))))
    (is (= :recorded (:outcome (yield/settle! module (settlement "retry" 3 2)))))
    (is (= 1 (count (set (map :transaction/id @submissions)))))
    (is (= [1 2] (mapv :source-event-counter @submissions)))
    (is (= [0.04M 0.04M] (mapv :money/amount @submissions)))
    (is (= 110.04M (:financial-balance (authorization/snapshot authorization "ACC-001"))))))

(deftest zero-settlement-links-zero-components-without-financial-effect
  (let [{:keys [module submit! authorization submissions]} (real-fixture)]
    (submit! (fixtures/command "BHD-debit" :debit 10.000M 1 {:account/id "ACC-002" :money/currency :BHD}))
    (yield/calculate! module (assoc (daily 2) :account/id "ACC-002"))
    (let [before (authorization/snapshot authorization "ACC-002")
          result (yield/settle! module (assoc (settlement "zero" 3 2) :account/id "ACC-002"))]
      (is (= 0.000M (get-in result [:settlement/receipt :money/amount])))
      (is (= 1 (count (get-in result [:settlement/receipt :component/ids]))))
      (is (= before (authorization/snapshot authorization "ACC-002")))
      (is (empty? @submissions))
      (is (= [0.000M] (mapv :money/amount (:fees (yield/report module "ACC-002"))))))))

(deftest zero-cancellation-settles-positive-and-negative-components-once
  (let [{:keys [module submit! authorization submissions]} (real-fixture)]
    (submit! (fixtures/command "A" :credit 250.00M 1))
    (yield/calculate! module (daily 2))
    (submit! (fixtures/command "B" :debit 250.00M 2 {:value-day 1}))
    (yield/calculate! module (historical 3 1 1 "B"))
    (let [before (yield/report module "ACC-001")
          snapshot (authorization/snapshot authorization "ACC-001")
          result (yield/settle! module (settlement "cancel" 4 1))]
      (is (= [0.10M -0.10M] (mapv :money/amount (:interest/components before))))
      (is (= 0.00M (get-in result [:settlement/receipt :money/amount])))
      (is (= 2 (count (get-in result [:settlement/receipt :component/ids]))))
      (is (= snapshot (authorization/snapshot authorization "ACC-001")))
      (is (empty? @submissions))
      (is (= (:interest/components before) (:interest/components (yield/report module "ACC-001")))))
    (yield/calculate! module (historical 5 1 1 "B"))
    (is (= 2 (count (:interest/components (yield/report module "ACC-001")))))))

(deftest reversal-refunds-use-original-charge-dates-and-preserve-other-period-fees
  (let [{:keys [module submit! authorization ledger]} (real-fixture)]
    (submit! (fixtures/command "credit" :credit 10.00M 1))
    (submit! (fixtures/command "debit" :debit 20.00M 1))
    (yield/calculate! module (daily 2))
    (yield/calculate! module (daily 3))
    (submit! (dissoc (fixtures/command "reverse" :reversal nil 3 {:value-day 1 :reversal/of "debit"}) :money/amount))
    (let [before (yield/report module "ACC-001")]
      (yield/calculate! module (historical 4 1 2 "reverse"))
      (let [after (yield/report module "ACC-001")
            refund (last (:fees after))]
        (is (= (:fees before) (subvec (:fees after) 0 2)))
        (is (= [-25.00M 4 2] ((juxt :money/amount :booking-day :value-day) refund)))
        (is (= "reverse" (:cause/transaction-id refund)))
        (is (= [1 2 1] (mapv :reference-day (:fees after))))
        (is (= -15.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
        (is (= -15.00M (:money/amount (ledger/balance ledger {:account/id "ACC-001" :value-through-day 2 :booking-through-day 3}))))
        (is (= 10.00M (:money/amount (ledger/balance ledger {:account/id "ACC-001" :value-through-day 2 :booking-through-day 4}))))
        (yield/calculate! module (historical 4 1 2 "reverse"))
        (is (= after (yield/report module "ACC-001")))))
    ;; The Day 4 refund only becomes calculation input under Day 5's cutoff 4.
    (yield/calculate! module (historical 5 2 2 "reverse"))
    (let [refund (last (:fees (yield/report module "ACC-001")))]
      (is (= [-25.00M 5 3] ((juxt :money/amount :booking-day :value-day) refund)))
      (is (= 10.00M (:financial-balance (authorization/snapshot authorization "ACC-001")))))))

(deftest confirmed-payment-duplicate-retains-original-links-after-lost-response
  (let [lose? (atom true)
        {:keys [module submit! authorization submissions]}
        (real-fixture (fn [auth command]
                        (let [result (authorization/submit! auth command)]
                          (if (compare-and-set! lose? true false)
                            (throw (ex-info "Controlled loss after payment recording" {:test/fault :lost-response}))
                            result))))]
    (submit! (fixtures/command "A" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled loss"
                          (yield/settle! module (settlement "same-payment" 3 2))))
    (is (empty? (:settlements (yield/report module "ACC-001"))))
    (yield/calculate! module (daily 3))
    (let [components (:interest/components (yield/report module "ACC-001"))
          result (yield/settle! module (settlement "same-payment" 3 2))
          report (yield/report module "ACC-001")]
      (is (= (:transaction/id (first @submissions)) (:recovered-from-event result)))
      (is (= [(:component/id (first components))] (get-in result [:settlement/receipt :component/ids])))
      (is (= 0.04M (:pending-interest report)))
      (is (= 100.04M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (= [0.04M] (mapv :money/amount @submissions))))))

(deftest delivered-payment-is-already-settled-before-historical-recalculation
  (let [lose? (atom true)
        {:keys [module submit! authorization submissions]}
        (real-fixture (fn [auth command]
                        (let [result (authorization/submit! auth command)]
                          (if (compare-and-set! lose? true false)
                            (throw (ex-info "Controlled loss after payment recording" {:test/fault :lost-response}))
                            result))))]
    (submit! (fixtures/command "A" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (let [original (first (:interest/components (yield/report module "ACC-001")))]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled loss"
                            (yield/settle! module (settlement "same-payment" 3 2))))
      (submit! (fixtures/command "B" :debit 100.00M 3 {:value-day 1}))
      (yield/calculate! module (historical 4 1 1 "B"))
      ;; Delivery already confirmed the original credit, so only its correction is pending.
      (is (= -0.04M (:pending-interest (yield/report module "ACC-001"))))
      (let [result (yield/settle! module (settlement "same-payment" 5 2))
            view (yield/report module "ACC-001")]
        (is (= [(:component/id original)] (get-in result [:settlement/receipt :component/ids])))
        (is (= 0.04M (:interest-paid view)))
        (is (= -0.04M (:pending-interest view)))
        (is (= 0.04M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
        (is (= 3 (get-in result [:settlement/receipt :run-day])))
        (is (= 1 (count (:settlements view))))
        (is (= 1 (count @submissions)))))))

(deftest lost-fee-response-preserves-original-assessment-before-zero-recalculation
  (let [lose? (atom true)
        {:keys [module submit! authorization]}
        (real-fixture (fn [auth command]
                        (let [result (authorization/submit! auth command)]
                          (if (compare-and-set! lose? true false)
                            (throw (ex-info "Controlled loss after fee recording" {:test/fault :lost-response}))
                            result))))]
    (submit! (fixtures/command "A" :debit 1.00M 1))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Controlled loss"
                          (yield/calculate! module (daily 2))))
    (submit! (fixtures/command "B" :credit 1.00M 1 {:received-day 2}))
    (let [original (first (:fees (yield/report module "ACC-001")))
          result (yield/calculate! module (daily 2))
          view (yield/report module "ACC-001")]
      (is (= 25.00M (:money/amount original)))
      (is (= :recorded (:outcome result)))
      (is (= [25.00M -25.00M] (mapv :money/amount (:fees view))))
      (is (= original (first (:fees view))))
      (is (= "B" (:cause/transaction-id (second (:fees view)))))
      (is (= 0.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))
      (is (= [0.00M] (mapv :money/amount (:interest/components view)))))))

(deftest ordered-interest-review-never-reverts-a-later-input-boundary
  (let [{:keys [module submit! submissions]} (real-fixture)]
    (submit! (fixtures/command "A" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (submit! (fixtures/command "B" :credit 50.00M 2 {:value-day 1}))
    (submit! (fixtures/command "C" :credit 50.00M 2 {:value-day 1}))
    (yield/calculate! module (historical 3 1 1 "B" {:interest-only? true :input-through-event-counter 2}))
    (yield/calculate! module (historical 3 1 1 "C" {:interest-only? true :input-through-event-counter 3}))
    (let [before (yield/report module "ACC-001")]
      (is (= [0.04M 0.02M 0.02M] (mapv :money/amount (:interest/components before))))
      (is (= [1 2 3] (mapv :input-through-event-counter (:interest/components before))))
      (is (= :stale-calculation-view
             (:reason (yield/calculate! module (historical 3 1 1 "B" {:interest-only? true :input-through-event-counter 2})))))
      (is (= before (yield/report module "ACC-001")))
      (is (empty? @submissions)))))

(deftest accepted-payment-with-pending-delivery-does-not-claim-completion
  (let [module (atom nil)
        flush-count (atom 0)
        created (yield/create fixtures/config
                              {:submit-financial!
                               (fn [command]
                                 {:outcome :recorded :transaction/id (:transaction/id command)
                                  :recorded/event (assoc command :event-counter 2 :financial/effect (:money/amount command))})
                               :flush-deliveries!
                               (fn []
                                 (if (>= (swap! flush-count inc) 3)
                                   {:pending-count 1 :errors [{:reason :delivery-failed}]}
                                   {:pending-count 0 :errors []}))})]
    (reset! module created)
    (yield/receive! created (approved "A" 1 100.00M 1))
    (yield/calculate! created (daily 2))
    (let [result (yield/settle! created (settlement "payment" 3 2))]
      (is (= :recorded (:outcome result)))
      (is (false? (:complete? result)))
      (is (= 1 (get-in result [:delivery :pending-count])))
      (is (= 0.04M (:interest-paid (yield/report created "ACC-001")))))))

(deftest stale-fee-attempt-records-no-assessment-and-keeps-its-movement-identity
  (let [inject? (atom true)
        {:keys [module submit! submissions]}
        (real-fixture (fn [auth command]
                        (when (compare-and-set! inject? true false)
                          (authorization/submit! auth (fixtures/command "future-credit" :credit 0.01M 2 {:value-day 1})))
                        (authorization/submit! auth command)))]
    (submit! (fixtures/command "negative" :debit 0.01M 1))
    (is (= :retry-required (:outcome (yield/calculate! module (daily 2)))))
    (is (empty? (:fees (yield/report module "ACC-001"))))
    (is (empty? (:interest/components (yield/report module "ACC-001"))))
    (is (= :recorded (:outcome (yield/calculate! module (daily 2)))))
    (is (= 1 (count (set (map :transaction/id @submissions)))))
    (is (= [1 2] (mapv :source-event-counter @submissions)))
    (is (= [25.00M 25.00M] (mapv :money/amount @submissions)))
    (is (= 1 (count (:fees (yield/report module "ACC-001")))))))

(deftest historical-fees-refresh-and-recompute-each-next-proposal
  (let [{:keys [module submit! submissions]} (real-fixture)]
    (submit! (fixtures/command "positive" :credit 1000.00M 1))
    (doseq [day [2 3 4]] (yield/calculate! module (daily day)))
    (submit! (fixtures/command "late" :debit 1200.00M 4 {:value-day 1}))
    (yield/calculate! module (historical 5 1 3 "late"))
    (is (= [1 2 3] (mapv :reference-day @submissions)))
    (is (= [2 3 4] (mapv :source-event-counter @submissions)))
    (is (= [25.00M 25.00M 25.00M] (mapv :money/amount @submissions)))
    (is (= 5 (:source-event-counter (yield/report module "ACC-001"))))
    (let [before (yield/report module "ACC-001")]
      (yield/calculate! module (historical 5 1 3 "late"))
      (is (= before (yield/report module "ACC-001")))
      (is (= 3 (count @submissions))))))

(deftest new-eligible-arrival-requires-an-explicit-historical-correction
  (let [{:keys [module submit!]} (real-fixture)]
    (submit! (fixtures/command "A" :credit 100.00M 1))
    (yield/calculate! module (daily 2))
    (submit! (fixtures/command "late" :credit 100.00M 1 {:received-day 2}))
    (let [before (yield/report module "ACC-001")]
      (is (= :historical-review-required (:reason (yield/calculate! module (daily 2)))))
      (is (= before (yield/report module "ACC-001"))))
    (yield/calculate! module (historical 2 1 1 "late"))
    (is (= [0.04M 0.04M] (mapv :money/amount (:interest/components (yield/report module "ACC-001")))))))

(deftest pending-interest-is-neither-spendable-nor-an-input-to-next-day-interest
  (let [{:keys [module submit! authorization]} (real-fixture)]
    ;; Adding the unpaid 0.50 would cross the next HALF_UP boundary at 1262.50.
    (submit! (fixtures/command "principal" :credit 1262.49M 1))
    (yield/calculate! module (daily 2))
    (yield/calculate! module (daily 3))
    (let [view (yield/report module "ACC-001")
          snapshot (authorization/snapshot authorization "ACC-001")]
      (is (= [0.50M 0.50M] (mapv :money/amount (:interest/components view))))
      (is (= [1262.49M 1262.49M] (mapv :calculation/base (:interest/components view))))
      (is (= 1.00M (:pending-interest view)))
      (is (= 1262.49M (:financial-balance snapshot) (:available-balance snapshot))))))

(deftest unconfirmed-fee-is-retried-before-replacing-its-calculation
  (let [lose? (atom true)
        {:keys [module submit! authorization submissions]}
        (real-fixture (fn [auth command]
                        (if (compare-and-set! lose? true false)
                          (throw (ex-info "Fee request did not reach Authorization" {}))
                          (authorization/submit! auth command))))]
    (submit! (fixtures/command "overdraft" :debit 1.00M 1))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"did not reach"
                          (yield/calculate! module (daily 2))))
    (let [original (first @submissions)]
      (is (= [original] (:financial-intents (yield/report module "ACC-001")))))
    ;; New eligible principal makes the recalculated fee zero, but does not
    ;; establish whether the original command was recorded by its recipient.
    (submit! (fixtures/command "late-credit" :credit 1.00M 1 {:received-day 2}))
    (is (= :stale-source (:reason (yield/calculate! module (daily 2)))))
    (is (= 2 (count @submissions)))
    (is (= (first @submissions) (second @submissions)))
    (is (empty? (:pending-financial-commands (yield/report module "ACC-001"))))
    (is (= :recorded (:outcome (yield/calculate! module (daily 2)))))
    (is (= [0.00M] (mapv :money/amount (:fees (yield/report module "ACC-001")))))
    (is (= 0.00M (:financial-balance (authorization/snapshot authorization "ACC-001"))))))
