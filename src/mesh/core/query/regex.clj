(ns mesh.core.query.regex)

;; Graph regexes
(def create_graph_re #"(?i)CREATE\s*(?:[A-Za-z0-9\_\-\.]{1,}\s*){1,}")
(def delete_graph_re #"(?i)DELETE\s*(?:[A-Za-z0-9\_\-\.]{1,}\s*){1,}")
(def load_graph_re #"(?i)LOAD\s*[A-Za-z0-9\_\-\.]{1,}")
(def list_graphs_re #"(?i)LIST")

;; Node regexes
(def create_node_re #"(?i)CREATE(\s*\(\s*(?:[A-Za-z0-9\_\-\.]{1,}:?){1,}(?:\s*,*\s*|\s*)(?:\s{1,}\{\s*[A-Za-z\_\-\.]{1,}\s*:\s*(?:[A-Za-z\_\-\.]{1,}|[0-9]{1,})(?:(?:\s*,\s*|\s{1,})[A-Za-z\_\-\.]{1,}\s*:\s*(?:[A-Za-z\_\-\.]{1,}|[0-9]{1,}))*\s*\})?\s*\)\s*\,?){1,}")
(def show_node_re #"(?i)SHOW(?:\s*\(\s*[A-Za-z0-9\_\-\.]{1,}\s*\)){1,}")

;; Edge regexes
(def create_edge_re #"(?i)CREATE(\s*\([A-Za-z0-9\_\-\.]{1,}\)(?:-|<-)\[[A-Za-z0-9\_\-\.]{1,}\](?:-|->)\([A-Za-z0-9\_\-\.]{1,}\)\s*,?){1,}")