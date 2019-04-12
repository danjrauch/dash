(ns mesh.core.graph
  (:require [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.local :as l]))

(load "transform")

(defn add-node
  "Add a node to a graph."
  {:added "0.1.0"}
  [graph node]
  (assoc-in graph [:nodes (keyword (:name node))] node))

(defn add-edge
  "Add a edge to a graph."
  {:added "0.1.0"}
  [graph edge]
  (reduce #(update-in %1 [:nodes (keyword (nth %2 0)) :adjacency_map (keyword (nth %2 1))] (fnil conj #{(:label edge)}) (:label edge))
          graph
          (case (:direction edge)
            "--" [[(:u edge) (:v edge)] [(:v edge) (:u edge)]]
            "<-" [[(:v edge) (:u edge)]]
            "->" [[(:u edge) (:v edge)]])))

(defn return-node
  "Return a node if it exists in the graph."
  {:added "0.1.0"}
  [graph node_name]
  (when (contains? (:nodes graph) (keyword node_name))
    ((keyword node_name) (:nodes graph))))

(defn dfs
  "Depth first search from s for t in graph. Returns path from s to t if t is found, nil otherwise."
  {:added "0.1.0"}
  [graph s_name t_name]
  (loop [stack [(keyword s_name)] explored #{(keyword s_name)} preds {(keyword s_name) nil}]
    (if (empty? stack)
      nil
      (if (= (peek stack) (keyword t_name))
        (loop [p ((keyword t_name) preds) path (list t_name)]
          (if (= p nil)
            path
            (recur ((keyword p) preds) (conj path p))))
        (recur
         (into (pop stack) (remove explored (keys (:adjacency_map ((peek stack) (:nodes graph))))))
         (into explored (keys (:adjacency_map ((peek stack) (:nodes graph)))))
         (into preds (vec (for [n (keys (:adjacency_map ((peek stack) (:nodes graph))))] [n (name (peek stack))]))))))))

(defn bfs
  "Breadth first search from s for t in graph. Returns path from s to t if t is found, nil otherwise."
  {:added "0.1.0"}
  [graph s_name t_name]
  (loop [queue (conj clojure.lang.PersistentQueue/EMPTY (keyword s_name)) explored #{(keyword s_name)} preds {(keyword s_name) nil}]
    (if (empty? queue)
      nil
      (if (= (peek queue) (keyword t_name))
        (loop [p ((keyword t_name) preds) path (list t_name)]
          (if (= p nil)
            path
            (recur ((keyword p) preds) (conj path p))))
        (recur
         (into (pop queue) (remove explored (keys (:adjacency_map ((peek queue) (:nodes graph))))))
         (into explored (keys (:adjacency_map ((peek queue) (:nodes graph)))))
         (into preds (vec (for [n (keys (:adjacency_map ((peek queue) (:nodes graph))))] [n (name (peek queue))]))))))))

; (bfs {:name "g" :nodes {:a {:name "a" :descriptor_set #{}
;                             :attribute_map {}
;                             :adjacency_map {:b #{}}}
;                         :b {:name "b" :descriptor_set #{}
;                             :attribute_map {}
;                             :adjacency_map {:c #{}}}
;                         :c {:name "c" :descriptor_set #{}
;                             :attribute_map {}
;                             :adjacency_map {}}}}
;      "a" "c")

(comment
  Structure of a graph
  {:name "g" :nodes {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"foo"}}}
                     :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                     :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"bar" "baz"}}}}}

  Structure of an edge
  {:u "a" :label "foo" :v "b"
   :direction "--"}

  Structure of a node
  {:name "a" :descriptor_set #{"b"}
   :attribute_map {:c "d"}
   :adjacency_map {}})