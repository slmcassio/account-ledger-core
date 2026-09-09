(ns account-ledger.unit.authorization-test
  (:require [clojure.test :refer [deftest is testing]]
            [account-ledger.authorization.domain :as domain]
            [account-ledger.contracts :as contracts]
            [account-ledger.fixtures :as fixtures]))

(defn initial [] (domain/initial-state (contracts/normalize-config fixtures/config)))

(defn apply-commands [state commands]
  (reduce (fn [s command] (first (domain/submit s command))) state commands))

(deftest principal-hold-and-final-settlement
  (let [opening (initial)
        held (apply-commands opening (take 3 fixtures/events))
        credited (apply-commands held [(nth fixtures/events 3)])
        settled (apply-commands credited [(nth fixtures/events 4)])]
    (is (= {:financial-balance 0.00M :held-amount 0.00M :available-balance 0.00M
            :last-event-counter 0}
           (select-keys (get-in opening [:accounts "ACC-001" :snapshot])
                        [:financial-balance :held-amount :available-balance :last-event-counter])))
    (is (= [250.00M 200.00M 50.00M 3]
           ((juxt :financial-balance :held-amount :available-balance :last-event-counter)
            (get-in held [:accounts "ACC-001" :snapshot]))))
    (is (= 450.00M (get-in credited [:accounts "ACC-001" :snapshot :available-balance])))
    (is (= [465.00M 0.00M 465.00M 5]
           ((juxt :financial-balance :held-amount :available-balance :last-event-counter)
            (get-in settled [:accounts "ACC-001" :snapshot]))))
    (is (= {} (get-in settled [:accounts "ACC-001" :snapshot :holds])))
    (let [event (get-in settled [:identities "E5" :recorded/event])]
      (is (= -185.00M (:financial/effect event)))
      (is (= :settled (:authorization/state event)))
      (is (= 15.00M (:hold/released event))))))

(deftest hold-boundary-and-decline-recording
  (doseq [[account-id currency funds over zero minor-unit]
          [["ACC-001" :AED 20.00M 20.01M 0.00M 0.01M]
           ["ACC-002" :BHD 20.000M 20.001M 0.000M 0.001M]]]
    (testing (str currency " uses its own minor unit at the availability boundary")
      (let [account {:account/id account-id :money/currency currency}
            funded (apply-commands (initial) [(fixtures/command "fund" :credit funds 1 account)])
            [exact approved] (domain/submit funded
                                            (fixtures/command "exact" :authorization funds 1
                                                              (assoc account :authorization/id "exact-hold")))
            declined-command (fixtures/command "over" :authorization over 1
                                               (assoc account :authorization/id "over-hold"))
            [declined refused] (domain/submit funded declined-command)]
        (is (= :recorded (:outcome approved)))
        (is (= [funds funds zero 2 {"exact-hold" funds}]
               ((juxt :financial-balance :held-amount :available-balance :last-event-counter :holds)
                (get-in exact [:accounts account-id :snapshot]))))
        (is (= :declined (:outcome refused)))
        (is (= :insufficient-available-balance (:reason refused)))
        (is (= (:accounts funded) (update-in (:accounts declined) [account-id :history] pop)))
        (is (nil? (:recorded/snapshot refused)))
        (is (nil? (:recorded/event refused)))
        (is (= (conj (get-in funded [:accounts account-id :history])
                     (assoc refused :command declined-command))
               (get-in declined [:accounts account-id :history])))
        (is (= (select-keys funded [:deliveries :delivery-order :acknowledged])
               (select-keys declined [:deliveries :delivery-order :acknowledged])))
        (is (= refused (get-in declined [:identities "over"])))
        (let [more-funds (apply-commands declined [(fixtures/command "later" :credit 100M 2 account)])
              [unchanged duplicate] (domain/submit more-funds {:transaction/id "over" :money/amount :malformed})]
          (is (= :duplicate (:outcome duplicate)))
          (is (= refused (:original/result duplicate)))
          (is (= more-funds unchanged))
          (is (= 120M (get-in unchanged [:accounts account-id :snapshot :available-balance])))))
      (testing "zero funds cannot support even the smallest hold"
        (let [before (initial)
              command (fixtures/command "unfunded" :authorization minor-unit 1
                                        {:account/id account-id :money/currency currency
                                         :authorization/id "unfunded-hold"})
              [after result] (domain/submit before command)]
          (is (= :declined (:outcome result)))
          (is (= :insufficient-available-balance (:reason result)))
          (is (= (:accounts before) (update-in (:accounts after) [account-id :history] pop)))
          (is (= [{:outcome :declined :reason :insufficient-available-balance
                   :transaction/id "unfunded" :account/id account-id
                   :authorization/state :declined :occurrences [] :command command}]
                 (get-in after [:accounts account-id :history])))
          (is (= (select-keys before [:deliveries :delivery-order :acknowledged])
                 (select-keys after [:deliveries :delivery-order :acknowledged])))
          (is (= result (get-in after [:identities "unfunded"])))
          (is (nil? (:recorded/event result)))
          (is (nil? (:recorded/snapshot result))))))))

