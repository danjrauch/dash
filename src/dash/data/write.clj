(ns dash.persistence.write
  (:require [clojure.string :as str]
            [dash.persistence.io :as io]))

(defn create-graph
  ""
  [graph_name]
  (io/create-path (str "data/" graph_name))
  (io/provide-file graph_name "node")
  (io/provide-file graph_name "rel"))

(defn create-node
  "Execute the create action for an individual record."
  [graph_name node_map]
  (def nodefile (io/provide-file graph_name "node"))
  (io/concat-append nodefile (:name_adjs node_map))
  (when (contains? node_map :prop_pairs)
    (io/append-content nodefile "@")
    (io/concat-append nodefile (:prop_pairs node_map)))
  (io/append-content nodefile "^")
  (io/write-to-disk nodefile)
  (Thread/sleep 3000) ; TEST SPINNER
  (id/create-id))

(defn create-relationship
  "Execute the create action for an individual record."
  [graph_name v]
  (def relfile (io/provide-file graph_name "rel"))
  (io/concat-append relfile v)
  (io/append-content relfile "^")
  (io/write-to-disk relfile)
  (id/create-id))