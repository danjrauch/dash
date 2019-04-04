(ns dash.core.input
  (:require [clojure.string :as str]
            [spinner.core :as spin]
            [dash.query.execute :as execute]
            [dash.data.read :as read]
            [dash.query.regex :refer :all]
            [dash.data.globals :refer :all]))

(defn handle-input
  ""
  [raw_query_string]
  (when (empty? @global_graph_set) 
    (reset! global_graph_set (read/read-graph-names)))
  (cond
    (not-empty (re-matches list_graphs_re raw_query_string))
    (do
      (print "")
      (println)
      (doseq [graph_name @global_graph_set]
        (print (str graph_name " "))))
    (not-empty (re-matches load_graph_re raw_query_string))
    (if (contains? @global_graph_set (nth (str/split (re-matches load_graph_re raw_query_string) #"\s{1,}") 1))
      (do
        (reset! global_graph_name (nth (str/split (re-find load_graph_re raw_query_string) #"\s{1,}") 1))
        (reset! global_graph (read/read-graph @global_graph_name))
        (print ""))
      (print " Graph not found."))
    (not-empty (re-matches create_graph_re raw_query_string))
    (do
      (execute/execute-create-graph-query (subs raw_query_string 6))
      (print ""))
    (not-empty (re-matches delete_graph_re raw_query_string))
    (if (contains? @global_graph_set (nth (str/split (re-find load_graph_re raw_query_string) #" ") 2))
      (do
        (execute/execute-delete-graph-query (subs raw_query_string 6))
        (print ""))
      (print " Graph not found."))
    (not-empty (re-matches create_edge_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (spin/spin! #(execute/execute-create-edge-query @global_graph_name (subs raw_query_string 6)) {:frames (spin/styles :braille)})
        (print ""))
      (print " Graph not found."))
    (not-empty (re-matches create_node_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (spin/spin! #(execute/execute-create-node-query @global_graph_name (subs raw_query_string 6)) {:frames (spin/styles :braille)})
        (print ""))
      (print " Graph not found."))
    :else
    (print ""))
  (println))