(in-ns 'mesh.repl)

(def global_graph_set (atom #{}))
(def global_graph_name (atom ""))
(def global_graph (atom {:name "" :nodes {}}))

(def data_dir
  (if (= (environ/env :clj-env) "test")
    "test/test_data/"
    "data/"))