(ns dash.parse-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [dash.query.parse :refer :all :as parse]
            [dash.persistence.io :refer :all :as io]
            [test.util :refer :all]))

(deftest parse-create-node-block-test
  (with-files [["/node"]["/transactions"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (parse/parse-create-node-block "(a:b)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|b^"))
    (parse/parse-create-node-block "(c:d {e:f})")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|b^c|d@e|f^"))))

(deftest parse-create-rel-block-test
  (with-files [["/rel"]["/transactions"]]
    (def file (io/create-file (str tmp-dir "/rel")))
    (parse/parse-create-rel-block "(a)-[:b]->(c)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-contents file)) "a|-|:b|->|c^"))))