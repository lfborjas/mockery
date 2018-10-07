(defproject mockery "0.1.0-SNAPSHOT"
  :description "A mock XML service"
  :url "http://xmlmockery.herokuapp.com"
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main ^:skip-aot mockery.core
  :target-path "target/%s"
  :min-lein-version "2.0.0"
  :uberjar-name "mockery.jar"
  :profiles {:uberjar {:aot :all}})
