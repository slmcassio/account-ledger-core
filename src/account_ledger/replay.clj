(ns account-ledger.replay
  "The supplied exercise, with explicit logical checkpoints and a separate Day 7."
  (:require [account-ledger.system :as system]))

(def config
  {:accounts {"ACC-001" {:money/currency :AED :opening-balance 0.00M :account/type :aed-standard}
              "ACC-002" {:money/currency :BHD :opening-balance 0.000M :account/type :bhd-exempt}}
   :account-types {:aed-standard {:money/currency :AED :daily-fee 25.00M}
                   :bhd-exempt {:money/currency :BHD :daily-fee 0.000M}}})

(defn- command [id type amount day extra]
  (merge {:transaction/id id :transaction/type type :account/id "ACC-001"
          :money/currency :AED :money/amount amount :booking-day day :value-day day :received-day day}
         extra))

(def events
  [(command "E1" :credit 1200.00M 1 {})
   (command "E2" :debit 950.00M 1 {})
   (command "E3" :authorization 200.00M 2 {:authorization/id "Auth-A"})
   (command "E4" :credit 400.00M 3 {})
   (command "E5" :settlement 185.00M 4 {:authorization/id "Auth-A" :settlement/final? true})
   (command "E6" :settlement 180.00M 4 {:authorization/id "Auth-Z" :settlement/final? true})
   (command "E7" :debit 620.00M 5 {:value-day 2})
   (command "E8" :authorization 90.00M 5 {:authorization/id "Auth-B"})
   (dissoc (command "E9" :reversal nil 6 {:value-day 2 :reversal/of "E7"}) :money/amount)
   (command "E10" :credit 10.000M 5 {:account/id "ACC-002" :money/currency :BHD
                                    :received-day 6 :installment-count 3})])

(defn- require-success [result]
  (when-not (#{:recorded :duplicate :declined} (:outcome result))
    (throw (ex-info "Replay operation did not complete" {:result result})))
  result)

(defn- drain-completely! [app]
  (let [result (system/drain! app)]
    (when-not (and (zero? (:pending-count result)) (empty? (:errors result)))
      (throw (ex-info "Replay has pending delivery" result)))))

(defn- daily! [app day]
  (doseq [id (sort (keys (:accounts config)))]
    (require-success (system/run-day! app {:account/id id :run-day day :reference-day (dec day)
                                           :booking-cutoff (dec day) :mode :daily}))))

(defn- review! [app id day from through cause]
  (require-success (system/run-day! app {:account/id id :run-day day :booking-cutoff (dec day)
                                         :mode :historical :from-day from :through-day through
                                         :cause/transaction-id cause})))

(defn- reports [app day]
  (drain-completely! app)
  (mapv (fn [id]
          (let [report (system/report app {:account/id id :day day})]
            (when-not (:complete? report)
              (throw (ex-info "Incomplete replay report" {:account/id id :day day})))
            report)) (sort (keys (:accounts config)))))

(defn run []
  (let [app (system/create config)
        checkpoints {1 [0 1] 2 [2] 3 [3] 4 [4 5] 5 [6 7]}
        early-reports
        (reduce (fn [captured day]
                  (when (> day 1) (daily! app day))
                  (doseq [position (checkpoints day)]
                    (require-success (system/submit! app (nth events position))))
                  (into captured (reports app day))) [] (range 1 6))]
    (review! app "ACC-001" 6 2 4 "E7")
    (daily! app 6)
    (require-success (system/submit! app (nth events 8)))
    (require-success (system/submit! app (nth events 9)))
    (drain-completely! app)
    (review! app "ACC-002" 6 5 5 "E10")
    (doseq [id (sort (keys (:accounts config)))]
      (require-success (system/settle! app {:account/id id :settlement/id (str "month-one/" id)
                                             :run-day 6 :period/end-day 5 :booking-cutoff 5})))
    (let [final-reports (reports app 6)]
      {:system app :daily-reports (into early-reports final-reports)
       :summary (into (sorted-map) (map (juxt :account/id identity) final-reports))})))

(defn continue-day-seven [app]
  (review! app "ACC-001" 7 2 5 "E9")
  (daily! app 7)
  (let [views (reports app 7)]
    {:day 7 :reports views :summary (into (sorted-map) (map (juxt :account/id identity) views))}))

(defn- display-report [report]
  (println (str "Day " (:day report) " | " (:account/id report) " | " (name (:money/currency report))))
  (println "  Financial:" (:financial-balance report) "Held:" (:held-amount report)
           "Available:" (:available-balance report) "Counter:" (:last-event-counter report))
  (println "  Interest paid:" (:interest-paid report) "Pending:" (:pending-interest report))
  (println "  Authorizations:" (pr-str (:authorization-states report)) "Active holds:" (pr-str (:holds report)))
  (println "  Fee history (reference / booking / value / signed charge):"
           (pr-str (mapv #(select-keys % [:reference-day :booking-day :value-day :money/amount]) (:fees report))))
  (println "  Occurrences:" (pr-str (:occurrences report)))
  (when (= "ACC-002" (:account/id report))
    (println "  Installments:" (pr-str (mapv :installments (:journal report)))))
  (println "  Complete internal delivery:" (:complete? report)))

(defn -main [& _]
  (let [{:keys [system daily-reports]} (run)]
    (println "Six-day operational reports captured at each checkpoint")
    (doseq [report daily-reports] (display-report report))
    (println "Separate Day 7 continuation. Earlier Day 6 reports remain unchanged.")
    (doseq [report (:reports (continue-day-seven system))] (display-report report))))
