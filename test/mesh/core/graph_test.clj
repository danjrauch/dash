(ns mesh.core.graph-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [mesh.core.graph :as graph]))

;; Creation Tests

(deftest string->node-test
  (is (= (graph/string->node "a") {:name "a" :descriptor_set #{} :attribute_map {} :adjacency_map {}}))
  (is (= (graph/string->node "a|b") {:name "a" :descriptor_set #{"b"} :attribute_map {} :adjacency_map {}}))
  (is (= (graph/string->node "a|b|c") {:name "a" :descriptor_set #{"b" "c"} :attribute_map {} :adjacency_map {}}))
  (is (= (graph/string->node "a|b|c@c|d") {:name "a" :descriptor_set #{"b" "c"} :attribute_map {:c "d"} :adjacency_map {}}))
  (is (= (graph/string->node "a|b|c@c|d|e|f") {:name "a" :descriptor_set #{"b" "c"} :attribute_map {:c "d" :e "f"} :adjacency_map {}})))

(deftest string->edge-test
  (is (= (graph/string->edge "a|-|con|-|b") {:u "a" :direction "--" :v "b" :label "con"})))

(deftest add-node-test
  (is (= (:nodes (graph/add-node {:name "X" :nodes {}} (graph/string->node "a|b@c|d"))) {:a {:name "a" :descriptor_set #{"b"} :adjacency_map {}
                                                                                             :attribute_map {:c "d"}}})))

(deftest add-node-test
  (is (= (:nodes (graph/add-edge {:name "X" :nodes {:a {:name "a" :adjacency_map {}} :w {:name "w" :adjacency_map {}}}}
                                 (graph/string->edge "a|-|foo|-|w")))
         {:a {:name "a" :adjacency_map {:w #{"foo"}}} :w {:name "w" :adjacency_map {:a #{"foo"}}}})))

;; Search Alg Tests

(deftest dfs-test
  ;; a -> b -> c path in the graph.
  (is (= (graph/dfs {:name "g" :nodes {:a {:name "a" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:b #{}}}
                                       :b {:name "b" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:c #{}}}
                                       :c {:name "c" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {}}}}
                    "a" "c") '("a" "b" "c")))
  (is (= (graph/dfs {:name "g" :nodes {:a {:name "a" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:b #{}}}
                                       :b {:name "b" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:c #{}}}
                                       :c {:name "c" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {}}}}
                    :a :d) nil)))

(deftest bfs-test
  ;; a -> b -> c path in the graph.
  (is (= (graph/bfs {:name "g" :nodes {:a {:name "a" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:b #{}}}
                                       :b {:name "b" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:c #{}}}
                                       :c {:name "c" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {}}}}
                    "a" "c") '("a" "b" "c")))
  (is (= (graph/bfs {:name "g" :nodes {:a {:name "a" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:b #{}}}
                                       :b {:name "b" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {:c #{}}}
                                       :c {:name "c" :descriptor_set #{}
                                           :attribute_map {}
                                           :adjacency_map {}}}}
                    :a :d) nil)))