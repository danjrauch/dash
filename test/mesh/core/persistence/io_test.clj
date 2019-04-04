(ns mesh.core.persistence.io-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.java.io :as javaio]
            [test.util :refer :all]
            [mesh.core.persistence.io :as io]
            [mesh.core.data.transform :as transform]))

;; File Tests

(deftest write-to-disk-test
  (with-files [["/t"]]
    (def file (io/create-file (str tmp_dir "/t")))
    (io/read-from-disk file)
    (io/append-content file "aaa")
    (io/write-to-disk file)
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "aaa"))))

(deftest read-from-disk-test
  (with-files [["/t" "12345"]]
    (def file (io/create-file (str tmp_dir "/t")))
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "12345"))))

(deftest create-file-on-write
  (def file (io/create-file (str tmp_dir "/t")))
  (io/append-content file "123")
  (io/write-to-disk file)
  (is (= (transform/bytes->string (io/get-contents file)) "123")))

(deftest multiple-read-write-test
  (with-files [["/t" "1"]]
    (def file (io/create-file (str tmp_dir "/t")))
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "1"))
    (io/append-content file "2")
    (io/write-to-disk file)
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "12"))
    (io/append-content file "345")
    (io/write-to-disk file)
    (io/read-from-disk file)
    (is (= (transform/bytes->string (io/get-contents file)) "12345"))))

(deftest set-dirty-value-test
  (with-files [["/t1"]["/t2"]["/t3"]["/t4"]["/t5"]]
    (def file (io/create-file (str tmp_dir "/t")))
    (io/set-dirty-value file 1)
    (is (= (io/get-dirty-value file) 1))
    (io/set-dirty-value file 0)
    (is (= (io/get-dirty-value file) 0))))

;; Buffer Manager Tests

(deftest get-file-position-negative-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file-position bm (str tmp_dir "/t1"))
  (is (= (io/get-file-position bm (str tmp_dir "/t1")) nil)))

(deftest get-file-negative-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file bm (str tmp_dir "/t1"))
  (is (= (io/get-file bm (str tmp_dir "/t1")) nil)))

(deftest get-file-position-positive-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file-position bm "No File")
  (is (= (io/get-file-position bm "No File") 0)))

(deftest get-file-positive-test
  (def bm (io/create-buffer-manager 5))
  (io/get-file-position bm "No File")
  (is (= (io/get-name (io/get-file bm "No File")) "No File")))

(deftest pin-file-test
  (with-files [["/t1"]["/t2"]["/t3"]["/t4"]["/t5"]]
    (def bm (io/create-buffer-manager 5))
    (io/pin-file bm (str tmp_dir "/t1"))
    (is (= (io/get-file-position bm (str tmp_dir "/t1")) 0))
    (is (= (io/get-name (io/get-file bm (str tmp_dir "/t1"))) (str tmp_dir "/t1")))))

(deftest unpin-file-test
  (with-files [["/t1"]]
    (def bm (io/create-buffer-manager 10))
    (io/pin-file bm (str tmp_dir "/t1"))
    (def file (io/get-file bm (str tmp_dir "/t1")))
    (io/append-content file "1234")
    (is (= (transform/bytes->string (io/get-contents file)) "1234"))
    (is (= (io/get-dirty-value file) true))
    (is (= (io/get-r-value file) 1))
    (io/unpin-file bm (str tmp_dir "/t1"))
    (is (= (io/get-file bm (str tmp_dir "/t1")) nil))))

(deftest pin-file-to-overflow-test
  (with-files [["/t1"]["/t2"]["/t3"]["/t4"]["/t5"]["/t6"]["/t7"]["/t8"]["/t9"]["/t10"]]
    (def bm (io/create-buffer-manager 5))
    (doseq [x (range 1 11)]
      (io/pin-file bm (str tmp_dir "/t" x)))
    (is (= (io/get-file-position bm (str tmp_dir "/t6")) 0))
    (is (= (io/get-file-position bm (str tmp_dir "/t7")) 1))
    (is (= (io/get-file-position bm (str tmp_dir "/t8")) 2))
    (is (= (io/get-file-position bm (str tmp_dir "/t9")) 3))
    (is (= (io/get-file-position bm (str tmp_dir "/t10")) 4))))

(deftest pin-file-to-overflow-with-hotfile-test
  (with-files [["/t1"]["/t2"]["/t3"]["/t4"]["/t5"]["/t6"]["/t7"]["/t8"]["/t9"]["/t10"]]
    (def bm (io/create-buffer-manager 5))
    (doseq [x (range 1 11)]
      (io/pin-file bm (str tmp_dir "/t" x))
      (io/pin-file bm (str tmp_dir "/t1")))
    (is (= (io/get-file-position bm (str tmp_dir "/t10")) 0))
    (is (= (io/get-file-position bm (str tmp_dir "/t1")) 1))
    (is (= (io/get-file-position bm (str tmp_dir "/t7")) 2))
    (is (= (io/get-file-position bm (str tmp_dir "/t8")) 3))
    (is (= (io/get-file-position bm (str tmp_dir "/t9")) 4))))

(deftest pin-file-to-overflow-with-dirty-test
  (with-files [["/t1" "12"]["/t2"]["/t3"]["/t4"]["/t5"]["/t6"]["/t7"]["/t8"]["/t9"]["/t10"]]
    (def bm (io/create-buffer-manager 5))
    (io/pin-file bm (str tmp_dir "/t1"))
    (io/append-content (io/get-file bm (str tmp_dir "/t1")) "34")
    (doseq [x (range 2 11)]
      (io/pin-file bm (str tmp_dir "/t" x)))
    (def file (io/create-file (str tmp_dir "/t1")))
    (io/read-from-disk file)
    (is (= (io/get-dirty-value file) false))
    (is (= (transform/bytes->string (io/get-contents file)) "1234"))))

(deftest write-files-to-disk-test
  (with-files [["/t1"]["/t2"]["/t3"]["/t4"]["/t5"]["/t6"]["/t7"]["/t8"]["/t9"]["/t10"]]
    (def bm (io/create-buffer-manager 10))
    (doseq [x (range 1 11)]
      (io/pin-file bm (str tmp_dir "/t" x))
      (io/append-content (io/get-file bm (str tmp_dir "/t" x)) (str x x x)))
    (io/write-files-to-disk bm)
    (doseq [x (range 1 11)]
      (def file (io/get-file bm (str tmp_dir "/t" x)))
      (io/read-from-disk file)
      (is (= (transform/bytes->string (io/get-contents file)) (str x x x)))
      (is (= (io/get-dirty-value file) false)))))

; User I/O Function Tests