(deftest known-identity-precedes-every-payload-check
  (let [[recorded original] (domain/submit (initial) (fixtures/command "same" :credit 10.00M 1))]
    (doseq [duplicate [{:transaction/id "same"}
                       {:transaction/id "same" :account/id "ACC-002" :money/currency :BHD
                        :transaction/type :credit :money/amount 999.999M}
                       {:transaction/id "same" :transaction/type :reversal :reversal/of "missing"}
                       {:transaction/id "same" :source-event-counter -10 :money/amount :invalid}]]
      (let [[state result] (domain/submit recorded duplicate)]
        (is (= recorded state))
        (is (= {:outcome :duplicate :transaction/id "same" :account/id "ACC-001"
                :original/result original} result))))))

(deftest partial-settlement-and-explicit-release
  (let [funded (apply-commands (initial)
                               [(fixtures/command "fund" :credit 50.00M 1)
                                (fixtures/command "hold" :authorization 20.00M 1 {:authorization/id "A"})])
        [partial result] (domain/submit funded (fixtures/command "partial" :settlement 15.00M 1
                                                                {:authorization/id "A" :settlement/final? false}))
        [released release-result] (domain/submit partial (fixtures/command "release" :release 5.00M 1
                                                                          {:authorization/id "A"}))]
    (is (= [35.00M 5.00M 30.00M]
           ((juxt :financial-balance :held-amount :available-balance)
            (get-in partial [:accounts "ACC-001" :snapshot]))))
    (is (= :partially-settled (get-in result [:recorded/event :authorization/state])))
    (is (= 5.00M (get-in result [:recorded/event :hold/remaining])))
    (is (= :recorded (:outcome release-result)))
    (is (= 0.00M (get-in release-result [:recorded/event :financial/effect])))
    (is (= :released (get-in release-result [:recorded/event :authorization/state])))
    (is (= [35.00M 0.00M 35.00M 4]
           ((juxt :financial-balance :held-amount :available-balance :last-event-counter)
            (get-in released [:accounts "ACC-001" :snapshot]))))
    (is (= [:yield]
           (map :destination (filter #(= "release" (get-in % [:event :transaction/id]))
                                     (vals (:deliveries released))))))
    (let [[unchanged invalid] (domain/submit partial (fixtures/command "over-release" :release 5.01M 1
                                                                                      {:authorization/id "A"}))]
      (is (= :invalid (:outcome invalid)))
      (is (= :insufficient-hold (:reason invalid)))
      (is (= partial unchanged)))))

(deftest confirmed-settlement-debits-without-sufficient-funds-or-live-hold
  (let [[unmatched result] (domain/submit (initial) (fixtures/command "missing" :settlement 180.00M 1
                                                                                    {:authorization/id "Z" :settlement/final? true}))]
    (is (= :recorded (:outcome result)))
    (is (= -180.00M (get-in unmatched [:accounts "ACC-001" :snapshot :financial-balance])))
    (is (= [{:type :missing-authorization :authorization/id "Z"}] (:occurrences result)))
    (is (= {} (get-in unmatched [:accounts "ACC-001" :snapshot :holds]))))
  (let [held (apply-commands (initial) [(fixtures/command "fund" :credit 20.00M 1)
                                       (fixtures/command "hold" :authorization 20.00M 1 {:authorization/id "A"})])
        [settled result] (domain/submit held (fixtures/command "larger" :settlement 30.00M 1
                                                               {:authorization/id "A" :settlement/final? false}))]
    (is (= :recorded (:outcome result)))
    (is (= [-10.00M 0.00M -10.00M]
           ((juxt :financial-balance :held-amount :available-balance)
            (get-in settled [:accounts "ACC-001" :snapshot]))))
    (is (= [] (:occurrences result)))
    (let [[again recorded] (domain/submit settled (fixtures/command "after-settled" :settlement 2.00M 1
                                                                                        {:authorization/id "A" :settlement/final? true}))]
      (is (= :recorded (:outcome recorded)))
      (is (= -12.00M (get-in again [:accounts "ACC-001" :snapshot :financial-balance])))
      (is (= [{:type :inactive-authorization :authorization/id "A"}] (:occurrences recorded))))))

