(ns dashdb.core-test
  (:require [clojure.test :refer :all]
            [clojure.java [io :as io]]
            [dashdb.fs :refer :all :as fs]))

(deftest write-to-file-test
  (clojure.java.io/file "test_resources/node.dir")
  (fs/write-to-file (fs/append-content {:name "test_resources/node.dir"} "123"))
  (def file (fs/read-from-file {:name "test_resources/node.dir"}))
  (is (= (:contents file) [49 50 51]))
  (io/delete-file "test_resources/node.dir")
  )

(deftest read-from-file-test
  (clojure.java.io/file "test_resources/node.dir")
  (spit "test_resources/node.dir" "12345")
  (is (= (:contents (fs/read-from-file {:name "test_resources/node.dir"})) [49 50 51 52 53]))
  (io/delete-file "test_resources/node.dir"))
