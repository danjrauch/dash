(ns dash.data.write-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [dash.data.globals :refer :all]
            [dash.query.parse :as parse]
            [dash.persistence.io :as io]
            [dash.data.transform :as transform]
            [dash.data.read :as read]
            [dash.data.write :as write]))

(deftest create-graph-test
  (with-tmp-dir
    (write/create-graph "create_graph_test_graph")
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/create_graph_test_graph/node")))))
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/create_graph_test_graph/edge")))))
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/create_graph_test_graph/stats")))))
    (is (true? (contains? @global_graph_set "create_graph_test_graph")))
    (is (true? (contains? (read/read-graph-names) "create_graph_test_graph")))))

(deftest create-node-test
  (with-tmp-dir
    (write/create-graph "create_node_test_graph")
    (write/create-node "create_node_test_graph" (parse/parse-node-to-string "(a:b)"))
    (def node_file (io/provide-file (str tmp_dir "/create_node_test_graph/node")))
    (is (= (transform/bytes->string (io/get-contents node_file)) "a|b^"))
    (write/create-node "create_node_test_graph" (parse/parse-node-to-string "(w:x {y:z})"))
    (io/read-from-disk node_file)
    (is (= (transform/bytes->string (io/get-contents node_file)) "a|b^w|x@y|z^"))
    (is (true? (contains? @global_graph :a)))
    (is (true? (contains? @global_graph :w)))))

(deftest create-edge-test
  (with-tmp-dir
    (write/create-graph "create_edge_test_graph")
    (write/create-node "create_edge_test_graph" (parse/parse-node-to-string "(a:b)"))
    (write/create-node "create_edge_test_graph" (parse/parse-node-to-string "(c:d)"))
    (write/create-node "create_edge_test_graph" (parse/parse-node-to-string "(e:f)"))
    (write/create-node "create_edge_test_graph" (parse/parse-node-to-string "(g:h)"))
    (write/create-node "create_edge_test_graph" (parse/parse-node-to-string "(i:k)"))
    (write/create-node "create_edge_test_graph" (parse/parse-node-to-string "(j:l)"))
    (write/create-edge "create_edge_test_graph" (parse/parse-edge-to-string "(a)-[foo]-(c)"))
    (write/create-edge "create_edge_test_graph" (parse/parse-edge-to-string "(e)-[bar]->(g)"))
    (write/create-edge "create_edge_test_graph" (parse/parse-edge-to-string "(i)<-[baz]-(j)"))
    (def edge_file (io/provide-file (str tmp_dir "/create_edge_test_graph/edge")))
    (is (= (transform/bytes->string (io/get-contents edge_file)) "a|-|foo|-|c^e|-|bar|->|g^i|<-|baz|-|j^"))
    (is (true? (contains? (:adjacency_map (:a @global_graph)) :c)))
    (is (true? (contains? (:adjacency_map (:c @global_graph)) :a)))
    (is (true? (contains? (:adjacency_map (:e @global_graph)) :g)))
    (is (false? (contains? (:adjacency_map (:g @global_graph)) :e)))
    (is (true? (contains? (:adjacency_map (:j @global_graph)) :i)))
    (is (false? (contains? (:adjacency_map (:i @global_graph)) :j)))))

(deftest delete-graph-test
  (with-tmp-dir
    (write/create-graph "delete_graph_test_graph")
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/delete_graph_test_graph/node")))))
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/delete_graph_test_graph/edge")))))
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/delete_graph_test_graph/stats")))))
    (is (true? (contains? @global_graph_set "delete_graph_test_graph")))
    (is (true? (contains? (read/read-graph-names) "delete_graph_test_graph")))
    (write/delete-graph "delete_graph_test_graph")
    (is (false? (.exists (clojure.java.io/file (str tmp_dir "/delete_graph_test_graph/node")))))
    (is (false? (.exists (clojure.java.io/file (str tmp_dir "/delete_graph_test_graph/edge")))))
    (is (false? (.exists (clojure.java.io/file (str tmp_dir "/delete_graph_test_graph/stats")))))
    (is (false? (contains? @global_graph_set "delete_graph_test_graph")))
    (is (false? (contains? (read/read-graph-names) "delete_graph_test_graph")))))