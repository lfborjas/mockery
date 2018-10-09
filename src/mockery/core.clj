(ns mockery.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET POST]]
            [mockery.services :as services]))

(defresource home [_]
  :available-media-types ["text/html"]
  :allowed-methods [:get]
  :handle-ok "<html>Hi there, please check my <a href=\"https://github.com/lfborjas/mockery/blob/master/README.md\">README</html>")

(defresource card-service [_]
  :available-media-types ["text/xml" "application/xml"]
  :allowed-methods [:post]
  :handle-created (fn [ctx]
                    (let [body (slurp (get-in ctx [:request :body]))]
                      (services/handle-card-request body))))

(defroutes app
  (ANY "/" [] home)
  (POST "/card_service" [] card-service))

(def handler
  (-> app
      wrap-params))
