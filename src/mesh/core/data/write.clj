(ns mesh.core.data.write
  (:require [environ.core :as environ]
            [clojure.string :as str]
            [mesh.core.persistence.io :as io]
            [mesh.core.data.graph :as graph]
            [mesh.core.data.transform :as transform]
            [mesh.core.data.globals :refer :all]))

(defn create-graph
  "Create a graph."
  [graph_name]
  (try
    (io/create-path (str data_dir graph_name))
    (io/provide-file (str data_dir graph_name "/node"))
    (io/provide-file (str data_dir graph_name "/edge"))
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
  [graph_name node_string]
  (try
    (def node_file (io/provide-file (str data_dir graph_name "/node")))
    (io/append-content node_file (str node_string "^"))
    (swap! global_graph graph/add-node (transform/string->node node_string))
    (io/write-to-disk node_file)
    (when (not= (environ/env :clj-env) "test") (Thread/sleep 3000)) ; TEST SPINNER
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in create-node: " (.getMessage ex)))))

(defn create-edge
  "Create a edge."
  [graph_name edge_string]
  (try
    (def edge_file (io/provide-file (str data_dir graph_name "/edge")))
    (io/append-content edge_file (str edge_string "^"))
    (swap! global_graph graph/add-edge (transform/string->edge edge_string))
    (io/write-to-disk edge_file)
    (when (not= (environ/env :clj-env) "test") (Thread/sleep 3000)) ; TEST SPINNER
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in create-edge: " (.getMessage ex)))))

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
      (when (> (count @global_graph_set) 0) (io/append-content graph_names_file "\n"))
      (io/write-to-disk graph_names_file))
    (catch Exception ex
      (.printStackTrace ex)
      (str "Exception in delete-graph: " (.getMessage ex)))))