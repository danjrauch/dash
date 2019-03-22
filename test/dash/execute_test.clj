(ns dash.execute-test
  (:require [clojure.test :refer :all]
            [dash.query.execute :as execute]
            [dash.persistence.io :as io]
            [test.util :refer :all]))

(deftest execute-create-node-query-test
  (with-files [["/node"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (execute/execute-create-node-query "(a:b)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|b^"))
    (execute/execute-create-node-query "(c:d {e:f})")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|b^c|d@e|f^"))))

(deftest execute-create-relationship-query-test
  (with-files [["/rel"]]
    (def file (io/create-file (str tmp-dir "/rel")))
    (execute/execute-create-relationship-query "(a)-[:b]->(c)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|-|:b|->|c^"))))