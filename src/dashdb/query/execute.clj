(ns dashdb.query.execute
  (:require [dashdb.persistence.io :refer :all :as io]
            [environ.core :as environ]
            [dashdb.crypto.id :as id]))

(defn create-node
  "Execute the create action for an individual record.
   [vector & vector] -> string"
  [name_adjs & prop_pairs]
  ;; Node writing
  (if (= (environ/env :clj-env) "test") 
    (def nodefilename "test/test_data/node")
    (def nodefilename "data/node"))
  (io/pin-file BM nodefilename)
  (def nodefile (io/get-file BM nodefilename))
  (io/concat-append nodefile name_adjs)
  (when prop_pairs
    (io/append-content nodefile "@")
    (io/concat-append nodefile (nth prop_pairs 0)))
  (io/append-content nodefile "^")
  (io/write-to-file nodefile)
  (when (not= (environ/env :clj-env) "test")
    (println "+---------------------------+")
    (println "| Successfully created node |")
    (println "+---------------------------+"))
  (id/create-id))

(defn create-rel
  "Execute the create action for an individual record.
   [vector] -> string"
  [v]
  (if (= (environ/env :clj-env) "test") 
    (def filename "test/test_data/rel")
    (def filename "data/rel"))
  (io/pin-file BM filename)
  (def file (io/get-file BM filename))
  (io/concat-append file v)
  (io/append-content file "^")
  (io/write-to-file file)
  (when (not= (environ/env :clj-env) "test")
    (println "+-----------------------------------+")
    (println "| Successfully created relationship |")
    (println "+-----------------------------------+"))
  (id/create-id))
