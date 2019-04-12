(in-ns 'mesh.core.repl)

(defn handle-input
  ""
  {:added "0.1.0"}
  [raw_query_string]
  (when (empty? @global_graph_set)
    (reset! global_graph_set (persist/read-graph-names data_dir)))
  (cond
    (not-empty (re-matches list_graphs_re raw_query_string))
    (do
      (print " ")
      (println)
      (doseq [graph_name @global_graph_set]
        (print (str graph_name " "))))
    (not-empty (re-matches save_graph_re raw_query_string))
    (do
      (print " ")
      (persist/write-graph @global_graph data_dir))
    (not-empty (re-matches load_graph_re raw_query_string))
    (if (contains? @global_graph_set (nth (str/split (re-matches load_graph_re raw_query_string) #"\s{1,}") 1))
      (do
        (print " ")
        (reset! global_graph_name (nth (str/split (re-find load_graph_re raw_query_string) #"\s{1,}") 1))
        (reset! global_graph (persist/read-graph @global_graph_name data_dir)))
      (print " Graph not found."))
    (not-empty (re-matches create_graph_re raw_query_string))
    (if (not (contains? @global_graph_set (nth (str/split (re-find create_graph_re raw_query_string) #"\s{1,}") 1)))
      (do
        (print " ")
        (execute-create-graph-query (subs raw_query_string 6)))
      (print " Graph already created."))
    (not-empty (re-matches delete_graph_re raw_query_string))
    (if (contains? @global_graph_set (nth (str/split (re-find delete_graph_re raw_query_string) #"\s{1,}") 1))
      (do
        (print " ")
        (execute-delete-graph-query (subs raw_query_string 6)))
      (print " Graph not found."))
    (not-empty (re-matches create_node_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (print " ")
        (spin/spin! #(execute-create-node-query @global_graph_name (subs raw_query_string 6)) {:frames (spin/styles :braille)}))
      (print " Graph not found."))
    (not-empty (re-matches show_node_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (print " ")
        (pprint/print-table (spin/spin! #(execute-show-node-query (subs raw_query_string 4)) {:frames (spin/styles :braille)})))
      (print " Graph not found."))
    (not-empty (re-matches create_edge_re raw_query_string))
    (if (contains? @global_graph_set @global_graph_name)
      (do
        (print " ")
        (spin/spin! #(execute-create-edge-query @global_graph_name (subs raw_query_string 6)) {:frames (spin/styles :braille)}))
      (print " Graph not found."))
    :else
    (print ""))
  (println))