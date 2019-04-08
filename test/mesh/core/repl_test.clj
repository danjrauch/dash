(ns mesh.core.repl-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [mesh.core.graph :as graph]
            [mesh.core.persist :as persist]
            [mesh.core.repl :as repl]))

;;; Read/Write Tests



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
    (repl/execute-create-node-query "X" "(c:d {e:f})")))

(deftest execute-create-edge-query-test
  (with-files [["graph_names" "X\n"] ["X/edge"]]
    (def file (persist/provide-file (str repl/data_dir "X/edge")))
    (repl/execute-create-edge-query "X" "(a)-[b]>(c)")))