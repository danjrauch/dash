(ns mesh.core.persist
  (:require [clojure.string :as str]
            [environ.core :as environ]
            [clojure.string :as str]
            [clojure.core.async :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clojure.java.io :as io]
            [mesh.core.graph :as graph]))

(load "io")

(defn read-graph-names
  "Read the graph_names file."
  [data_dir]
  (def graph_name_file (provide-file (str data_dir "graph_names")))
  (def graph_name_file_contents (get-contents graph_name_file))
  (def graph_name_list (remove empty? (str/split-lines graph_name_file_contents)))
  (set graph_name_list))

(defn read-graph
  "Read a graph from its files."
  [graph_name data_dir]
  (as-> (provide-file (str data_dir graph_name "/edge")) $
    (get-contents $)
    (str/split $ #"\^")
    (remove empty? $)
    (map graph/string->edge $)
    (reduce #(graph/add-edge %1 %2)
            (as-> (provide-file (str data_dir graph_name "/node")) $$
              (get-contents $$)
              (str/split $$ #"\^")
              (remove empty? $$)
              (map graph/string->node $$)
              (reduce #(graph/add-node %1 %2)
                      {:name graph_name :nodes {}}
                      $$))
            $)))

(defn create-graph
  "Create a graph on disk."
  [graph_name data_dir]
  (create-path (str data_dir graph_name))
  (provide-file (str data_dir graph_name "/node"))
  (provide-file (str data_dir graph_name "/edge"))
  (provide-file (str data_dir graph_name "/stats"))
  {:name graph_name :nodes {}})

(defn write-node
  "Write a node to disk."
  [graph_name node data_dir]
  (def node_file (provide-file (str data_dir graph_name "/node")))

  (append-content node_file (:name node))
  (when (> (count (:descriptor_set node)) 0)
    (append-content node_file "|")
    (concat-append node_file (:descriptor_set node) "|"))
  (when (> (count (:attribute_map node)) 0)
    (append-content node_file "@")
    (concat-append node_file (reduce-kv #(conj %1 %3 (name %2)) '() (:attribute_map node)) "|"))
  (append-content node_file "^")

  (write-to-disk node_file))

(defn write-edge
  "Write a edge."
  [graph_name edge data_dir]
  (def edge_file (provide-file (str data_dir graph_name "/edge")))
  ; (concat-append edge_file (reduce-kv #(conj %1 (name %2) %3) '() edge) "|")
  (append-content edge_file (str (:u edge) "|"))
  (case (:direction edge)
    ("--" "<>" "->") (append-content edge_file "-|")
    "<-" (append-content edge_file "<|"))
  (append-content edge_file (str (:label edge) "|"))
  (case (:direction edge)
    ("--" "<>" "<-") (append-content edge_file "-|")
    "->" (append-content edge_file ">|"))
  (append-content edge_file (str (:v edge)))
  (append-content edge_file "^")
  (write-to-disk edge_file))

(defn delete-graph
  "Delete a graph on disk."
  [graph_name data_dir]
  (delete-file-recursively (str data_dir graph_name)))

(comment
  Structure of a graph
  {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"foo"}}}
   :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
   :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"bar" "baz"}}}}

  Structure of an edge
  {:u (nth edge_components 0) :label (nth edge_components 2) :v (nth edge_components 4)
   :direction (str (nth edge_components 1) (nth edge_components 3))}

  Structure of a node
  {:name (nth name_and_descriptors 0) :descriptor_set (set (rest name_and_descriptors))
   :attribute_map (zipmap (map keyword (take-nth 2 attributes)) (take-nth 2 (rest attributes)))
   :adjacency_map {}})