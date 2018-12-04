(defproject dashdb "0.1.0-SNAPSHOT"
  :description "lightweight graph-based DBMS"
  :url "https://github.com/danjrauch/dashDB"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot dashdb.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
