(ns mockery.services
  (:require [clojure.xml :as xml]
            [clojure.data.xml :as data-xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]))

;; Inspired by:
;; https://github.com/clojuredocs/guides/blob/master/articles/tutorials/parsing_xml_with_zippers.md
;; http://blog.korny.info/2014/03/08/xml-for-fun-and-profit.html#parsing-wikipedia
;; http://clojure-doc.org/articles/tutorials/parsing_xml_with_zippers.html

(defn xml-zipper [str]
   (-> str
     data-xml/parse-str
     zip/xml-zip))


;; This convenience macro refactors away a bunch of very similar methods
;; taken from the xml zippers guide:
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

(defn get-tv-action [xml-req]
  (if
   (= "TransferredValue" (get-request-category xml-req))
   (get-request-action xml-req)))

(defn response-template [params]
  (format "error: %s" (:status-code params)))

(defn dispatch-redemption [card-number]
  (str "hello" card-number))

(defn dispatch-inquiry [card-number]
  (str "oh" card-number))

(defn bad-request []
  (response-template {:status-code 36}))

(defn redeem [xml-req]
  (if-let [card-number (get-card-number xml-req)]
    (dispatch-redemption card-number)
    (bad-request)))

(defn redeem-reversal [xml-req]
  (if-let [card-number (get-card-number xml-req)]
    (dispatch-redemption card-number)
    (bad-request)))

(defn status-inq [xml-req]
  (if-let [card-number (get-card-number xml-req)]
    (dispatch-inquiry card-number)
    (bad-request)))

(defn echo [xml-req]
  (let [echo-data (get-echo-data xml-req)]
    {:echo-data echo-data}))

(defn handle-card-request [xml-req]
  (cond
   (= "Redeem" (get-tv-action xml-req)) (redeem xml-req)
   (= "RedeemReversal" (get-tv-action xml-req)) (redeem-reversal xml-req)
   (= "StatusInq" (get-tv-action xml-req)) (status-inq xml-req)
   (= "Echo" (get-tv-action xml-req)) (echo xml-req)
   :else (bad-request)))
