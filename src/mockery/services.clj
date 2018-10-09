(ns mockery.services
  (:require [clojure.xml :as xml]
            [clojure.data.xml :as data-xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]
            [mockery.views :as views]
            [mockery.models :as models]))

;; Inspired by:
;; http://blog.korny.info/2014/03/08/xml-for-fun-and-profit.html#parsing-wikipedia
;; http://clojure-doc.org/articles/tutorials/parsing_xml_with_zippers.html

(defn xml-zipper [str]
   (-> str
     data-xml/parse-str
     zip/xml-zip))


;; This convenience macro refactors away a bunch of very similar methods
;; taken from the xml zippers guide:
;; https://github.com/clojuredocs/guides/blob/master/articles/tutorials/parsing_xml_with_zippers.md
;; for example:
;; (macroexpand-1 '(get-text-at-path "TransferredValueTxn/TransferredValueTxnReq/EchoData" a))
;; returns
;; (clojure.data.zip.xml/xml1-> (mockery.services/xml-zipper a) :TransferredValueTxn :TransferredValueTxnReq :EchoData clojure.data.zip.xml/text)
;; that is, it takes the given document, turns it into a zipper
;; and traverses it until it gets the text of the last predicate

(defmacro get-text-at-path [path xml-doc]
  (let [segments (map keyword (clojure.string/split path #"/"))]
    `(zip-xml/xml1->
      (xml-zipper ~xml-doc)
      ~@segments
      zip-xml/text)))

(defn get-request-category [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/ReqCat"
   xml-req))

(defn get-request-action [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/ReqAction"
   xml-req))

(defn get-card-number [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/CardActionInfo/PIN"
   xml-req))

(defn get-echo-data [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/EchoData"
   xml-req))

(defn get-date [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/Date"
   xml-req))

(defn get-time [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/Time"
   xml-req))

(defn get-account [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/CardActionInfo/AcctNum"
   xml-req))

(defn get-ref [xml-req]
  (get-text-at-path
   "TransferredValueTxn/TransferredValueTxnReq/CardActionInfo/SrcRefNum"
   xml-req))

(defn get-tv-action [xml-req]
  (if
   (= "TransferredValue" (get-request-category xml-req))
   (get-request-action xml-req)))

;; takes a clojure symbol and returns a CamelCased string
;; e.g. (case->action :status-inq)
;; => StatusInq
(defn camelize [use-case]
  (-> use-case
      name
      (#(clojure.string/split % #"-"))
      (#(map  clojure.string/capitalize %))
      (#(clojure.string/join %))))

(defn respond [use-case card-number xml-req]
  (let [card-data   (models/match-card-number
                      use-case card-number)
        client-date (get-date xml-req)
        client-time (get-time xml-req)
        account     (get-account xml-req)
        ref-num     (get-ref xml-req)
        action-name (camelize use-case)]
    (views/render-card-response
     "card-action"
     (merge card-data
            {:pin card-number
             :acct-num account
             :src-ref-num ref-num
             :action action-name
             :given-date client-date
             :given-time client-time}))))

(defn bad-request [xml-req]
  (let [client-date (get-date xml-req)
        client-time (get-time xml-req)]
    (views/render-card-response
     "bad-request"
     {:given-date client-date
      :given-time client-time})))

(defn card-action [use-case xml-req]
  (if-let [card-number (get-card-number xml-req)]
    (respond use-case card-number xml-req)
    (bad-request xml-req)))

(defn echo [xml-req]
  (let [echo-data (get-echo-data xml-req)
        client-date (get-date xml-req)
        client-time (get-time xml-req)]
    (views/render-card-response "echo"
                                {:echo-data echo-data
                                 :given-date client-date
                                 :given-time client-time})))

(defn handle-card-request [xml-req]
  (cond
   (= "Redeem" (get-tv-action xml-req))
   (card-action :redeem xml-req)
   (= "RedeemReversal" (get-tv-action xml-req))
   (card-action :redeem-reversal xml-req)
   (= "StatInq" (get-tv-action xml-req))
   (card-action :stat-inq xml-req)
   (= "Echo" (get-tv-action xml-req))
   (echo xml-req)
   :else (bad-request xml-req)))
