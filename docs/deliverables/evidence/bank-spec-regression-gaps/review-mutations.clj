;; Reproduce the four review mutations in separate JVMs without editing source.
;; Each invocation runs the real normal suite and must exit 1 with failed assertions.
(require '[account-ledger.authorization.domain :as authorization]
         '[account-ledger.ledger.api :as ledger]
         '[account-ledger.system :as system]
         '[account-ledger.yield-fees.api]
         '[account-ledger.runner :as runner])

(let [mutation (first *command-line-args*)
      post ledger/post!
      submit authorization/submit
      report system/report
      replacements
      (case mutation
        "negative-ledger"
        {#'ledger/post!
         (fn [module event]
           (if (and (= :interest (:purpose event)) (neg? (:financial/effect event)))
             {:outcome :invalid :reason :controlled-negative-interest-rejection}
             (post module event)))}

        "duplicate-response"
        {(ns-resolve 'account-ledger.yield-fees.api 'original-event)
         (fn [result]
           (when (= :recorded (:outcome result)) (:recorded/event result)))}

        "bhd-availability"
        {#'authorization/submit
         (fn [state command]
           (submit (if (and (= :authorization (:transaction/type command))
                            (= :BHD (:money/currency command)))
                     (assoc-in state [:accounts (:account/id command) :snapshot :available-balance] 1000000M)
                     state)
                   command))}

        "fabricated-occurrences"
        {#'system/report
         (fn [app query]
           (assoc (report app query) :occurrences [{:type :wrong-error :authorization/id "Wrong-ID"}]))}

        (throw (ex-info "Unknown review mutation" {:mutation mutation})))]
  (println "Review mutation:" mutation)
  (with-redefs-fn replacements #(runner/-main)))
