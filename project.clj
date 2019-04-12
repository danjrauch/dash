(defproject mesh "0.1.0-SNAPSHOT"
  :description "Graph processing library and database"
  :url "https://github.com/danjrauch/mesh"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/data.fressian "0.2.1"]
                 [org.fressian/fressian "0.6.2"]
                 [clj-time "0.15.0"]
                 [environ "1.1.0"]
                 [clj-commons/spinner "0.6.0"]]
  :plugins [[lein-environ "1.1.0"]
            [lein-binplus "0.6.5"]      ; https://github.com/BrunoBonacci/lein-binplus
            [lein-annotations "0.1.0"]  ; https://github.com/bbatsov/lein-annotations
            ]
  :main ^:skip-aot mesh.core.repl
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev        {:env {:clj-env "development"}}
             :test       {:env {:clj-env "test"}}
             :production {:env {:clj-env "production"}}}
  :bin {:name "mesh"
        :bin-path "~/usr/local/bin"
        :bootclasspath false
        :jvm-opts ["-Dmesh.version=0.1.0"]})