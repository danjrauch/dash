(ns mesh.repl-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [mesh.graph :as graph]
            [mesh.persist :as persist]
            [mesh.repl :as repl]))

;;; Read/Write Tests



;; Parse Tests

(deftest parse-node-to-string-test
  (is (= "a|b@c|d" (repl/parse-node-to-string "(a:b {c:d})")))
  (is (= "a|b|c" (repl/parse-node-to-string "(a:b:c)"))))

(deftest parse-edge-to-string-test
  (is (= "a|-|con|-|b" (repl/parse-edge-to-string "(a)-[con]-(b)"))))

;; Execute Tests

(deftest create-graphs-test)

(deftest create-nodes-test
  (with-files [["graph_names" "X\n"] ["X/node"]]
    (def file (persist/provide-file (str repl/data_dir "X/node")))
    (repl/create-nodes "X" "(a:b)")
    (repl/create-nodes "X" "(c:d {e:f})")))

(deftest create-edges-test
  (with-files [["graph_names" "X\n"] ["X/edge"]]
    (def file (persist/provide-file (str repl/data_dir "X/edge")))
    (repl/create-edges "X" "(a)-[b]>(c)")))