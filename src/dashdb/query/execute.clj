(ns dashdb.query.execute
  (:require [dashdb.persistence.io :as io]
            [environ.core :as environ]))

(defn create-node
  "Execute the create action for an individual record.
   [vector & vector] -> nil"
  [name_adjs & prop_pairs]
  (def file (io/create-file "data/node"))
  (when (= (environ/env :clj-env) "test") (io/set-file-name file "test/test_data/t"))
  (io/read-from-file file)
  (io/concat-append file name_adjs)
  (when prop_pairs
    (io/append-content file "@")
    (io/concat-append file (nth prop_pairs 0)))
  (io/append-content file "^")
  (io/write-to-file file)
  (when (not= (environ/env :clj-env) "test")
    (println "+---------------------------+")
    (println "| Successfully created node |")
    (println "+---------------------------+")))

(defn create-rel
  "Execute the create action for an individual record.
   [vector] -> nil"
  [v]
  (def file (io/create-file "data/rel"))
  (when (= (environ/env :clj-env) "test") (io/set-file-name file "test/test_data/t"))
  (io/read-from-file file)
  (io/concat-append file v)
  (io/append-content file "^")
  (io/write-to-file file)
  (when (not= (environ/env :clj-env) "test")
    (println "+-----------------------------------+")
    (println "| Successfully created relationship |")
    (println "+-----------------------------------+")))
