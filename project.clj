(defproject dash "0.1.0-SNAPSHOT"
  :description "lightweight graph-based DBMS"
  :url "https://github.com/danjrauch/dash"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [clj-time "0.15.0"]
                 [environ "1.1.0"]
                 [clj-commons/spinner "0.6.0"]
                ]
  :plugins [[lein-environ "1.1.0"]
            [lein-binplus "0.6.5"] ; https://github.com/BrunoBonacci/lein-binplus
           ]
  :main ^:skip-aot dash.core.cli
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev        {:env {:clj-env "development"}}
             :test       {:env {:clj-env "test"}}
             :production {:env {:clj-env "production"}}})