(defproject mockery "0.1.0-SNAPSHOT"
  :description "A mock XML service"
  :url "http://xmlmockery.herokuapp.com"
  :plugins [[lein-ring "0.12.4"]]
  :ring {:handler mockery.core/handler}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [liberator "0.15.2"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.7.0"]]
  :main ^:skip-aot mockery.core
  :target-path "target/%s"
  :min-lein-version "2.0.0"
  :uberjar-name "mockery.jar"
  :profiles {:uberjar {:aot :all}})
