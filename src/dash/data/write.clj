(ns dash.data.write
  (:require [environ.core :as environ]
            [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.globals :refer :all]))

(defn create-graph
  "Create a graph."
  [graph_name]
  (try
    (io/create-path (str data_dir graph_name))
    (io/provide-file (str data_dir graph_name "/node"))
    (io/provide-file (str data_dir graph_name "/rel"))
    (io/provide-file (str data_dir graph_name "/stats"))
    (when (not (contains? @global_graph_set graph_name))
      (def graph_file (io/provide-file (str data_dir "graph_names")))
      (io/append-content graph_file (str graph_name "\n"))
      (io/write-to-disk graph_file))
    (swap! global_graph_set conj graph_name)
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in create-graph: " (.getMessage ex)))))

(defn create-node
  "Create a node."
  [graph_name node_map]
  (try
    (def node_file (io/provide-file (str data_dir graph_name "/node")))
    (io/concat-append node_file (:name_adjs node_map) "|")
    (when (contains? node_map :prop_pairs)
      (io/append-content node_file "@")
      (io/concat-append node_file (:prop_pairs node_map) "|"))
    (io/append-content node_file "^")
    (io/write-to-disk node_file)
    (when (not= (environ/env :clj-env) "test") (Thread/sleep 3000)) ; TEST SPINNER
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in create-node: " (.getMessage ex)))))

(defn create-relationship
  "Create a relationship."
  [graph_name rel]
  (try
    (def rel_file (io/provide-file (str data_dir graph_name "/rel")))
    (io/concat-append rel_file rel "|")
    (io/append-content rel_file "^")
    (io/write-to-disk rel_file)
    (when (not= (environ/env :clj-env) "test") (Thread/sleep 3000)) ; TEST SPINNER
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in create-relationship: " (.getMessage ex)))))

(defn delete-graph
  "Delete a graph."
  [graph_name]
  (try
    (when (contains? @global_graph_set graph_name)
      (io/delete-file-recursively (str data_dir graph_name))
      (swap! global_graph_set disj graph_name)
      (io/delete-file-recursively (str data_dir "graph_names"))
      (def graph_names_file (io/provide-file (str data_dir "graph_names")))
      (io/concat-append graph_names_file @global_graph_set "\n")
      (io/append-content graph_names_file "\n")
      (io/write-to-disk graph_names_file))
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in delete-graph: " (.getMessage ex)))))