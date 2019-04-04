(ns mesh.core.data.graph
  (:require [mesh.core.data.transform :as transform]))
  
(defn add-node
  "Add a node to a graph."
  [graph node]
  (assoc graph (keyword (:name node)) node))

(defn add-edge
  "Add a edge to a graph."
  [graph edge]
  (reduce #(update-in %1 [(keyword (nth %2 0)) :adjacency_map (keyword (nth %2 1))] (fnil conj #{(:label edge)}) (:label edge))
          graph
          (case (:direction edge)
            ("--" "<-->") [[(:u edge) (:v edge)] [(:v edge) (:u edge)]]
            "<--" [[(:v edge) (:u edge)]]
            "-->" [[(:u edge) (:v edge)]])))

(defn return-node
  "Return a node if it exists in the graph."
  [graph node_name]
  (when (contains? graph (keyword node_name))
    ((keyword node_name) graph)))

(comment 
  Structure of a graph
  {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"foo"}}}
   :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
   :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"bar" "baz"}}}})