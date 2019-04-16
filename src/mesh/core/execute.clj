(in-ns 'mesh.core.repl)

(defn create-graphs
  "Create each graph in the query."
  {:added "0.1.0"}
  [raw_graph_query]
  (doseq [graph_name (re-seq #"[A-Za-z0-9\_\-\.]{1,}" raw_graph_query)]
    (persist/create-graph graph_name data_dir)
    (when (not (contains? @global_graph_set graph_name))
      (def graph_file (persist/provide-file (str data_dir "graph_names")))
      (persist/append-string graph_file (str graph_name "\n"))
      (persist/write-to-disk graph_file))
    (swap! global_graph_set conj graph_name)))

(defn delete-graphs
  "Delete each graph in the query."
  {:added "0.1.0"}
  [raw_graph_query]
  (doseq [graph_name (re-seq #"[A-Za-z0-9\_\-\.]{1,}" raw_graph_query)]
    (persist/delete-graph graph_name data_dir)
    (swap! global_graph_set disj graph_name)
    (persist/delete-file-recursively (str data_dir "graph_names"))
    (def graph_names_file (persist/provide-file (str data_dir "graph_names")))
    (persist/concat-append-string graph_names_file @global_graph_set "\n")
    (when (> (count @global_graph_set) 0) (persist/append-string graph_names_file "\n"))
    (persist/write-to-disk graph_names_file)))

(defn create-nodes
  "Create each node in the query."
  {:added "0.1.0"}
  [graph_name raw_node_query]
  (doseq [block (re-seq #"\(\s*[A-Za-z0-9\_\-\.:]*?\s*(?:\{.*?\}){0,1}\s*\)" raw_node_query)]
    (swap! global_graph graph/add-node (graph/string->node (parse-node-to-string block)))))

(defn create-edges
  "Create each edge in the query."
  {:added "0.1.0"}
  [graph_name raw_edge_query]
  (doseq [block (re-seq (re-pattern edge_string) raw_edge_query)]
    (swap! global_graph graph/add-edge (graph/string->edge (parse-edge-to-string block)))))

(defn show-nodes
  "Find and return a node in the graph."
  {:added "0.1.0"}
  [raw_node_query]
  (for [block (re-seq #"\(\s*[A-Za-z0-9\_\-\.]{1,}\s*\)" raw_node_query)
        :let [node (graph/return-node @global_graph (str/trim (subs block 1 (dec (count block)))))]
        :when (not (nil? node))]
    node))

;; TODO 'save graph_name' command, also query->command