(ns dashdb.io-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as javaio]
            [dashdb.persistence.io :refer :all :as io]
            [test.util :refer :all]))

;; File Tests

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

;; Buffer Manager Tests

(deftest get-file-position-negative-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file-position bm (str tmp-dir "/t1"))
  (is (= (io/get-file-position bm (str tmp-dir "/t1")) nil)))

(deftest get-file-negative-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file bm (str tmp-dir "/t1"))
  (is (= (io/get-file bm (str tmp-dir "/t1")) nil)))

(deftest get-file-position-positive-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file-position bm "No File")
  (is (= (io/get-file-position bm "No File") 0)))

(deftest get-file-positive-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file-position bm "No File")
  (is (= (io/get-file-name (io/get-file bm "No File")) "No File")))

(deftest insert-file-test
  (with-files [["/t1" "/t2" "/t3" "/t4" "/t5"]]
    (def bm (io/create-buffer-manager 5))
    (io/insert-file bm (str tmp-dir "/t1"))
    (is (= (io/get-file-position bm (str tmp-dir "/t1")) 0))
    (is (= (io/get-file-name (io/get-file bm (str tmp-dir "/t1"))) (str tmp-dir "/t1")))))

(deftest insert-file-to-overflow-test
  (with-files [["/t1" "/t2" "/t3" "/t4" "/t5"]]
    (doseq [x (range 10)]
      (def bm (io/create-buffer-manager 5))
      (io/insert-file bm (str tmp-dir "/t" (inc (mod x 5))))
      (is (= (io/get-file-position bm (str tmp-dir "/t1")) 0))
      (is (= (io/get-file-position bm (str tmp-dir "/t2")) 1))
      (is (= (io/get-file-position bm (str tmp-dir "/t3")) 2))
      (is (= (io/get-file-position bm (str tmp-dir "/t4")) 3))
      (is (= (io/get-file-position bm (str tmp-dir "/t5")) 4)))))
