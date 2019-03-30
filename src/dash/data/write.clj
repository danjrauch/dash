(ns dash.data.write
  (:require [environ.core :as environ]
            [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.crypto.id :as id]
            [dash.data.globals :refer :all]))

(defn create-graph
  ""
  [graph_name]
  (io/create-path (str data_dir graph_name))
  (io/provide-file (str data_dir graph_name "/node"))
  (io/provide-file (str data_dir graph_name "/rel"))
  (when (not (contains? @global_graph_set graph_name))
    (def graph_file (io/provide-file (str data_dir "graph_names")))
    (io/append-content graph_file (str graph_name "\n"))
    (io/write-to-disk graph_file))
  (swap! global_graph_set conj graph_name))

(defn create-node
  "Execute the create action for an individual node."
  [graph_name node_map]
  (def node_file (io/provide-file (str data_dir graph_name "/node")))
  (io/concat-append node_file (:name_adjs node_map))
  (when (contains? node_map :prop_pairs)
    (io/append-content node_file "@")
    (io/concat-append node_file (:prop_pairs node_map)))
  (io/append-content node_file "^")
  (io/write-to-disk node_file)
  (when (not= (environ/env :clj-env) "test") (Thread/sleep 3000)) ; TEST SPINNER
  (id/create-id))

(defn create-relationship
  "Execute the create action for an individual relationship."
  [graph_name v]
  (def rel_file (io/provide-file (str data_dir graph_name "/rel")))
  (io/concat-append rel_file v)
  (io/append-content rel_file "^")
  (io/write-to-disk rel_file)
  (id/create-id))