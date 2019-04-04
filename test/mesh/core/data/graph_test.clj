(ns mesh.core.data.graph-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [clojure.string :as str]
            [mesh.core.data.graph :as graph]
            [mesh.core.data.transform :as transform]))

(deftest add-node-test
  (is (= (graph/add-node {} (transform/string->node "a|b@c|d")) {:a {:name "a" :descriptor_set #{"b"} :adjacency_map {}
                                                                         :attribute_map {:c "d"}}})))

(deftest add-node-test
  (is (= (graph/add-edge {:a {:name "a" :adjacency_map {}} :w {:name "w" :adjacency_map {}}}
                         (transform/string->edge "a|-|foo|-|w"))
         {:a {:name "a" :adjacency_map {:w #{"foo"}}} :w {:name "w" :adjacency_map {:a #{"foo"}}}})))