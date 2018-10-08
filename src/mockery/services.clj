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

;; TODO: these three methods probably are in dire need of a macro

(defn get-card-number [xml-req]
  (zip-xml/xml1->
   (xml-zipper xml-req)
   :TransferredValueTxn :TransferredValueTxnReq :CardActionInfo :PIN
   zip-xml/text))

(defn get-request-category [xml-req]
  (zip-xml/xml1->
   (xml-zipper xml-req)
   :TransferredValueTxn :TransferredValueTxnReq :ReqCat
   zip-xml/text))

(defn get-request-action [xml-req]
  (zip-xml/xml1->
   (xml-zipper xml-req)
   :TransferredValueTxn :TransferredValueTxnReq :ReqAction
   zip-xml/text))

(defn get-request-body [xml-req]
  (zip-xml/xml1->
   (xml-zipper xml-req)
   :TransferredValueTxn :TransferredValueTxnReq))

(defn get-echo-data [xml-req]
  (zip-xml/xml1->
   (xml-zipper xml-req)
   :TransferredValueTxn :TransferredValueTxnReq :EchoData
   zip-xml/text))

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

(defn status-inq [xml-req]
  (if-let [card-number (get-card-number xml-req)]
    (dispatch-inquiry card-number)
    (bad-request)))

(defn echo [xml-req]
  (let [body (get-request-body xml-req)
        echo-data (get-echo-data xml-req)]
    {:body (data-xml/emit-str (first body)) :echo-data echo-data}))

(defn handle-card-request [xml-req]
  (cond
   (= "Redeem" (get-tv-action xml-req)) (redeem xml-req)
   (= "StatusInq" (get-tv-action xml-req)) (status-inq xml-req)
   (= "Echo" (get-tv-action xml-req)) (echo xml-req)
   :else (bad-request)))
