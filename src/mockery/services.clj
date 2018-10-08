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
   :TransferredValueTxn :TransferredValueTxnReq :Extension :Value
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

;; TODO: what happens when the xml is malformed (or is another req?)
(defn is-redemption? [xml-req]
  (if (and
       (= "TransferredUse" (get-request-category xml-req))
       (= "Redeem" (get-request-action xml-req)))
    (get-card-number xml-req)
    nil))

(defn response-template [params]
  (format "error: %s" (:status-code params)))

(defn dispatch-suffix [card-number]
  (str "hello" card-number))

(defn bad-request []
  (response-template {:status-code 36}))

(defn redeem [xml-req]
  (if-let [card-number (is-redemption? xml-req)]
    (dispatch-suffix card-number)
    (bad-request)))

;; also need to define echo and status inquiry
