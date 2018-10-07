(ns mockery.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET POST]]))

(defresource home [_]
  :available-media-types ["text/html"]
  :allowed-methods [:get]
  :handle-ok "<html>Oh hi there</html>")

(defroutes app
  (ANY "/" [] home))

(def handler
  (-> app
      wrap-params))
