(defproject mesh "0.1.0-SNAPSHOT"
  :description "Graph processing library and database"
  :url "https://github.com/danjrauch/mesh"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1-beta2"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/data.fressian "0.2.1"]
                 [org.fressian/fressian "0.6.2"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.15.0"]
                 [environ "1.1.0"]
                 [clj-commons/spinner "0.6.0"]
                 [prismatic/schema "1.1.10"]
                 [metosin/compojure-api "2.0.0-alpha30" :exclude [compojure, metosin/muuntaja]]
                 [ring/ring "1.6.3"]
                 [compojure "1.6.1"]
                 [manifold "0.1.8"]]
  :plugins [[lein-environ "1.1.0"]
            [lein-binplus "0.6.5"]      ; https://github.com/BrunoBonacci/lein-binplus
            [lein-annotations "0.1.0"]  ; https://github.com/bbatsov/lein-annotations
            [lein-ring "0.12.5"]
            ]
  :main ^:skip-aot mesh.repl
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev        {:env {:clj-env "development"}}
             :test       {:env {:clj-env "test"}}
             :production {:env {:clj-env "production"}}}
  :ring {:handler mesh.api/app
         :async? true}
  :bin {:name "mesh"
        :bin-path "~/usr/local/bin"
        :bootclasspath false
        :jvm-opts ["-Dmesh.version=0.1.0"]})