(defn financial-command [id amount source]
  (fixtures/command id :credit amount 6
                    {:purpose :interest :reference-day 5 :component/ids ["interest/day5"]
                     :source-event-counter source :settlement/id "period-one"
                     :period/end-day 5 :booking-cutoff 5}))

(deftest source-version-is-account-specific-and-unrecorded-attempts-retain-identity
  (let [funded (apply-commands (initial) [(fixtures/command "fund" :credit 10.00M 1)])
        [stale result] (domain/submit funded (financial-command "system/payment" 1.00M 0))]
    (is (= funded stale))
    (is (= :retry-required (:outcome result)))
    (is (= :stale-source (:reason result)))
    (let [declined (apply-commands stale [(fixtures/command "decline" :authorization 10.01M 2
                                                                          {:authorization/id "declined"})])
          other (apply-commands declined [(fixtures/command "other" :credit 3.000M 2
                                                                             {:account/id "ACC-002" :money/currency :BHD})])
          [paid result] (domain/submit other (financial-command "system/payment" 0.10M 1))
          event (:recorded/event result)
          [again duplicate] (domain/submit paid {:transaction/id "system/payment" :source-event-counter 0
                                                 :money/amount 999.00M :component/ids ["new-component"]})]
      (is (= :recorded (:outcome result)))
      (is (= 10.10M (get-in paid [:accounts "ACC-001" :snapshot :financial-balance])))
      (is (= 2 (:event-counter event)))
      (is (= 1 (:source-event-counter event)))
      (is (= :interest (:purpose event)))
      (is (= ["interest/day5"] (:component/ids event)))
      (is (= :duplicate (:outcome duplicate)))
      (is (= result (:original/result duplicate)))
      (is (= paid again)))))

(deftest interest-without-receipt-metadata-cannot-record-effects-or-reserve-identity
  (let [before (initial)
        command (financial-command "system/payment" 0.10M 0)]
    (doseq [key [:settlement/id :period/end-day :booking-cutoff]]
      (let [invalid (dissoc command key)
            [unchanged rejected] (domain/submit before invalid)]
        (is (= :invalid (:outcome rejected)))
        (is (= :invalid-interest-settlement (:reason rejected)))
        (is (= before unchanged))
        (let [[recorded result] (domain/submit unchanged command)
              [duplicate-state duplicate] (domain/submit recorded invalid)]
          (is (= :recorded (:outcome result)))
          (is (= 0.10M (get-in recorded [:accounts "ACC-001" :snapshot :financial-balance])))
          (is (= (select-keys command [:settlement/id :period/end-day :booking-cutoff])
                 (select-keys (:recorded/event result) [:settlement/id :period/end-day :booking-cutoff])))
          (is (= :duplicate (:outcome duplicate)))
          (is (= result (:original/result duplicate)))
          (is (= recorded duplicate-state)))))))

