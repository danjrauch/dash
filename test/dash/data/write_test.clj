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
    (is (true? (.exists (clojure.java.io/file (str tmp_dir "/create_graph_test_graph/rel")))))
    (is (true? (contains? @global_graph_set "create_graph_test_graph")))
    (is (true? (contains? (read/get-graph-names) "create_graph_test_graph")))))

(deftest create-node-test
  (with-tmp-dir
    (write/create-graph "create_node_test_graph")
    (write/create-node "create_node_test_graph" (parse/parse-create-node-block "(a:b)"))
    (def node_file (io/provide-file (str tmp_dir "/create_node_test_graph/node")))
    (is (= (transform/bytes->string (io/get-contents node_file)) "a|b^"))
    (write/create-node "create_node_test_graph" (parse/parse-create-node-block "(a:b {c:d})"))
    (io/read-from-disk node_file)
    (is (= (transform/bytes->string (io/get-contents node_file)) "a|b^a|b@c|d^"))))

(deftest create-relationship-test
  (with-tmp-dir
    (write/create-graph "create_relationship_test_graph")
    (write/create-relationship "create_relationship_test_graph" (parse/parse-create-relationship-block "(a)-[:b]->(c)"))
    (def rel_file (io/provide-file (str tmp_dir "/create_relationship_test_graph/rel")))
    (is (= (transform/bytes->string (io/get-contents rel_file)) "a|-|:b|->|c^"))))