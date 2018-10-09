(ns mockery.models)

(def status-codes
  {:stat-inq        {:4001 "Card Is Active"
                     :4002 "Card Is Deactive"
                     :4003 "Card Is Redeemed"
                     :4006 "Card Not Found"
                     :4007 "Card Expired"}
   :redeem          {:0000 "Success"
                     :0043 "Card Is Invalid"
                     :0046 "Card Is Deactive"
                     :0038 "Card Is Redeemed"}
   :redeem-reversal {:0000 "Success"
                     :0067 "Not Reversible"}
   :all             {:0014 "Routing Error"
                     :0016 "Database Error"
                     :0019 "PIN Not found"
                     :0020 "Invalid PIN"
                     :0022 "MDN Not Found"
                     :0035 "Server Error"
                     :0036 "Server Error"
                     :0037 "Server Error"
                     :0082 "Invalid Identity Number"}})

(def cards
  {:default {:product-face-value 30.0
             :product-upc "123456789"
             :activation-date "2018-10-01 22:00:00"
             :serial-number "987654321"}
   :1234666 {:product-face-value 66.6
             :product-upc "312654987"
             :activation-date "2017-10-31 03:33:33"
             :serial-number "918273465"}})

(defn match-status-code [use-case card-number]
  (let [conditions (merge (:all status-codes)
                          (use-case status-codes))]
    (first (filter
            (fn [[code _]]
              (clojure.string/ends-with? card-number
                                         (name code)))
            conditions))))

(defn match-card-number [use-case card-number]
  (let [status-data (if-let [sc (match-status-code use-case card-number)]
                      (zipmap [:resp-code :resp-msg] sc)
                      {:resp-code :0000 :resp-msg "Success"})
        card-data (get cards (keyword card-number) (:default cards))]
    (merge status-data card-data)))
