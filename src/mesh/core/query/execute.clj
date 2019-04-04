(ns mesh.core.query.execute
  (:require [clojure.string :as str]
            [mesh.core.persistence.io :as io]
            [mesh.core.data.write :as write]
            [mesh.core.data.graph :as graph]
            [mesh.core.query.parse :as parse]
            [mesh.core.data.globals :refer :all]))

(defn execute-create-graph-query
  "Create each graph in the query."
  [raw_graph_query]
  (doseq [block (re-seq #"[A-Za-z0-9\_\-\.]{1,}" raw_graph_query)]
    (write/create-graph block)))

(defn execute-delete-graph-query
  "Delete each graph in the query."
  [raw_graph_query]
  (doseq [block (re-seq #"[A-Za-z0-9\_\-\.]{1,}" raw_graph_query)]
    (write/delete-graph block)))

(defn execute-create-node-query
  "Create each node in the query."
  [graph_name raw_node_query]
  (doseq [block (re-seq #"\(\s*[A-Za-z0-9\_\-\.:]*?\s*(?:\{.*?\}){0,1}\s*\)" raw_node_query)]
    (write/create-node graph_name (parse/parse-node-to-string block))))

(defn execute-show-node-query
  "Find and return a node in the graph."
  [raw_node_query]
  (for [block (re-seq #"\(\s*[A-Za-z0-9\_\-\.]{1,}\s*\)" raw_node_query)
        :let [node (graph/return-node @global_graph (str/trim (subs block 1 (dec (count block)))))]
        :when (not (nil? node))]
    node))

(defn execute-create-edge-query
  "Create each edge in the query."
  [graph_name raw_edge_query]
  (doseq [block (re-seq #"\([A-Za-z0-9\_\-\.]{1,}\)(?:-|<-)\[[A-Za-z0-9\_\-\.]{1,}\](?:-|->)\([A-Za-z0-9\_\-\.]{1,}\)" raw_edge_query)]
    (write/create-edge graph_name (parse/parse-edge-to-string block))))