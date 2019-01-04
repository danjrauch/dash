(ns dashdb.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [dashdb.persistence.io :refer :all :as io]
            [dashdb.query.parse :refer :all :as parse]
            [test.util :refer :all]))

(deftest write-to-file-test
  (with-files [["/node"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (io/read-from-file file)
    (io/append-content file "aaa")
    (io/write-to-file file)
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "aaa"))))

(deftest read-from-file-test
  (with-files [["/node" "12345"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "12345"))))

(deftest parse-create-node-block-test
  (with-files [["/node"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (parse/parse-create-node-block "(a:b)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "a|b^"))
    (parse/parse-create-node-block "(c:d {e:f})")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "a|b^c|d@e|f^"))))

(deftest parse-create-rel-block-test
  (with-files [["/rel"]]
    (def file (io/create-file (str tmp-dir "/rel")))
    (parse/parse-create-rel-block "(a)-[:b]->(c)")
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "a|-|:b|->|c^"))))
