(ns mockery.views
  (:require [selmer.parser :as p]))

;; TODO: use actual date fns
(defn defaults []
  {:ref-num "666"
   :date "20181006"
   :time "162000"})

(defn render-card-response [template params]
  (let [updated-params (merge (defaults) params)]
    (p/render-file (str "card_responses/" template ".xml")
                   updated-params)))
