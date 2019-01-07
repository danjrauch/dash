(ns dashdb.io-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as javaio]
            [dashdb.persistence.io :refer :all :as io]
            [test.util :refer :all]))

(deftest write-to-file-test
  (with-files [["/t"]]
    (def file (io/create-file (str tmp-dir "/t")))
    (io/read-from-file file)
    (io/append-content file "aaa")
    (io/write-to-file file)
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "aaa"))))

(deftest read-from-file-test
  (with-files [["/t" "12345"]]
    (def file (io/create-file (str tmp-dir "/t")))
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "12345"))))

(deftest multiple-read-write-test
  (with-files [["/t" "1"]]
    (def file (io/create-file (str tmp-dir "/t")))
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "1"))
    (io/append-content file "2")
    (io/write-to-file file)
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "12"))
    (io/append-content file "345")
    (io/write-to-file file)
    (io/read-from-file file)
    (is (= (io/bytes->string (io/get-file-contents file)) "12345"))))

(deftest bytes->num-test
  (is (= (io/bytes->num (vec (map byte "AZEB"))) 1096435010)))
