(ns dash.core.launch
  (:require [clojure.string :as str]
            [spinner.core :as spin]
            [dash.query.execute :as execute]
            [dash.data.read :as read]
            [dash.query.regex :refer :all]
            [dash.data.globals :refer :all]))

(reset! global_graph_set (read/get-graph-names))

(defn direct-query-input
  "Take the input from the cli and parse it into executable query."
  [graph_name query_string]
  ; Match the query_string to one of the recognized forms
  (cond
    ; The query_string is a create-graph clause
    (= query_string (nth (re-matches create_graph_re query_string) 0)) (execute/execute-create-graph-query (subs query_string 13))
    ; The query_string is a create-node clause
    (= query_string (nth (re-matches create_node_re query_string) 0)) (execute/execute-create-node-query graph_name (subs query_string 6))
    ; The query_stringis a create-rel clause
    (= query_string (nth (re-matches create_rel_re query_string) 0)) (execute/execute-create-relationship-query graph_name (subs query_string 6))
    :else (println "Invalid query string.")))

(defn handle-input
  ""
  [raw_query_string]
  (cond
    (not-empty (re-matches set_graph_re raw_query_string))
      (reset! global_graph_name (nth (str/split (re-matches set_graph_re raw_query_string) #" ") 2))
    (contains? @global_graph_set @global_graph_name)
      (let [s (spin/create-and-start! {:frames (spin/styles :braille)})]
        (direct-query-input @global_graph_name raw_query_string)
        (spin/stop! s)))
  (println))