(ns dashdb.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [dashdb.persistence.io :refer :all :as io]
            [test.util :refer :all]))

(deftest write-to-file-test
  (with-files [["/node"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (io/read-from-file file)
    (io/append-content file "aaa")
    (io/write-to-file file)
    (io/read-from-file file)
    (is (= (io/get-contents file) [97 97 97]))))

(deftest read-from-file-test
  (with-files [["/node" "12345"]]
    (def file (io/create-file (str tmp-dir "/node")))
    (io/read-from-file file)
    (is (= (io/get-contents file) [49 50 51 52 53]))))
