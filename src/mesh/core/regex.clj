(in-ns 'mesh.core.repl)

(def quit_re #"(?:(?i)QUIT|(?i)EXIT)")

;; Graph regexes
(def create_graph_re #"(?i)CREATE\s*(?:[A-Za-z0-9\_\-\.]{1,}\s*){1,}")
(def delete_graph_re #"(?i)DELETE\s*(?:[A-Za-z0-9\_\-\.]{1,}\s*){1,}")
(def load_graph_re #"(?i)LOAD\s*[A-Za-z0-9\_\-\.]{1,}")
(def list_graphs_re #"(?i)LIST")
(def save_graph_re #"(?i)SAVE")

;; Node regexes
(def create_node_re #"(?i)CREATE(\s*\(\s*(?:[A-Za-z0-9\_\-\.]{1,}:?){1,}(?:\s*,*\s*|\s*)(?:\s{1,}\{\s*[A-Za-z\_\-\.]{1,}\s*:\s*(?:[A-Za-z\_\-\.]{1,}|[0-9]{1,})(?:(?:\s*,\s*|\s{1,})[A-Za-z\_\-\.]{1,}\s*:\s*(?:[A-Za-z\_\-\.]{1,}|[0-9]{1,}))*\s*\})?\s*\)\s*\,?){1,}")
(def node_string "")

(def show_node_re #"(?i)SHOW(?:\s*\(\s*[A-Za-z0-9\_\-\.]{1,}\s*\)){1,}")

;; Edge regexes
(def label_string "(?:[A-Za-z0-9\\_\\-\\.]{1,})")
(def weight_string "(?:(?:(?:\\-|\\+)?)[0-9]{1,}(?:\\/[0-9]{1,}|\\.[0-9]*)?)")
(def label_weight_string (str "\\[(?:(?:" label_string "|" weight_string ")|(?:" label_string "\\:" weight_string "))\\]"))
(def edge_string (str "\\([A-Za-z0-9\\_\\-\\.]{1,}\\)(?:-|<)" label_weight_string "(?:-|>)\\([A-Za-z0-9\\_\\-\\.]{1,}\\)"))
(def create_edge_re (re-pattern (str "(?i)CREATE(\\s*" edge_string "\\s*,?){1,}")))