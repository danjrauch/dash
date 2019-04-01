(ns dash.query.execute
  (:require [dash.persistence.io :as io]
            [dash.data.write :as write]
            [dash.query.parse :as parse]
            [dash.data.globals :refer :all]))

(defn execute-create-graph-query
  "Create each graph in the query."
  [raw_graph_query]
  (doseq [block (re-seq #"[A-Za-z0-9]{1,}" raw_graph_query)]
    (write/create-graph block)))

(defn execute-create-node-query
  "Create each node in the query."
  [graph_name raw_node_query]
  (doseq [block (filter #(re-seq #"\(\s*[A-Za-z0-9:]*?\s*(\{.*?\}){0,1}\s*\)" %) (re-seq #"\(.*?\)" raw_node_query))]
    (write/create-node graph_name (parse/parse-create-node-block block))))

(defn execute-create-relationship-query
  "Create each relationship in the query."
  [graph_name raw_relationship_query]
  (doseq [block (re-seq #"\([A-Za-z0-9:]{1,}\)(-|<-)\[[A-Za-z0-9:]{1,}\](-|->)\([A-Za-z0-9:]{1,}\)" raw_relationship_query)]
    (write/create-relationship graph_name (parse/parse-create-relationship-block (nth block 0)))))

(defn execute-delete-graph-query
  "Delete each graph in the query."
  [raw_graph_query]
  (doseq [block (re-seq #"[A-Za-z0-9]{1,}" raw_graph_query)]
    (write/delete-graph block)))