(ns dash.data.read
  (:require [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.transform :as transform]
            [dash.data.graph :as graph]
            [dash.data.globals :refer :all]))

(defn read-graph-names
  "Read the graph_names file."
  []
  (def graph_name_file (io/provide-file (str data_dir "graph_names")))
  (def graph_name_file_contents (transform/bytes->string (io/get-contents graph_name_file)))
  (def graph_name_list (remove empty? (str/split-lines graph_name_file_contents)))
  (set graph_name_list))

(defn read-graph
  "Read a graph from its files."
  [graph_name]
  (as-> (io/provide-file (str data_dir graph_name "/edge")) $
    (io/get-contents $)
    (transform/bytes->string $)
    (str/split $ #"\^")
    (remove empty? $)
    (map transform/string->edge $)
    (reduce #(graph/add-edge %1 %2) 
            (as-> (io/provide-file (str data_dir graph_name "/node")) $$
              (io/get-contents $$)
              (transform/bytes->string $$)
              (str/split $$ #"\^")
              (remove empty? $$)
              (map transform/string->node $$)
              (reduce #(graph/add-node %1 %2)
                      {}
                      $$))
            $)))
  ; (-> (io/provide-file (str data_dir graph_name "/edge"))
  ;       io/get-contents
  ;       transform/bytes->string
  ;       (str/split #"\^")
  ;       (->> (remove empty?)
  ;            (map transform/string->edge)
  ;            (reduce #(graph/add-edge %1 %2) 
  ;                    (-> (io/provide-file (str data_dir graph_name "/node"))
  ;                        io/get-contents
  ;                        transform/bytes->string
  ;                        (str/split #"\^")
  ;                        (->> (remove empty?)
  ;                             (map transform/string->node)
  ;                             (reduce #(graph/add-node %1 %2) {})))))))