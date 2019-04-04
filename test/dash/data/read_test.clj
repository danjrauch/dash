(ns dash.data.read-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [clojure.string :as str]
            [dash.persistence.io :as io]
            [dash.data.globals :refer :all]
            [dash.data.transform :as transform]
            [dash.data.read :as read]))

(deftest read-graph-names-test
  (with-files [["/graph_names" "X\nY\nZ\n"]]
    (def graph_name_set (read/read-graph-names))
    (is (true? (contains? graph_name_set "X")))
    (is (true? (contains? graph_name_set "Y")))
    (is (true? (contains? graph_name_set "Z")))))

(deftest read-graph-test
  (with-files [["/read_graph_test_graph1/node" "a^w"] ["/read_graph_test_graph1/edge" "a|-|con|-|w^"]
               ["/read_graph_test_graph2/node" "a|b@c|d^w|x@y|z"] ["/read_graph_test_graph2/edge" "a|-|con|-|w^"]
               ["/read_graph_test_graph3/node" "a|b@c|d^w|x@y|z"] ["/read_graph_test_graph3/edge" "a|<-|con|-|w^a|-|baz|->|w^"]
               ["/read_graph_test_graph4/node" "a|b@c|d^w|x@y|z"] ["/read_graph_test_graph4/edge" "a|-|foo|->|w^a|<-|bar|-|w^"]
               ["/read_graph_test_graph4/node" "a|b@c|d^w|x@y|z^h|i@j|k^"] ["/read_graph_test_graph4/edge" "a|-|foo|->|w^a|<-|bar|-|w^a|<-|baz|-|w^"]]
    (is (= (read/read-graph "read_graph_test_graph1") {:a {:name "a" :descriptor_set #{} :adjacency_map {:w #{"con"}}}
                                                       :w {:name "w" :descriptor_set #{} :adjacency_map {:a #{"con"}}}}))
    (is (= (read/read-graph "read_graph_test_graph2") {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"con"}}}
                                                       :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"con"}}}}))
    (is (= (read/read-graph "read_graph_test_graph3") {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"baz"}}}
                                                       :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"con"}}}}))
    (is (= (read/read-graph "read_graph_test_graph4") {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"foo"}}}
                                                       :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                                                       :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"bar" "baz"}}}}))))