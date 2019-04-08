(ns mesh.core.graph
  (:require [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.local :as l]))

(load "transform")

(defn add-node
  "Add a node to a graph."
  [graph node]
  (assoc-in graph [:nodes (keyword (:name node))] node))

(defn add-edge
  "Add a edge to a graph."
  [graph edge]
  (reduce #(update-in %1 [:nodes (keyword (nth %2 0)) :adjacency_map (keyword (nth %2 1))] (fnil conj #{(:label edge)}) (:label edge))
          graph
          (case (:direction edge)
            "--" [[(:u edge) (:v edge)] [(:v edge) (:u edge)]]
            "<-" [[(:v edge) (:u edge)]]
            "->" [[(:u edge) (:v edge)]])))

(defn return-node
  "Return a node if it exists in the graph."
  [graph node_name]
  (when (contains? (:nodes graph) (keyword node_name))
    ((keyword node_name) (:nodes graph))))

(comment
  Structure of a graph
  {:name "g" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"foo"}}}
                     :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                     :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"bar" "baz"}}}}}

  Structure of an edge
  {:u (nth edge_components 0) :label (nth edge_components 2) :v (nth edge_components 4)
   :direction (str (nth edge_components 1) (nth edge_components 3))}

  Structure of a node
  {:name (nth name_and_descriptors 0) :descriptor_set (set (rest name_and_descriptors))
   :attribute_map (zipmap (map keyword (take-nth 2 attributes)) (take-nth 2 (rest attributes)))
   :adjacency_map {}})