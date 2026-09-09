(ns account-ledger.fixtures)

(def config
  {:accounts {"ACC-001" {:money/currency :AED :opening-balance 0.00M :account/type :aed-standard}
              "ACC-002" {:money/currency :BHD :opening-balance 0.000M :account/type :bhd-exempt}}
   :account-types {:aed-standard {:money/currency :AED :daily-fee 25.00M}
                   :bhd-exempt {:money/currency :BHD :daily-fee 0.000M}}})

(defn command [id type amount day & [extra]]
  (merge {:transaction/id id :transaction/type type :account/id "ACC-001"
          :money/currency :AED :money/amount amount
          :booking-day day :value-day day :received-day day} extra))

(def events
  [(command "E1" :credit 1200.00M 1)
   (command "E2" :debit 950.00M 1)
   (command "E3" :authorization 200.00M 2 {:authorization/id "Auth-A"})
   (command "E4" :credit 400.00M 3)
   (command "E5" :settlement 185.00M 4 {:authorization/id "Auth-A" :settlement/final? true})
   (command "E6" :settlement 180.00M 4 {:authorization/id "Auth-Z" :settlement/final? true})
   (command "E7" :debit 620.00M 5 {:value-day 2})
   (command "E8" :authorization 90.00M 5 {:authorization/id "Auth-B"})
   (dissoc (command "E9" :reversal nil 6 {:value-day 2 :reversal/of "E7"}) :money/amount)
   (command "E10" :credit 10.000M 5 {:account/id "ACC-002" :money/currency :BHD
                                    :received-day 6 :installment-count 3})])
