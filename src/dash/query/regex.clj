(ns dash.query.regex)

(def create_node_re #"(?i)CREATE(\s*\(\s*(?:[A-Za-z0-9]{1,}:?){1,}(?:\s*,*\s*|\s*)(?:\s{1,}\{\s*[A-Za-z]{1,}\s*:\s*(?:[A-Za-z]{1,}|[0-9]{1,})(?:(?:\s*,\s*|\s{1,})[A-Za-z]{1,}\s*:\s*(?:[A-Za-z]{1,}|[0-9]{1,}))*\s*\})?\s*\)\s*\,?){1,}")
(def create_edge_re #"(?i)CREATE(\s*\([A-Za-z0-9]{1,}\)(?:-|<-)\[[A-Za-z0-9]{1,}\](?:-|->)\([A-Za-z0-9]{1,}\)\s*,?){1,}")
(def create_graph_re #"(?i)CREATE\s*([A-Za-z0-9]{1,}\s*){1,}")
(def delete_graph_re #"(?i)DELETE\s*([A-Za-z0-9]{1,}\s*){1,}")
(def load_graph_re #"(?i)LOAD\s*[A-Za-z0-9]{1,}")
(def list_graphs_re #"(?i)LIST")