(deftest reversal-restores-principal-once-with-supplied-dates
  (let [state (apply-commands (initial) (take 8 fixtures/events))
        [reversed result] (domain/submit state (nth fixtures/events 8))
        event (:recorded/event result)]
    (is (= :recorded (:outcome result)))
    (is (= ["E9" "E7" 620.00M 620.00M 6 2 6]
           ((juxt :transaction/id :reversal/of :money/amount :financial/effect
                  :booking-day :value-day :received-day) event)))
    (is (= 285.00M (get-in reversed [:accounts "ACC-001" :snapshot :financial-balance])))
    (is (= 8 (get-in reversed [:accounts "ACC-001" :snapshot :last-event-counter])))
    (is (= :declined (get-in reversed [:identities "E8" :outcome])))
    (let [[same duplicate] (domain/submit reversed {:transaction/id "E9"})
          [unchanged repeated] (domain/submit reversed (assoc (nth fixtures/events 8) :transaction/id "again"))]
      (is (= :duplicate (:outcome duplicate)))
      (is (= reversed same unchanged))
      (is (= :invalid (:outcome repeated)))
      (is (= :already-reversed (:reason repeated))))
    (doseq [[command reason]
            [[(assoc (nth fixtures/events 8) :money/amount 619.99M) :reversal-amount-mismatch]
             [(assoc (nth fixtures/events 8) :reversal/of "absent") :unknown-reversal-reference]
             [(assoc (nth fixtures/events 8) :reversal/of "E3") :not-principal-movement]
             [(assoc (nth fixtures/events 8) :account/id "ACC-002" :money/currency :BHD) :reversal-account-mismatch]]]
      (let [[unchanged result] (domain/submit state command)]
        (is (= :invalid (:outcome result)))
        (is (= reason (:reason result)))
        (is (= state unchanged))))))

(deftest unrelated-credit-metadata-cannot-consume-a-principal-reversal
  (let [credited (apply-commands (initial) [(fixtures/command "A" :credit 100.00M 1)])
        [before credit-result] (domain/submit credited
                                               (fixtures/command "B" :credit 1.00M 1 {:reversal/of "A"}))
        reversal (dissoc (fixtures/command "R" :reversal nil 2 {:reversal/of "A"}) :money/amount)
        [reversed result] (domain/submit before reversal)
        [unchanged repeated] (domain/submit reversed (assoc reversal :transaction/id "R-again"))]
    (is (= :recorded (:outcome credit-result)))
    (is (= :credit (get-in credit-result [:recorded/event :transaction/type])))
    (is (= 101.00M (get-in before [:accounts "ACC-001" :snapshot :financial-balance])))
    (is (= :recorded (:outcome result)))
    (is (= -100.00M (get-in result [:recorded/event :financial/effect])))
    (is (= [1.00M 3]
           ((juxt :financial-balance :last-event-counter)
            (get-in reversed [:accounts "ACC-001" :snapshot]))))
    (is (= :invalid (:outcome repeated)))
    (is (= :already-reversed (:reason repeated)))
    (is (= reversed unchanged))))

(deftest installment-credit-is-one-financial-effect-and-one-snapshot
  (let [[state result] (domain/submit (initial) (last fixtures/events))
        event (:recorded/event result)]
    (is (= :recorded (:outcome result)))
    (is (= [3.333M 3.333M 3.334M] (:installments event)))
    (is (= 10.000M (:financial/effect event)))
    (is (= 10.000M (get-in state [:accounts "ACC-002" :snapshot :financial-balance])))
    (is (= 1 (:event-counter event)))
    (is (= 1 (count (get-in state [:accounts "ACC-002" :history]))))
    (is (= 2 (count (:deliveries state))))))

(deftest release-can-free-a-partial-amount-without-posting-money
  (let [held (apply-commands (initial)
                             [(fixtures/command "fund" :credit 20.00M 1)
                              (fixtures/command "hold" :authorization 20.00M 1 {:authorization/id "A"})])
        [partial result] (domain/submit held (fixtures/command "release-part" :release 5.00M 1
                                                                {:authorization/id "A"}))
        [released _] (domain/submit partial (fixtures/command "release-rest" :release 15.00M 1
                                                                  {:authorization/id "A"}))]
    (is (= :partially-released (get-in result [:recorded/event :authorization/state])))
    (is (= [20.00M 15.00M 5.00M]
           ((juxt :financial-balance :held-amount :available-balance)
            (get-in partial [:accounts "ACC-001" :snapshot]))))
    (doseq [[state command reason]
            [[released (fixtures/command "inactive" :release 1.00M 1 {:authorization/id "A"}) :inactive-authorization]
             [held (fixtures/command "unknown" :release 1.00M 1 {:authorization/id "unknown"}) :unknown-authorization]
             [held (fixtures/command "reuse" :authorization 1.00M 1 {:authorization/id "A"}) :authorization-exists]]]
      (let [[unchanged result] (domain/submit state command)]
        (is (= :invalid (:outcome result)))
        (is (= reason (:reason result)))
        (is (= state unchanged))))
    (let [[settled result] (domain/submit released (fixtures/command "after-release" :settlement 25.00M 1
                                                                                        {:authorization/id "A" :settlement/final? true}))]
      (is (= -5.00M (get-in settled [:accounts "ACC-001" :snapshot :financial-balance])))
      (is (= [{:type :inactive-authorization :authorization/id "A"}] (:occurrences result))))))

