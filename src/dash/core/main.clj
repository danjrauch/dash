(ns dash.core.main
  (:require [dash.query.execute :as execute]
            [dash.query.regex :refer :all]))

(defn direct-query-input
  "Take the input from the cli and parse it into usable query.
   [string] -> nil"
  [query_string]
  ; Match the query_string to one of the recognized forms
  (cond
    ; The query_string is a create-node clause
    (= query_string (nth (re-find create_node_re query_string) 0)) (execute/execute-create-node-query (subs query_string 6))
    ; The query_stringis a create-rel clause
    (= query_string (nth (re-find create_rel_re query_string) 0)) (execute/execute-create-relationship-query (subs query_string 6))
    :else (println "Invalid query string. Please review and try again.")))