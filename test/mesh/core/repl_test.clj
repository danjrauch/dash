(ns mesh.core.repl-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [mesh.core.graph :as graph]
            [mesh.core.persist :as persist]
            [mesh.core.repl :as repl]))

;;; Read/Write Tests

(deftest create-graph-test
  (with-files [["graph_names"]]
    (persist/create-graph "create_graph_test_graph" repl/data_dir)
    (is (true? (.exists (clojure.java.io/file (str repl/data_dir "create_graph_test_graph/node")))))
    (is (true? (.exists (clojure.java.io/file (str repl/data_dir "create_graph_test_graph/edge")))))
    (is (true? (.exists (clojure.java.io/file (str repl/data_dir "create_graph_test_graph/stats")))))))

(deftest write-node-test
  (with-tmp-dir
    (persist/create-graph "create_node_test_graph" repl/data_dir)
    (persist/write-node "create_node_test_graph" {:name "a" :descriptor_set #{"b"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (def node_file (persist/provide-file (str repl/data_dir "create_node_test_graph/node")))
    (is (= (persist/get-contents node_file) "a|b^"))
    (persist/write-node "create_node_test_graph" {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {}} repl/data_dir)
    (persist/read-from-disk node_file)
    (is (= (persist/get-contents node_file) "a|b^w|x@y|z^"))))

(deftest write-edge-test
  (with-tmp-dir
    (persist/create-graph "create_edge_test_graph" repl/data_dir)
    (persist/write-node "create_edge_test_graph" {:name "a" :descriptor_set #{"b"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (persist/write-node "create_edge_test_graph" {:name "c" :descriptor_set #{"d"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (persist/write-node "create_edge_test_graph" {:name "e" :descriptor_set #{"f"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (persist/write-node "create_edge_test_graph" {:name "g" :descriptor_set #{"h"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (persist/write-node "create_edge_test_graph" {:name "i" :descriptor_set #{"k"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (persist/write-node "create_edge_test_graph" {:name "j" :descriptor_set #{"l"} :attribute_map {} :adjacency_map {}} repl/data_dir)
    (persist/write-edge "create_edge_test_graph" {:u "a" :label "foo" :v "c" :direction "--"} repl/data_dir)
    (persist/write-edge "create_edge_test_graph" {:u "e" :label "bar" :v "g" :direction "->"} repl/data_dir)
    (persist/write-edge "create_edge_test_graph" {:u "i" :label "baz" :v "j" :direction "<-"} repl/data_dir)
    (def edge_file (persist/provide-file (str repl/data_dir "create_edge_test_graph/edge")))
    (is (= (persist/get-contents edge_file) "a|-|foo|-|c^e|-|bar|>|g^i|<|baz|-|j^"))))

(deftest delete-graph-test
  (with-tmp-dir
    (persist/create-graph "delete_graph_test_graph" repl/data_dir)
    (is (true? (.exists (clojure.java.io/file (str repl/data_dir "delete_graph_test_graph/node")))))
    (is (true? (.exists (clojure.java.io/file (str repl/data_dir "delete_graph_test_graph/edge")))))
    (is (true? (.exists (clojure.java.io/file (str repl/data_dir "delete_graph_test_graph/stats")))))
    (persist/delete-graph "delete_graph_test_graph" repl/data_dir)
    (is (false? (.exists (clojure.java.io/file (str repl/data_dir "delete_graph_test_graph/node")))))
    (is (false? (.exists (clojure.java.io/file (str repl/data_dir "delete_graph_test_graph/edge")))))
    (is (false? (.exists (clojure.java.io/file (str repl/data_dir "delete_graph_test_graph/stats")))))))

(deftest read-graph-test
  (with-files [["read_graph_test_graph1/node" "a^w"] ["read_graph_test_graph1/edge" "a|-|con|-|w^"]
               ["read_graph_test_graph2/node" "a|b@c|d^w|x@y|z"] ["read_graph_test_graph2/edge" "a|-|con|-|w^"]
               ["read_graph_test_graph3/node" "a|b@c|d^w|x@y|z"] ["read_graph_test_graph3/edge" "a|<|con|-|w^a|-|baz|>|w^"]
               ["read_graph_test_graph4/node" "a|b@c|d^w|x@y|z"] ["read_graph_test_graph4/edge" "a|-|foo|>|w^a|<|bar|-|w^"]
               ["read_graph_test_graph4/node" "a|b@c|d^w|x@y|z^h|i@j|k^"] ["read_graph_test_graph4/edge" "a|-|foo|>|w^a|<|bar|-|w^a|<|baz|-|w^"]]
    (is (= (:nodes (persist/read-graph "read_graph_test_graph1" repl/data_dir)) {:a {:name "a" :descriptor_set #{} :attribute_map {} :adjacency_map {:w #{"con"}}}
                                                                                                   :w {:name "w" :descriptor_set #{} :attribute_map {} :adjacency_map {:a #{"con"}}}}))
    (is (= (:nodes (persist/read-graph "read_graph_test_graph2" repl/data_dir)) {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"con"}}}
                                                                                                   :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"con"}}}}))
    (is (= (:nodes (persist/read-graph "read_graph_test_graph3" repl/data_dir)) {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"baz"}}}
                                                                                                   :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"con"}}}}))
    (is (= (:nodes (persist/read-graph "read_graph_test_graph4" repl/data_dir)) {:a {:name "a" :descriptor_set #{"b"} :attribute_map {:c "d"} :adjacency_map {:w #{"foo"}}}
                                                                                                   :h {:name "h" :descriptor_set #{"i"} :attribute_map {:j "k"} :adjacency_map {}}
                                                                                                   :w {:name "w" :descriptor_set #{"x"} :attribute_map {:y "z"} :adjacency_map {:a #{"bar" "baz"}}}}))))

(deftest read-graph-names-test
  (with-files [["graph_names" "X\nY\nZ\n"]]
    (def graph_name_set (persist/read-graph-names repl/data_dir))
    (is (true? (contains? graph_name_set "X")))
    (is (true? (contains? graph_name_set "Y")))
    (is (true? (contains? graph_name_set "Z")))))

;; Parse Tests

(deftest parse-node-to-string-test
  (is (= "a|b@c|d" (repl/parse-node-to-string "(a:b {c:d})")))
  (is (= "a|b|c" (repl/parse-node-to-string "(a:b:c)"))))

(deftest parse-edge-to-string-test
  (is (= "a|-|con|-|b" (repl/parse-edge-to-string "(a)-[con]-(b)"))))

;; Execute Tests

(deftest execute-create-graph-query-test)

(deftest execute-create-node-query-test
  (with-files [["graph_names" "X\n"] ["X/node"]]
    (def file (persist/provide-file (str repl/data_dir "X/node")))
    (repl/execute-create-node-query "X" "(a:b)")
    (persist/read-from-disk file)
    (is (= (persist/get-contents file) "a|b^"))
    (repl/execute-create-node-query "X" "(c:d {e:f})")
    (persist/read-from-disk file)
    (is (= (persist/get-contents file) "a|b^c|d@e|f^"))))

(deftest execute-create-edge-query-test
  (with-files [["graph_names" "X\n"] ["X/edge"]]
    (def file (persist/provide-file (str repl/data_dir "X/edge")))
    (repl/execute-create-edge-query "X" "(a)-[b]>(c)")
    (persist/read-from-disk file)
    (is (= (persist/get-contents file) "a|-|b|>|c^"))))