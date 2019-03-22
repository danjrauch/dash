(ns dash.query.execute
  (:require [dash.persistence.io :refer :all :as io]
            [environ.core :as environ]
            [dash.query.parse :as parse]
            [dash.crypto.id :as id]))

(defn create-node
  "Execute the create action for an individual record.
   [vector & vector] -> string"
  [node_map]
  (if (= (environ/env :clj-env) "test") 
    (def nodefilename "test/test_data/node")
    (def nodefilename "data/node"))
  (io/pin-file BM nodefilename)
  (def nodefile (io/get-file BM nodefilename))
  (io/concat-append nodefile (:name_adjs node_map))
  (when (contains? node_map :prop_pairs)
    (io/append-content nodefile "@")
    (io/concat-append nodefile (:prop_pairs node_map)))
  (io/append-content nodefile "^")
  (io/write-to-file nodefile)
  (when (not= (environ/env :clj-env) "test")
    (println "+---------------------------+")
    (println "| Successfully created node |")
    (println "+---------------------------+"))
  (id/create-id))

(defn create-relationship
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

(defn execute-create-node-query
  ""
  [raw_create_query]
  (doseq [block (filter #(re-seq #"\(\s*[A-Za-z0-9:]*?\s*(\{.*?\}){0,1}\s*\)" %) (re-seq #"\(.*?\)" raw_create_query))]
    (create-node (parse/parse-create-node-block block))))

(defn execute-create-relationship-query
  ""
  [raw_relationship_query]
  (doseq [block (re-seq #"\([A-Za-z0-9:]{1,}\)(-|<-)\[:[A-Za-z0-9:]{1,}\](-|->)\([A-Za-z0-9:]{1,}\)" raw_relationship_query)]
    (create-relationship (parse/parse-create-relationship-block (nth block 0)))))
  