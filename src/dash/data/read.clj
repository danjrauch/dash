(ns dash.data.read
  (:require [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.transform :as transform]
            [dash.data.globals :refer :all]))

(defn get-graph-names
  "Read the graph_names file."
  []
  (def graph_name_file (io/provide-file (str data_dir "graph_names")))
  (def graph_name_file_contents (transform/bytes->string (io/get-contents graph_name_file)))
  (def graph_name_list (remove empty? (str/split-lines graph_name_file_contents)))
  (set graph_name_list))

(defn read-graph
  "Read a graph from its files."
  [graph_name]
  (def node_file (io/provide-file (str data_dir graph_name "/node")))
  (def rel_file (io/provide-file (str data_dir graph_name "/rel")))
  (def graph (atom (reduce #(assoc %1 (keyword (:name %2)) %2)
                           {}
                           (map transform/string->node-map (remove empty? (str/split (transform/bytes->string (io/get-contents node_file)) #"\^"))))))
  (doseq [rel_map (map transform/string->rel-map (remove empty? (str/split (transform/bytes->string (io/get-contents rel_file)) #"\^")))]
    (case (:direction rel_map)
      ("--" "<-->") (do
                      (swap! graph update-in [(keyword (:u rel_map)) :adjacency_map (keyword (:v rel_map))] 
                             (fn [v] (if (nil? v) #{(:label rel_map)} (conj v (:label rel_map)))))
                      (swap! graph update-in [(keyword (:v rel_map)) :adjacency_map (keyword (:u rel_map))] 
                             (fn [v] (if (nil? v) #{(:label rel_map)} (conj v (:label rel_map))))))
      "<--" (swap! graph update-in [(keyword (:v rel_map)) :adjacency_map (keyword (:u rel_map))] 
                   (fn [v] (if (nil? v) #{(:label rel_map)} (conj v (:label rel_map)))))
      "-->" (swap! graph update-in [(keyword (:u rel_map)) :adjacency_map (keyword (:v rel_map))] 
                   (fn [v] (if (nil? v) #{(:label rel_map)} (conj v (:label rel_map)))))))
  @graph)