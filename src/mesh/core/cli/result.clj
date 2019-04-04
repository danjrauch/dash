(ns mesh.core.cli.result
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [spinner.core :as spin]
            [mesh.core.query.execute :as execute]
            [mesh.core.data.read :as read]
            [mesh.core.query.regex :refer :all]
            [mesh.core.data.globals :refer :all]))

(defn handle-input
  ""
  [raw_query_string]
  (when (empty? @global_graph_set) 
    (reset! global_graph_set (read/read-graph-names)))
  (cond
    (not-empty (re-matches list_graphs_re raw_query_string))
    (do
      (print " ")
      (println)
      (doseq [graph_name @global_graph_set]
        (print (str graph_name " "))))
    (not-empty (re-matches load_graph_re raw_query_string))
    (if (contains? @global_graph_set (nth (str/split (re-matches load_graph_re raw_query_string) #"\s{1,}") 1))
      (do
        (print " ")
        (reset! global_graph_name (nth (str/split (re-find load_graph_re raw_query_string) #"\s{1,}") 1))
        (reset! global_graph (read/read-graph @global_graph_name)))
      (print " Graph not found."))
    (not-empty (re-matches create_graph_re raw_query_string))
    (do
      (print " ")
      (execute/execute-create-graph-query (subs raw_query_string 6)))
    (not-empty (re-matches delete_graph_re raw_query_string))
    (if (contains? @global_graph_set (nth (str/split (re-find delete_graph_re raw_query_string) #"\s{1,}") 1))
      (do
        (print " ")
        (execute/execute-delete-graph-query (subs raw_query_string 6)))
      (print " Graph not found."))
    (not-empty (re-matches create_node_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (print " ")
        (spin/spin! #(execute/execute-create-node-query @global_graph_name (subs raw_query_string 6)) {:frames (spin/styles :braille)}))
      (print " Graph not found."))
    (not-empty (re-matches show_node_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (print " ")
        (pprint/print-table (spin/spin! #(execute/execute-show-node-query (subs raw_query_string 4)) {:frames (spin/styles :braille)})))
      (print " Graph not found."))
    (not-empty (re-matches create_edge_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (print " ")
        (spin/spin! #(execute/execute-create-edge-query @global_graph_name (subs raw_query_string 6)) {:frames (spin/styles :braille)}))
      (print " Graph not found."))
    :else
    (print ""))
  (println))