(deftest reversal-of-credit-and-optional-exact-amount
  (let [[credit _] (domain/submit (initial) (fixtures/command "credit" :credit 10.000M 1
                                                                          {:account/id "ACC-002" :money/currency :BHD}))
        reversal (fixtures/command "reverse" :reversal 10.0000M 2
                                   {:account/id "ACC-002" :money/currency :BHD :value-day 1 :reversal/of "credit"})
        [reversed result] (domain/submit credit reversal)]
    (is (= :recorded (:outcome result)))
    (is (= -10.000M (get-in result [:recorded/event :financial/effect])))
    (is (= 10.000M (get-in result [:recorded/event :money/amount])))
    (is (= 0.000M (get-in reversed [:accounts "ACC-002" :snapshot :financial-balance])))
    (doseq [[amount reason] [[10.0001M :excess-precision] [10.0 :invalid-amount] [nil :invalid-amount]]]
      (let [[unchanged result] (domain/submit credit (assoc reversal :money/amount amount))]
        (is (= credit unchanged))
        (is (= :invalid (:outcome result)))
        (is (= reason (:reason result)))))))

(defn- fee-command []
  (fixtures/command "system/fee" :debit 25.0M 6
                    {:purpose :fee :reference-day 2 :source-event-counter 0
                     :component/ids ["fee/day2"] :fee/difference 25.00M
                     :cause/transaction-id "late-debit" :fee/original-value-day 6
                     :fee/assessment {:component/id "fee/day2" :component/type :adjustment
                                      :reference-day 2 :booking-day 6 :value-day 6
                                      :money/amount 25.00M :source-event-counter 0
                                      :booking-cutoff 5 :cause/transaction-id "late-debit"}}))

(deftest financial-debit-preserves-fee-contract-even-into-negative-funds
  (let [command (fee-command)
        [state result] (domain/submit (initial) command)
        event (:recorded/event result)]
    (is (= :recorded (:outcome result)))
    (is (= -25.00M (:financial/effect event)))
    (is (= 2 (.scale ^BigDecimal (:money/amount event))))
    (is (= -25.00M (get-in state [:accounts "ACC-001" :snapshot :available-balance])))
    (is (= (select-keys command [:purpose :reference-day :source-event-counter :component/ids
                                 :fee/difference :fee/assessment :cause/transaction-id :fee/original-value-day])
           (select-keys event [:purpose :reference-day :source-event-counter :component/ids
                              :fee/difference :fee/assessment :cause/transaction-id :fee/original-value-day])))
    (let [reversal (dissoc (fixtures/command "reverse-fee" :reversal nil 7 {:reversal/of "system/fee"}) :money/amount)
          [unchanged rejected] (domain/submit state reversal)]
      (is (= state unchanged))
      (is (= :not-principal-movement (:reason rejected))))))

(deftest invalid-fee-assessments-cannot-record-effects-or-reserve-identity
  (let [before (initial)
        command (fee-command)]
    (doseq [invalid [(dissoc command :fee/assessment)
                     (assoc-in command [:fee/assessment :money/amount] 24.00M)
                     (assoc-in command [:fee/assessment :component/id] "unlinked-fee")]]
      (let [[unchanged rejected] (domain/submit before invalid)]
        (is (= :invalid (:outcome rejected)))
        (is (= :invalid-fee-assessment (:reason rejected)))
        (is (= before unchanged))
        (let [[recorded result] (domain/submit unchanged command)
              [duplicate-state duplicate] (domain/submit recorded invalid)]
          (is (= :recorded (:outcome result)))
          (is (= -25.00M (get-in recorded [:accounts "ACC-001" :snapshot :financial-balance])))
          (is (= :duplicate (:outcome duplicate)))
          (is (= result (:original/result duplicate)))
          (is (= recorded duplicate-state)))))))
