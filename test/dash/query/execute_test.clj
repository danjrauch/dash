(ns dash.query.execute-test
  (:require [clojure.test :refer :all]
            [test.util :refer :all]
            [dash.query.execute :as execute]
            [dash.persistence.io :as io]
            [dash.data.transform :as transform]))

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

(deftest execute-create-relationship-query-test
  (with-files [["/graph_names" "X\n"] ["/X/rel"]]
    (def file (io/provide-file (str tmp_dir "/X/rel")))
    (execute/execute-create-relationship-query "X" "(a)-[:b]->(c)")
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "a|-|:b|->|c^"))))