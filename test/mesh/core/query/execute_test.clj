(ns mesh.core.query.execute-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [mesh.core.query.execute :as execute]
            [mesh.core.persistence.io :as io]
            [mesh.core.data.transform :as transform]))

(deftest execute-create-graph-query-test
  )

(deftest execute-create-node-query-test
  (with-files [["/graph_names" "X\n"] ["/X/node"]]
    (def file (io/provide-file (str tmp_dir "/X/node")))
    (execute/execute-create-node-query "X" "(a:b)")
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "a|b^"))
    (execute/execute-create-node-query "X" "(c:d {e:f})")
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "a|b^c|d@e|f^"))))

(deftest execute-create-edge-query-test
  (with-files [["/graph_names" "X\n"] ["/X/edge"]]
    (def file (io/provide-file (str tmp_dir "/X/edge")))
    (execute/execute-create-edge-query "X" "(a)-[b]->(c)")
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "a|-|b|->|c^"))))