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
    (reset! global_graph_set (read/get-graph-names)))
  (cond
    (not-empty (re-matches set_graph_re raw_query_string))
    (do
      (reset! global_graph_name (nth (str/split (re-matches set_graph_re raw_query_string) #" ") 2))
      (print ""))
    (not-empty (re-matches create_graph_re raw_query_string))
    (do
      (execute/execute-create-graph-query (subs raw_query_string 6))
      (print ""))
    (not-empty (re-matches delete_graph_re raw_query_string))
    (do
      (execute/execute-delete-graph-query (subs raw_query_string 6))
      (print ""))
    (not-empty (re-matches list_graphs_re raw_query_string))
    (do
      (print "")
      (println)
      (doseq [graph_name @global_graph_set]
        (print (str graph_name " "))))
    (not-empty (re-matches create_rel_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (let [s (spin/create-and-start! {:frames (spin/styles :braille)})]
        (execute/execute-create-relationship-query @global_graph_name (subs raw_query_string 6))
        (spin/stop! s)
        (print ""))
      (print ""))
    (not-empty (re-matches create_node_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (let [s (spin/create-and-start! {:frames (spin/styles :braille)})]
        (execute/execute-create-node-query @global_graph_name (subs raw_query_string 6))
        (spin/stop! s)
        (print ""))
      (print ""))
    :else
    (print ""))
  (println))