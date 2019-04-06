(in-ns 'mesh.core.repl)

(defn execute-create-graph-query
  "Create each graph in the query."
  [raw_graph_query]
  (doseq [graph_name (re-seq #"[A-Za-z0-9\_\-\.]{1,}" raw_graph_query)]
    (persist/create-graph graph_name data_dir)
    (when (not (contains? @global_graph_set graph_name))
      (def graph_file (persist/provide-file (str data_dir "graph_names")))
      (persist/append-content graph_file (str graph_name "\n"))
      (persist/write-to-disk graph_file))
    (swap! global_graph_set conj graph_name)))

(defn execute-delete-graph-query
  "Delete each graph in the query."
  [raw_graph_query]
  (doseq [graph_name (re-seq #"[A-Za-z0-9\_\-\.]{1,}" raw_graph_query)]
    (persist/delete-graph graph_name data_dir)
    (swap! global_graph_set disj graph_name)
    (persist/delete-file-recursively (str data_dir "graph_names"))
    (def graph_names_file (persist/provide-file (str data_dir "graph_names")))
    (persist/concat-append graph_names_file @global_graph_set "\n")
    (when (> (count @global_graph_set) 0) (persist/append-content graph_names_file "\n"))
    (persist/write-to-disk graph_names_file)))

(defn execute-create-node-query
  "Create each node in the query."
  [graph_name raw_node_query]
  (doseq [block (re-seq #"\(\s*[A-Za-z0-9\_\-\.:]*?\s*(?:\{.*?\}){0,1}\s*\)" raw_node_query)]
    (def node (graph/string->node (parse-node-to-string block)))
    (persist/write-node graph_name node data_dir)
    (swap! global_graph graph/add-node node)))

(defn execute-create-edge-query
  "Create each edge in the query."
  [graph_name raw_edge_query]
  (doseq [block (re-seq #"\([A-Za-z0-9\_\-\.]{1,}\)(?:-|<)\[[A-Za-z0-9\_\-\.]{1,}\](?:-|>)\([A-Za-z0-9\_\-\.]{1,}\)" raw_edge_query)]
    (def edge (graph/string->edge (parse-edge-to-string block)))
    (persist/write-edge graph_name edge data_dir)
    (swap! global_graph graph/add-edge edge)))

(defn execute-show-node-query
  "Find and return a node in the graph."
  [raw_node_query]
  (for [block (re-seq #"\(\s*[A-Za-z0-9\_\-\.]{1,}\s*\)" raw_node_query)
        :let [node (graph/return-node @global_graph (str/trim (subs block 1 (dec (count block)))))]
        :when (not (nil? node))]
    node))