(ns mockery.views
  (:require [selmer.parser :as p]))

;; generates a random 6-digit numeric string
(defn random-ref []
  (clojure.string/join
   (take 6 (repeatedly #(rand-int 9)))))

(defn render-card-response [template params]
  (let [updated-params (merge {:ref-num (random-ref)}
                              params)]
    (p/render-file (str "card_responses/" template ".xml")
                   updated-params)))
